package com.example.malltestsystem.controller;

import com.example.malltestsystem.common.ApiResponse;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "管理员订单")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<ApiDtos.PageResponse<ApiDtos.OrderResponse>> listAllOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.listAllOrdersForAdmin(
                orderNo, username, status, startTime, endTime, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ApiDtos.OrderResponse> getOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrderForAdmin(id));
    }

    @PostMapping("/{id}/ship")
    public ApiResponse<ApiDtos.OrderResponse> shipOrder(@PathVariable Long id,
                                                        @Valid @RequestBody ApiDtos.ShippingRequest request) {
        return ApiResponse.success(orderService.shipOrder(id, request));
    }
}
