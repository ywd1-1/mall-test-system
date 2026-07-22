package com.example.malltestsystem.controller;

import com.example.malltestsystem.common.ApiResponse;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "管理员用户")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<ApiDtos.PageResponse<ApiDtos.UserResponse>> listUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.listNormalUsers(username, status, page, size));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<ApiDtos.UserResponse> updateStatus(@PathVariable Long id,
                                                          @Valid @RequestBody ApiDtos.StatusRequest request) {
        return ApiResponse.success(userService.updateNormalUserStatus(id, request.getStatus()));
    }
}
