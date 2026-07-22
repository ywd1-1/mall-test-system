package com.example.malltestsystem.controller;

import com.example.malltestsystem.common.ApiResponse;
import com.example.malltestsystem.common.BusinessException;
import com.example.malltestsystem.common.UserContext;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.User;
import com.example.malltestsystem.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "管理员商品")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<ApiDtos.PageResponse<ApiDtos.ProductResponse>> listProductsForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size) {
        requireAdmin();
        return ApiResponse.success(productService.listProductsForAdmin(keyword, category, status, page, size));
    }

    @PostMapping
    public ApiResponse<ApiDtos.ProductResponse> createProduct(@Valid @RequestBody ApiDtos.ProductRequest request) {
        requireAdmin();
        return ApiResponse.success(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ApiDtos.ProductResponse> updateProduct(@PathVariable Long id,
                                                              @Valid @RequestBody ApiDtos.ProductRequest request) {
        requireAdmin();
        return ApiResponse.success(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        requireAdmin();
        productService.deleteProduct(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<ApiDtos.ProductResponse> updateStatus(@PathVariable Long id,
                                                             @Valid @RequestBody ApiDtos.StatusRequest request) {
        requireAdmin();
        return ApiResponse.success(productService.updateProductStatus(id, request.getStatus()));
    }

    @PutMapping("/{id}/stock")
    public ApiResponse<ApiDtos.ProductResponse> updateStock(@PathVariable Long id,
                                                            @Valid @RequestBody ApiDtos.StockRequest request) {
        requireAdmin();
        return ApiResponse.success(productService.updateProductStock(id, request.getStock()));
    }

    private void requireAdmin() {
        User user = UserContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("未登录");
        }
        if (!User.ROLE_ADMIN.equals(user.getRole())) {
            throw BusinessException.forbidden("只有管理员可以访问该接口");
        }
    }
}
