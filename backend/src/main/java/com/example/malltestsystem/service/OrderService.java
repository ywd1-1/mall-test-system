package com.example.malltestsystem.service;

import com.example.malltestsystem.common.BusinessException;
import com.example.malltestsystem.common.UserContext;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.Cart;
import com.example.malltestsystem.entity.OrderEntity;
import com.example.malltestsystem.entity.OrderItem;
import com.example.malltestsystem.entity.OrderStatusLog;
import com.example.malltestsystem.entity.Product;
import com.example.malltestsystem.entity.User;
import com.example.malltestsystem.entity.UserAddress;
import com.example.malltestsystem.repository.CartRepository;
import com.example.malltestsystem.repository.OrderItemRepository;
import com.example.malltestsystem.repository.OrderRepository;
import com.example.malltestsystem.repository.OrderStatusLogRepository;
import com.example.malltestsystem.repository.ProductRepository;
import com.example.malltestsystem.repository.UserAddressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserAddressRepository addressRepository;
    private final OrderStatusLogRepository statusLogRepository;

    public OrderService(CartRepository cartRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserAddressRepository addressRepository,
                        OrderStatusLogRepository statusLogRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
        this.statusLogRepository = statusLogRepository;
    }

    @Transactional
    public ApiDtos.OrderResponse createOrder(ApiDtos.CreateOrderRequest request) {
        User user = requireNormalUser();
        if (request.getCartIds() == null || request.getCartIds().isEmpty()) {
            throw BusinessException.badRequest("请选择要结算的商品");
        }
        UserAddress address = addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> BusinessException.notFound("收货地址不存在"));
        List<Cart> carts = loadCartsForOrder(user.getId(), request.getCartIds());
        if (carts.isEmpty()) {
            throw BusinessException.badRequest("购物车为空，不能创建订单");
        }
        carts.sort(Comparator.comparing(cart -> cart.getProduct().getId()));

        LocalDateTime now = LocalDateTime.now();
        OrderEntity order = new OrderEntity();
        order.setOrderNo(generateOrderNo(user.getId()));
        order.setUser(user);
        order.setStatus(OrderEntity.STATUS_CREATED);
        order.setTotalAmount(BigDecimal.ZERO);
        applyAddressSnapshot(order, address);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Long> orderedCartIds = new ArrayList<Long>();
        for (Cart cart : carts) {
            Product product = productRepository.findByIdForUpdate(cart.getProduct().getId())
                    .orElseThrow(() -> BusinessException.notFound("商品不存在"));
            if (Boolean.TRUE.equals(product.getDeleted()) || !Product.STATUS_ON_SALE.equals(product.getStatus())) {
                throw BusinessException.badRequest("商品已下架，不能创建订单");
            }
            if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
                throw BusinessException.badRequest("商品数量必须大于 0");
            }
            if (product.getStock() < cart.getQuantity()) {
                throw BusinessException.badRequest("商品库存不足");
            }

            product.setStock(product.getStock() - cart.getQuantity());
            product.setUpdatedAt(now);
            productRepository.save(product);

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImageUrl(product.getImageUrl());
            item.setPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(subtotal);
            orderItemRepository.save(item);
            totalAmount = totalAmount.add(subtotal);
            orderedCartIds.add(cart.getId());
        }

        order.setTotalAmount(totalAmount);
        order.setUpdatedAt(now);
        orderRepository.save(order);
        writeStatusLog(order, null, OrderEntity.STATUS_CREATED, user, "创建订单");
        cartRepository.deleteByUserIdAndIdIn(user.getId(), orderedCartIds);
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public ApiDtos.PageResponse<ApiDtos.OrderResponse> listCurrentUserOrders(String status, int page, int size) {
        User user = requireNormalUser();
        String normalizedStatus = normalizeOrderStatus(status);
        Specification<OrderEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(root.get("user").get("id"), user.getId()));
            if (normalizedStatus != null) {
                predicates.add(builder.equal(root.get("status"), normalizedStatus));
            }
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        return toPageResponse(orderRepository.findAll(specification, buildPageable(page, size)));
    }

    @Transactional(readOnly = true)
    public ApiDtos.OrderResponse getCurrentUserOrder(Long id) {
        User user = requireNormalUser();
        OrderEntity order = orderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> BusinessException.notFound("订单不存在"));
        return toOrderResponse(order);
    }

    @Transactional
    public void cancelCurrentUserOrder(Long id) {
        User user = requireNormalUser();
        OrderEntity order = orderRepository.findByIdAndUserIdForUpdate(id, user.getId())
                .orElseThrow(() -> BusinessException.notFound("订单不存在"));
        cancelOrder(order, user);
    }

    @Transactional
    public ApiDtos.OrderResponse payCurrentUserOrder(Long id) {
        User user = requireNormalUser();
        OrderEntity order = orderRepository.findByIdAndUserIdForUpdate(id, user.getId())
                .orElseThrow(() -> BusinessException.notFound("订单不存在"));
        if (OrderEntity.STATUS_PAID.equals(order.getStatus())) {
            throw BusinessException.badRequest("订单已支付，不能重复支付");
        }
        if (OrderEntity.STATUS_CANCELLED.equals(order.getStatus())) {
            throw BusinessException.badRequest("订单已取消，不能支付");
        }
        if (!OrderEntity.STATUS_CREATED.equals(order.getStatus())) {
            throw BusinessException.badRequest("当前订单状态不能支付");
        }
        changeStatus(order, OrderEntity.STATUS_PAID, user, "用户完成支付");
        return toOrderResponse(order);
    }

    @Transactional
    public ApiDtos.OrderResponse confirmReceipt(Long id) {
        User user = requireNormalUser();
        OrderEntity order = orderRepository.findByIdAndUserIdForUpdate(id, user.getId())
                .orElseThrow(() -> BusinessException.notFound("订单不存在"));
        if (OrderEntity.STATUS_COMPLETED.equals(order.getStatus())) {
            throw BusinessException.badRequest("订单已完成，不能重复确认收货");
        }
        if (!OrderEntity.STATUS_SHIPPED.equals(order.getStatus())) {
            throw BusinessException.badRequest("只有已发货订单可以确认收货");
        }
        changeStatus(order, OrderEntity.STATUS_COMPLETED, user, "用户确认收货");
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public ApiDtos.PageResponse<ApiDtos.OrderResponse> listAllOrdersForAdmin(String orderNo,
                                                                             String username,
                                                                             String status,
                                                                             LocalDateTime startTime,
                                                                             LocalDateTime endTime,
                                                                             int page,
                                                                             int size) {
        requireAdmin();
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw BusinessException.badRequest("开始时间不能晚于结束时间");
        }
        String normalizedStatus = normalizeOrderStatus(status);
        Specification<OrderEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            addLikePredicate(predicates, builder, root.<String>get("orderNo"), orderNo);
            addLikePredicate(predicates, builder, root.get("user").<String>get("username"), username);
            if (normalizedStatus != null) {
                predicates.add(builder.equal(root.get("status"), normalizedStatus));
            }
            if (startTime != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.<LocalDateTime>get("createdAt"), startTime));
            }
            if (endTime != null) {
                predicates.add(builder.lessThanOrEqualTo(root.<LocalDateTime>get("createdAt"), endTime));
            }
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        return toPageResponse(orderRepository.findAll(specification, buildPageable(page, size)));
    }

    @Transactional(readOnly = true)
    public ApiDtos.OrderResponse getOrderForAdmin(Long id) {
        requireAdmin();
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("订单不存在"));
        return toOrderResponse(order);
    }

    @Transactional
    public ApiDtos.OrderResponse shipOrder(Long id, ApiDtos.ShippingRequest request) {
        User admin = requireAdmin();
        OrderEntity order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> BusinessException.notFound("订单不存在"));
        if (OrderEntity.STATUS_SHIPPED.equals(order.getStatus())) {
            throw BusinessException.badRequest("订单已发货，不能重复发货");
        }
        if (OrderEntity.STATUS_COMPLETED.equals(order.getStatus())) {
            throw BusinessException.badRequest("订单已完成，不能发货");
        }
        if (!OrderEntity.STATUS_PAID.equals(order.getStatus())) {
            throw BusinessException.badRequest("只有已支付订单可以发货");
        }
        order.setShippingCompany(request.getShippingCompany().trim());
        order.setTrackingNumber(request.getTrackingNumber().trim());
        changeStatus(order, OrderEntity.STATUS_SHIPPED, admin,
                "管理员发货：" + order.getShippingCompany() + " / " + order.getTrackingNumber());
        return toOrderResponse(order);
    }

    private void cancelOrder(OrderEntity order, User operator) {
        if (OrderEntity.STATUS_CANCELLED.equals(order.getStatus())) {
            throw BusinessException.badRequest("订单已取消，不能重复取消");
        }
        if (!OrderEntity.STATUS_CREATED.equals(order.getStatus())) {
            throw BusinessException.badRequest("只有待支付订单可以取消");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        items.sort(Comparator.comparing(OrderItem::getProductId));
        for (OrderItem item : items) {
            Product product = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> BusinessException.notFound("订单商品不存在，无法恢复库存"));
            product.setStock(product.getStock() + item.getQuantity());
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        }
        changeStatus(order, OrderEntity.STATUS_CANCELLED, operator, "用户取消订单并恢复库存");
    }

    private void changeStatus(OrderEntity order, String newStatus, User operator, String remark) {
        String oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        writeStatusLog(order, oldStatus, newStatus, operator, remark);
    }

    private void writeStatusLog(OrderEntity order, String oldStatus, String newStatus, User operator, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(order);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getUsername());
        log.setOperatorRole(operator.getRole());
        log.setOperatedAt(LocalDateTime.now());
        log.setRemark(remark);
        statusLogRepository.save(log);
    }

    private void applyAddressSnapshot(OrderEntity order, UserAddress address) {
        order.setAddressId(address.getId());
        order.setRecipientName(address.getRecipientName());
        order.setRecipientPhone(address.getPhone());
        order.setProvince(address.getProvince());
        order.setCity(address.getCity());
        order.setDistrict(address.getDistrict());
        order.setDetailAddress(address.getDetailAddress());
    }

    private String normalizeOrderStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (OrderEntity.STATUS_CREATED.equals(normalized)
                || OrderEntity.STATUS_PAID.equals(normalized)
                || OrderEntity.STATUS_SHIPPED.equals(normalized)
                || OrderEntity.STATUS_COMPLETED.equals(normalized)
                || OrderEntity.STATUS_CANCELLED.equals(normalized)) {
            return normalized;
        }
        throw BusinessException.badRequest("订单状态不合法");
    }

    private List<Cart> loadCartsForOrder(Long userId, List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            throw BusinessException.badRequest("请选择要结算的商品");
        }
        Set<Long> uniqueIds = new LinkedHashSet<Long>(cartIds);
        if (uniqueIds.contains(null)) {
            throw BusinessException.badRequest("购物车 ID 不能为空");
        }
        List<Cart> carts = cartRepository.findByUserIdAndIdIn(userId, uniqueIds);
        if (carts.size() != uniqueIds.size()) {
            throw BusinessException.badRequest("购物车商品不存在或不属于当前用户");
        }
        return carts;
    }

    private Pageable buildPageable(int page, int size) {
        if (page < 1) {
            throw BusinessException.badRequest("页码必须大于等于 1");
        }
        if (size < 1 || size > 100) {
            throw BusinessException.badRequest("每页数量必须在 1 到 100 之间");
        }
        return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
    }

    private String generateOrderNo(Long userId) {
        return "O" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + userId
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private User requireLoginUser() {
        User user = UserContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("未登录");
        }
        return user;
    }

    private User requireNormalUser() {
        User user = requireLoginUser();
        if (!User.ROLE_USER.equals(user.getRole())) {
            throw BusinessException.forbidden("该接口仅供普通用户访问");
        }
        return user;
    }

    private User requireAdmin() {
        User user = requireLoginUser();
        if (!User.ROLE_ADMIN.equals(user.getRole())) {
            throw BusinessException.forbidden("只有管理员可以访问该接口");
        }
        return user;
    }

    private void addLikePredicate(List<Predicate> predicates,
                                  javax.persistence.criteria.CriteriaBuilder builder,
                                  javax.persistence.criteria.Expression<String> expression,
                                  String value) {
        if (value != null && !value.trim().isEmpty()) {
            predicates.add(builder.like(builder.lower(expression), "%" + value.trim().toLowerCase() + "%"));
        }
    }

    private ApiDtos.PageResponse<ApiDtos.OrderResponse> toPageResponse(Page<OrderEntity> orders) {
        return new ApiDtos.PageResponse<ApiDtos.OrderResponse>(
                toOrderResponses(orders.getContent()),
                orders.getTotalElements(),
                orders.getNumber() + 1,
                orders.getSize()
        );
    }

    private List<ApiDtos.OrderResponse> toOrderResponses(List<OrderEntity> orders) {
        List<ApiDtos.OrderResponse> responses = new ArrayList<ApiDtos.OrderResponse>();
        for (OrderEntity order : orders) {
            responses.add(toOrderResponse(order));
        }
        return responses;
    }

    private ApiDtos.OrderResponse toOrderResponse(OrderEntity order) {
        ApiDtos.OrderResponse response = new ApiDtos.OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setUserId(order.getUser().getId());
        response.setUsername(order.getUser().getUsername());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setAddressId(order.getAddressId());
        response.setRecipientName(order.getRecipientName());
        response.setRecipientPhone(order.getRecipientPhone());
        response.setProvince(order.getProvince());
        response.setCity(order.getCity());
        response.setDistrict(order.getDistrict());
        response.setDetailAddress(order.getDetailAddress());
        response.setShippingCompany(order.getShippingCompany());
        response.setTrackingNumber(order.getTrackingNumber());
        response.setItems(toOrderItemResponses(orderItemRepository.findByOrderId(order.getId())));
        response.setStatusLogs(toStatusLogResponses(statusLogRepository.findByOrderIdOrderByIdAsc(order.getId())));
        return response;
    }

    private List<ApiDtos.OrderItemResponse> toOrderItemResponses(List<OrderItem> items) {
        List<ApiDtos.OrderItemResponse> responses = new ArrayList<ApiDtos.OrderItemResponse>();
        for (OrderItem item : items) {
            ApiDtos.OrderItemResponse response = new ApiDtos.OrderItemResponse();
            response.setId(item.getId());
            response.setProductId(item.getProductId());
            response.setProductName(item.getProductName());
            response.setProductImageUrl(item.getProductImageUrl());
            response.setPrice(item.getPrice());
            response.setQuantity(item.getQuantity());
            response.setSubtotal(item.getSubtotal());
            responses.add(response);
        }
        return responses;
    }

    private List<ApiDtos.OrderStatusLogResponse> toStatusLogResponses(List<OrderStatusLog> logs) {
        List<ApiDtos.OrderStatusLogResponse> responses = new ArrayList<ApiDtos.OrderStatusLogResponse>();
        for (OrderStatusLog log : logs) {
            ApiDtos.OrderStatusLogResponse response = new ApiDtos.OrderStatusLogResponse();
            response.setId(log.getId());
            response.setOldStatus(log.getOldStatus());
            response.setNewStatus(log.getNewStatus());
            response.setOperatorId(log.getOperatorId());
            response.setOperatorName(log.getOperatorName());
            response.setOperatorRole(log.getOperatorRole());
            response.setOperatedAt(log.getOperatedAt());
            response.setRemark(log.getRemark());
            responses.add(response);
        }
        return responses;
    }
}
