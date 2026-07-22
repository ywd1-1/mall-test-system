package com.example.malltestsystem.service;

import com.example.malltestsystem.dto.ApiDtos;
import com.example.malltestsystem.entity.User;
import com.example.malltestsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registrationStoresBcryptPassword() {
        ApiDtos.RegisterRequest request = request(new ApiDtos.RegisterRequest(), "new_user", "secret123");
        when(userRepository.existsByUsername("new_user")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(10L);
            return savedUser;
        });

        userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        String storedPassword = userCaptor.getValue().getPassword();
        assertNotEquals("secret123", storedPassword);
        assertTrue(passwordEncoder.matches("secret123", storedPassword));
    }

    @Test
    void loginAcceptsBcryptPasswordWithoutRewritingIt() {
        User user = activeUser("$2b$10$ypU/7h/lM.8jNyziy8xTeOlo5r3K8flGI3TOvAigEPn6oobF9oZmy");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        userService.login(request(new ApiDtos.LoginRequest(), "user", "123456"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUpgradesLegacyPlaintextPassword() {
        User user = activeUser("123456");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.login(request(new ApiDtos.LoginRequest(), "user", "123456"));

        verify(userRepository).save(user);
        assertNotEquals("123456", user.getPassword());
        assertTrue(passwordEncoder.matches("123456", user.getPassword()));
    }

    private User activeUser(String password) {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setPassword(password);
        user.setRole(User.ROLE_USER);
        user.setStatus(User.STATUS_ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private <T extends ApiDtos.LoginRequest> T request(T request, String username, String password) {
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
