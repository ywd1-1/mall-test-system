package com.example.malltestsystem.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {
    }

    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在 3 到 50 个字符之间")
        private String username;
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度必须在 6 到 100 个字符之间")
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest extends LoginRequest {
    }

    public static class UserResponse {
        private Long id;
        private String username;
        private String role;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public UserResponse() {
        }

        public UserResponse(Long id, String username, String role, String status) {
            this.id = id;
            this.username = username;
            this.role = role;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class TokenResponse {
        private String token;
        private UserResponse user;

        public TokenResponse() {
        }

        public TokenResponse(String token, UserResponse user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public UserResponse getUser() {
            return user;
        }

        public void setUser(UserResponse user) {
            this.user = user;
        }
    }

    public static class ProductRequest {
        @NotBlank(message = "商品名称不能为空")
        @Size(max = 100, message = "商品名称长度不能超过 100 个字符")
        private String name;
        @NotNull(message = "商品价格不能为空")
        @DecimalMin(value = "0.01", message = "商品价格必须大于 0")
        private BigDecimal price;
        @NotNull(message = "商品库存不能为空")
        @DecimalMin(value = "0", message = "库存必须为非负整数")
        @DecimalMax(value = "2147483647", message = "库存必须为非负整数")
        @Digits(integer = 10, fraction = 0, message = "库存必须为非负整数")
        private BigDecimal stock;
        @NotBlank(message = "商品分类不能为空")
        @Size(max = 50, message = "商品分类长度不能超过 50 个字符")
        private String category;
        @Size(max = 20, message = "商品状态长度不能超过 20 个字符")
        private String status;
        @Size(max = 500, message = "图片地址长度不能超过 500 个字符")
        private String imageUrl;
        @Size(max = 1000, message = "商品描述长度不能超过 1000 个字符")
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getStock() {
            return stock;
        }

        public void setStock(BigDecimal stock) {
            this.stock = stock;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class ProductResponse {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer stock;
        private String category;
        private String status;
        private String imageUrl;
        private String description;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class AddCartRequest {
        @NotNull(message = "商品 ID 不能为空")
        private Long productId;
        @NotNull(message = "商品数量不能为空")
        @Min(value = 1, message = "商品数量必须大于 0")
        private Integer quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    public static class UpdateCartRequest {
        @NotNull(message = "商品数量不能为空")
        @Min(value = 1, message = "商品数量必须大于 0")
        private Integer quantity;

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    public static class CartItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer stock;
        private Integer quantity;
        private String productStatus;
        private String imageUrl;
        private BigDecimal subtotal;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getProductStatus() {
            return productStatus;
        }

        public void setProductStatus(String productStatus) {
            this.productStatus = productStatus;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }

    public static class CreateOrderRequest {
        @NotEmpty(message = "请选择要结算的商品")
        @Size(max = 100, message = "单次最多结算 100 个购物车条目")
        private List<Long> cartIds;

        @NotNull(message = "收货地址 ID 不能为空")
        private Long addressId;

        public List<Long> getCartIds() {
            return cartIds;
        }

        public void setCartIds(List<Long> cartIds) {
            this.cartIds = cartIds;
        }

        public Long getAddressId() {
            return addressId;
        }

        public void setAddressId(Long addressId) {
            this.addressId = addressId;
        }
    }

    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImageUrl;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductImageUrl() {
            return productImageUrl;
        }

        public void setProductImageUrl(String productImageUrl) {
            this.productImageUrl = productImageUrl;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }

    public static class OrderResponse {
        private Long id;
        private String orderNo;
        private Long userId;
        private String username;
        private String status;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long addressId;
        private String recipientName;
        private String recipientPhone;
        private String province;
        private String city;
        private String district;
        private String detailAddress;
        private String shippingCompany;
        private String trackingNumber;
        private List<OrderItemResponse> items;
        private List<OrderStatusLogResponse> statusLogs;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        public Long getAddressId() { return addressId; }
        public void setAddressId(Long addressId) { this.addressId = addressId; }
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        public String getRecipientPhone() { return recipientPhone; }
        public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getDetailAddress() { return detailAddress; }
        public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
        public String getShippingCompany() { return shippingCompany; }
        public void setShippingCompany(String shippingCompany) { this.shippingCompany = shippingCompany; }
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

        public List<OrderItemResponse> getItems() {
            return items;
        }

        public void setItems(List<OrderItemResponse> items) {
            this.items = items;
        }

        public List<OrderStatusLogResponse> getStatusLogs() { return statusLogs; }
        public void setStatusLogs(List<OrderStatusLogResponse> statusLogs) { this.statusLogs = statusLogs; }
    }

    public static class OrderStatusLogResponse {
        private Long id;
        private String oldStatus;
        private String newStatus;
        private Long operatorId;
        private String operatorName;
        private String operatorRole;
        private LocalDateTime operatedAt;
        private String remark;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOldStatus() { return oldStatus; }
        public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public String getOperatorRole() { return operatorRole; }
        public void setOperatorRole(String operatorRole) { this.operatorRole = operatorRole; }
        public LocalDateTime getOperatedAt() { return operatedAt; }
        public void setOperatedAt(LocalDateTime operatedAt) { this.operatedAt = operatedAt; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    public static class AddressRequest {
        @NotBlank(message = "收货人不能为空")
        @Size(min = 2, max = 50, message = "收货人长度必须在 2 到 50 个字符之间")
        private String recipientName;
        @NotBlank(message = "手机号不能为空")
        @Size(min = 6, max = 20, message = "手机号长度必须在 6 到 20 个字符之间")
        private String phone;
        @NotBlank(message = "省份不能为空")
        @Size(max = 50, message = "省份长度不能超过 50 个字符")
        private String province;
        @NotBlank(message = "城市不能为空")
        @Size(max = 50, message = "城市长度不能超过 50 个字符")
        private String city;
        @NotBlank(message = "区县不能为空")
        @Size(max = 50, message = "区县长度不能超过 50 个字符")
        private String district;
        @NotBlank(message = "详细地址不能为空")
        @Size(min = 5, max = 200, message = "详细地址长度必须在 5 到 200 个字符之间")
        private String detailAddress;
        private Boolean isDefault;

        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getDetailAddress() { return detailAddress; }
        public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
        public Boolean getIsDefault() { return isDefault; }
        public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    }

    public static class AddressResponse {
        private Long id;
        private String recipientName;
        private String phone;
        private String province;
        private String city;
        private String district;
        private String detailAddress;
        private Boolean isDefault;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getDetailAddress() { return detailAddress; }
        public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
        public Boolean getIsDefault() { return isDefault; }
        public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class ShippingRequest {
        @NotBlank(message = "物流公司不能为空")
        @Size(max = 100, message = "物流公司长度不能超过 100 个字符")
        private String shippingCompany;
        @NotBlank(message = "物流单号不能为空")
        @Size(max = 100, message = "物流单号长度不能超过 100 个字符")
        private String trackingNumber;

        public String getShippingCompany() { return shippingCompany; }
        public void setShippingCompany(String shippingCompany) { this.shippingCompany = shippingCompany; }
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    }

    public static class PageResponse<T> {
        private List<T> records;
        private long total;
        private int page;
        private int size;
        private int pages;

        public PageResponse() {
        }

        public PageResponse(List<T> records, long total, int page, int size) {
            this.records = records;
            this.total = total;
            this.page = page;
            this.size = size;
            this.pages = size <= 0 ? 0 : (int) Math.ceil((double) total / (double) size);
        }

        public List<T> getRecords() {
            return records;
        }

        public void setRecords(List<T> records) {
            this.records = records;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }
    }

    public static class StatusRequest {
        @NotBlank(message = "状态不能为空")
        @Size(max = 20, message = "状态长度不能超过 20 个字符")
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class StockRequest {
        @NotNull(message = "库存不能为空")
        @DecimalMin(value = "0", message = "库存必须为非负整数")
        @DecimalMax(value = "2147483647", message = "库存必须为非负整数")
        @Digits(integer = 10, fraction = 0, message = "库存必须为非负整数")
        private BigDecimal stock;

        public BigDecimal getStock() {
            return stock;
        }

        public void setStock(BigDecimal stock) {
            this.stock = stock;
        }
    }

    public static class StatisticsResponse {
        private long productTotal;
        private long lowStockProductCount;
        private long orderCount;
        private long pendingOrderCount;

        public StatisticsResponse() {
        }

        public StatisticsResponse(long productTotal, long lowStockProductCount, long orderCount, long pendingOrderCount) {
            this.productTotal = productTotal;
            this.lowStockProductCount = lowStockProductCount;
            this.orderCount = orderCount;
            this.pendingOrderCount = pendingOrderCount;
        }

        public long getProductTotal() {
            return productTotal;
        }

        public void setProductTotal(long productTotal) {
            this.productTotal = productTotal;
        }

        public long getLowStockProductCount() {
            return lowStockProductCount;
        }

        public void setLowStockProductCount(long lowStockProductCount) {
            this.lowStockProductCount = lowStockProductCount;
        }

        public long getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(long orderCount) {
            this.orderCount = orderCount;
        }

        public long getPendingOrderCount() {
            return pendingOrderCount;
        }

        public void setPendingOrderCount(long pendingOrderCount) {
            this.pendingOrderCount = pendingOrderCount;
        }
    }
}
