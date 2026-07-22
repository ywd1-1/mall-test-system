package com.example.malltestsystem.controller;

import com.example.malltestsystem.common.ApiResponse;
import com.example.malltestsystem.common.BusinessException;
import com.example.malltestsystem.common.UserContext;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.User;
import com.example.malltestsystem.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "当前用户")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/current")
    public ApiResponse<ApiDtos.UserResponse> currentUser() {
        User user = UserContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("未登录");
        }
        return ApiResponse.success(userService.toUserResponse(user));
    }
}
