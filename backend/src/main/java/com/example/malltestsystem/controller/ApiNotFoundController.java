package com.example.malltestsystem.controller;

import com.example.malltestsystem.common.BusinessException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
public class ApiNotFoundController {
    @RequestMapping("/api/**")
    public void notFound() {
        throw BusinessException.notFound("接口不存在");
    }
}
