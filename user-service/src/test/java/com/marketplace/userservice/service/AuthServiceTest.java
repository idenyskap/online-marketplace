package com.marketplace.userservice.service;

import com.marketplace.userservice.dto.AuthResponse;
import com.marketplace.userservice.dto.LoginRequest;
import com.marketplace.userservice.dto.RegisterRequest;
import com.marketplace.userservice.entity.User;
import com.marketplace.userservice.enums.Role;
import com.marketplace.userservice.exception.EmailAlreadyExistsException;
import com.marketplace.userservice.exception.ResourceNotFoundException;
import com.marketplace.userservice.repository.UserRepository;
import com.marketplace.userservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@gmail.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(
                invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token-123");

        AuthResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("jwt-token-123", result.getToken());
        assertEquals("BUYER", result.getRole());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@gmail.com")
                .build();

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () ->
                authService.register(request));
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = LoginRequest.builder()
                .email("test@gmail.com")
                .password("password123")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.BUYER)
                .build();

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token-123");

        AuthResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("jwt-token-123", result.getToken());
        assertEquals("John", result.getFirstName());
        assertEquals("BUYER", result.getRole());
    }

    @Test
    void shouldThrowExceptionWhenBadCredentials() {
        LoginRequest request = LoginRequest.builder()
                .email("test@gmail.com")
                .password("password123")
                .build();

        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () ->
                authService.login(request));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundOnLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("test@gmail.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                authService.login(request));
    }
}
