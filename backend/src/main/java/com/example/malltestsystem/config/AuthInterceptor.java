package com.example.malltestsystem.config;

import com.example.malltestsystem.common.BusinessException;
import com.example.malltestsystem.common.UserContext;
import com.example.malltestsystem.entity.User;
import com.example.malltestsystem.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublicApi(request)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw BusinessException.unauthorized("未登录或 token 缺失");
        }

        String token = authorization.substring("Bearer ".length()).trim();
        User user = userService.findByToken(token);
        if (user == null) {
            throw BusinessException.unauthorized("登录状态已失效，请重新登录");
        }
        if (request.getRequestURI().startsWith("/api/admin/") && !User.ROLE_ADMIN.equals(user.getRole())) {
            throw BusinessException.forbidden("普通用户不能访问管理员接口");
        }
        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isPublicApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if ("/api/login".equals(uri) || "/api/register".equals(uri)) {
            return true;
        }
        return "GET".equalsIgnoreCase(method) && ("/api/products".equals(uri) || uri.matches("/api/products/\\d+"));
    }
}
