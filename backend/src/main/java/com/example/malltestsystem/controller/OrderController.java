package com.example.malltestsystem.controller;

import com.example.malltestsystem.common.ApiResponse;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "用户订单")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<ApiDtos.OrderResponse> createOrder(@Valid @RequestBody ApiDtos.CreateOrderRequest request) {
        return ApiResponse.success(orderService.createOrder(request));
    }

    @GetMapping
    public ApiResponse<ApiDtos.PageResponse<ApiDtos.OrderResponse>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.listCurrentUserOrders(status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ApiDtos.OrderResponse> getOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.getCurrentUserOrder(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelCurrentUserOrder(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<ApiDtos.OrderResponse> payOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.payCurrentUserOrder(id));
    }

    @PostMapping("/{id}/confirm-receipt")
    public ApiResponse<ApiDtos.OrderResponse> confirmReceipt(@PathVariable Long id) {
        return ApiResponse.success(orderService.confirmReceipt(id));
    }
}
