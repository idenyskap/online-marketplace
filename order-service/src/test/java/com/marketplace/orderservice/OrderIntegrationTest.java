package com.marketplace.orderservice;

import com.marketplace.orderservice.client.ProductClient;
import com.marketplace.orderservice.dto.*;
import com.marketplace.orderservice.enums.OrderStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class OrderIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.0");

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private ProductClient productClient;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    private String generateTestToken(Long userId, String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of("userId", userId, "role", role))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();
    }

    private HttpHeaders authHeaders(Long userId, String email, String role) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(generateTestToken(userId, email, role));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void shouldCreateOrder() {
        ProductResponse product = ProductResponse.builder()
                .id("prod1")
                .name("iPhone 15")
                .price(BigDecimal.valueOf(999.99))
                .stock(10)
                .build();

        when(productClient.getProduct("prod1")).thenReturn(product);

        OrderItemRequest item = OrderItemRequest.builder()
                .productId("prod1")
                .quantity(2)
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(List.of(item))
                .build();

        HttpEntity<OrderRequest> entity = new HttpEntity<>(request, authHeaders(1L, "buyer@test.com", "BUYER"));

        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/v1/orders", HttpMethod.POST, entity, OrderResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getBuyerId());
        assertEquals(OrderStatus.CREATED, response.getBody().getStatus());
        assertEquals(1, response.getBody().getItems().size());
        assertEquals("iPhone 15", response.getBody().getItems().get(0).getProductName());
    }

    @Test
    void shouldGetMyOrders() {
        ProductResponse product = ProductResponse.builder()
                .id("prod2")
                .name("Samsung TV")
                .price(BigDecimal.valueOf(1499.99))
                .stock(5)
                .build();

        when(productClient.getProduct("prod2")).thenReturn(product);

        OrderItemRequest item = OrderItemRequest.builder()
                .productId("prod2")
                .quantity(1)
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(List.of(item))
                .build();

        HttpHeaders headers = authHeaders(2L, "buyer2@test.com", "BUYER");
        HttpEntity<OrderRequest> createEntity = new HttpEntity<>(request, headers);
        restTemplate.exchange("/api/v1/orders", HttpMethod.POST, createEntity, OrderResponse.class);

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<OrderResponse[]> response = restTemplate.exchange(
                "/api/v1/orders", HttpMethod.GET, getEntity, OrderResponse[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().length > 0);
        assertEquals(2L, response.getBody()[0].getBuyerId());
    }

    @Test
    void shouldReturn403WithoutToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/orders", String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
