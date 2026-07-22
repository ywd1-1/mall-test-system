package com.example.malltestsystem.controller;

import com.example.malltestsystem.common.ApiResponse;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.service.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "购物车")
@SecurityRequirement(name = "bearerAuth")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ApiResponse<ApiDtos.CartItemResponse> addToCart(@Valid @RequestBody ApiDtos.AddCartRequest request) {
        return ApiResponse.success(cartService.addToCart(request));
    }

    @GetMapping
    public ApiResponse<List<ApiDtos.CartItemResponse>> listCart() {
        return ApiResponse.success(cartService.listCart());
    }

    @PutMapping("/{id}")
    public ApiResponse<ApiDtos.CartItemResponse> updateCart(@PathVariable Long id,
                                                            @Valid @RequestBody ApiDtos.UpdateCartRequest request) {
        return ApiResponse.success(cartService.updateCart(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCart(@PathVariable Long id) {
        cartService.deleteCart(id);
        return ApiResponse.success();
    }
}
