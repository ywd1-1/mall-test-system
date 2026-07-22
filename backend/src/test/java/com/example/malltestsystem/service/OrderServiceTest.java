package com.example.malltestsystem.service;

import com.example.malltestsystem.common.BusinessException;
import com.example.malltestsystem.common.UserContext;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.OrderEntity;
import com.example.malltestsystem.entity.OrderItem;
import com.example.malltestsystem.entity.Product;
import com.example.malltestsystem.entity.User;
import com.example.malltestsystem.repository.CartRepository;
import com.example.malltestsystem.repository.OrderItemRepository;
import com.example.malltestsystem.repository.OrderRepository;
import com.example.malltestsystem.repository.OrderStatusLogRepository;
import com.example.malltestsystem.repository.ProductRepository;
import com.example.malltestsystem.repository.UserAddressRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderServiceTest {
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private OrderStatusLogRepository statusLogRepository;
    private UserAddressRepository addressRepository;
    private OrderService orderService;
    private User user;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        productRepository = mock(ProductRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        addressRepository = mock(UserAddressRepository.class);
        statusLogRepository = mock(OrderStatusLogRepository.class);
        orderService = new OrderService(
                cartRepository,
                productRepository,
                orderRepository,
                orderItemRepository,
                addressRepository,
                statusLogRepository);

        user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setRole(User.ROLE_USER);
        user.setStatus(User.STATUS_ACTIVE);
        UserContext.set(user);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void repeatedCancellationRestoresStockOnlyOnce() {
        OrderEntity order = order(OrderEntity.STATUS_CREATED);
        OrderItem item = new OrderItem();
        item.setProductId(10L);
        item.setQuantity(2);
        Product product = new Product();
        product.setId(10L);
        product.setStock(8);

        when(orderRepository.findByIdAndUserIdForUpdate(100L, user.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(Collections.singletonList(item));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(product));

        orderService.cancelCurrentUserOrder(100L);
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancelCurrentUserOrder(100L));

        assertEquals("订单已取消，不能重复取消", exception.getMessage());
        assertEquals(10, product.getStock());
        verify(productRepository, times(1)).findByIdForUpdate(10L);
        verify(productRepository, times(1)).save(product);
        verify(statusLogRepository, times(1)).save(any());
    }

    @Test
    void cancelledOrderCannotBePaid() {
        OrderEntity order = order(OrderEntity.STATUS_CANCELLED);
        when(orderRepository.findByIdAndUserIdForUpdate(100L, user.getId())).thenReturn(Optional.of(order));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.payCurrentUserOrder(100L));

        assertEquals("订单已取消，不能支付", exception.getMessage());
        verify(orderRepository, never()).save(any());
        verify(statusLogRepository, never()).save(any());
    }

    @Test
    void emptySelectionCannotCreateOrder() {
        ApiDtos.CreateOrderRequest request = new ApiDtos.CreateOrderRequest();
        request.setAddressId(1L);
        request.setCartIds(Collections.emptyList());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(request));

        assertEquals("请选择要结算的商品", exception.getMessage());
        verifyNoInteractions(addressRepository, cartRepository, productRepository,
                orderRepository, orderItemRepository, statusLogRepository);
    }

    private OrderEntity order(String status) {
        OrderEntity order = new OrderEntity();
        order.setId(100L);
        order.setUser(user);
        order.setStatus(status);
        return order;
    }
}
