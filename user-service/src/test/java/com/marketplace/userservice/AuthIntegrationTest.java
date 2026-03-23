package com.marketplace.userservice;

import com.marketplace.userservice.dto.AuthResponse;
import com.marketplace.userservice.dto.LoginRequest;
import com.marketplace.userservice.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldRegisterSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, AuthResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    void shouldFailRegisterWithDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("duplicate@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        restTemplate.postForEntity("/api/v1/auth/register", request, AuthResponse.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, String.class
        );

        assertNotEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldLoginSuccessfully() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("login-test@example.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Doe")
                .build();

        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, AuthResponse.class);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("login-test@example.com")
                .password("password123")
                .build();

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, AuthResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertEquals("login-test@example.com", response.getBody().getEmail());
    }

    @Test
    void shouldFailLoginWithWrongPassword() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("wrong-pass@example.com")
                .password("password123")
                .firstName("Bob")
                .lastName("Smith")
                .build();

        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, AuthResponse.class);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("wrong-pass@example.com")
                .password("wrongPassword")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
