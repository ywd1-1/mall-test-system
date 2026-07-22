package com.example.malltestsystem.service;

import com.example.malltestsystem.common.BusinessException;
import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.User;
import com.example.malltestsystem.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<String, Long>();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ApiDtos.TokenResponse register(ApiDtos.RegisterRequest request) {
        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw BusinessException.badRequest("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.ROLE_USER);
        user.setStatus(User.STATUS_ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return issueToken(user);
    }

    @Transactional
    public ApiDtos.TokenResponse login(ApiDtos.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> BusinessException.badRequest("用户名或密码错误"));
        if (!User.STATUS_ACTIVE.equals(user.getStatus())) {
            throw BusinessException.forbidden("账号已被禁用");
        }
        String storedPassword = user.getPassword();
        boolean bcryptPassword = isBcrypt(storedPassword);
        boolean passwordMatches = bcryptPassword
                ? passwordEncoder.matches(request.getPassword(), storedPassword)
                : storedPassword.equals(request.getPassword());
        if (!passwordMatches) {
            throw BusinessException.badRequest("用户名或密码错误");
        }
        if (!bcryptPassword) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public User findByToken(String token) {
        Long userId = tokenStore.get(token);
        if (userId == null) {
            return null;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !User.STATUS_ACTIVE.equals(user.getStatus())) {
            tokenStore.remove(token);
            return null;
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> BusinessException.notFound("用户不存在"));
    }

    public ApiDtos.UserResponse toUserResponse(User user) {
        ApiDtos.UserResponse response = new ApiDtos.UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    @Transactional(readOnly = true)
    public ApiDtos.PageResponse<ApiDtos.UserResponse> listNormalUsers(String username,
                                                                      String status,
                                                                      int page,
                                                                      int size) {
        requireAdmin();
        String normalizedStatus = normalizeUserStatus(status, true);
        Specification<User> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(root.get("role"), User.ROLE_USER));
            if (username != null && !username.trim().isEmpty()) {
                predicates.add(builder.like(
                        builder.lower(root.<String>get("username")),
                        "%" + username.trim().toLowerCase() + "%"));
            }
            if (normalizedStatus != null) {
                predicates.add(builder.equal(root.get("status"), normalizedStatus));
            }
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        Page<User> users = userRepository.findAll(
                specification,
                PageRequest.of(validatePage(page) - 1, validateSize(size), Sort.by(Sort.Direction.DESC, "id")));
        List<ApiDtos.UserResponse> records = new ArrayList<ApiDtos.UserResponse>();
        for (User user : users.getContent()) {
            records.add(toUserResponse(user));
        }
        return new ApiDtos.PageResponse<ApiDtos.UserResponse>(
                records, users.getTotalElements(), users.getNumber() + 1, users.getSize());
    }

    @Transactional
    public ApiDtos.UserResponse updateNormalUserStatus(Long id, String status) {
        requireAdmin();
        String normalizedStatus = normalizeUserStatus(status, false);
        User target = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        if (!User.ROLE_USER.equals(target.getRole())) {
            throw BusinessException.forbidden("不能修改管理员账号状态");
        }
        if (normalizedStatus.equals(target.getStatus())) {
            throw BusinessException.badRequest(
                    User.STATUS_ACTIVE.equals(normalizedStatus) ? "用户已启用，不能重复启用" : "用户已禁用，不能重复禁用");
        }
        target.setStatus(normalizedStatus);
        target.setUpdatedAt(LocalDateTime.now());
        userRepository.save(target);
        if (User.STATUS_DISABLED.equals(normalizedStatus)) {
            tokenStore.entrySet().removeIf(entry -> target.getId().equals(entry.getValue()));
        }
        return toUserResponse(target);
    }

    private ApiDtos.TokenResponse issueToken(User user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, user.getId());
        return new ApiDtos.TokenResponse(token, toUserResponse(user));
    }

    private boolean isBcrypt(String password) {
        return password != null && password.matches("^\\$2[ayb]\\$\\d{2}\\$.{53}$");
    }

    private void requireAdmin() {
        User user = com.example.malltestsystem.common.UserContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("未登录");
        }
        if (!User.ROLE_ADMIN.equals(user.getRole())) {
            throw BusinessException.forbidden("只有管理员可以访问该接口");
        }
    }

    private String normalizeUserStatus(String status, boolean optional) {
        if (status == null || status.trim().isEmpty()) {
            if (optional) {
                return null;
            }
            throw BusinessException.badRequest("用户状态不能为空");
        }
        String normalized = status.trim().toUpperCase();
        if (!User.STATUS_ACTIVE.equals(normalized) && !User.STATUS_DISABLED.equals(normalized)) {
            throw BusinessException.badRequest("用户状态只能是 ACTIVE 或 DISABLED");
        }
        return normalized;
    }

    private int validatePage(int page) {
        if (page < 1) {
            throw BusinessException.badRequest("页码必须大于等于 1");
        }
        return page;
    }

    private int validateSize(int size) {
        if (size < 1 || size > 100) {
            throw BusinessException.badRequest("每页数量必须在 1 到 100 之间");
        }
        return size;
    }
}
