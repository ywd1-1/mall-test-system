package com.example.malltestsystem.controller;

import com.example.malltestsystem.common.ApiResponse;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "认证")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<ApiDtos.TokenResponse> login(@Valid @RequestBody ApiDtos.LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<ApiDtos.TokenResponse> register(@Valid @RequestBody ApiDtos.RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }
}
