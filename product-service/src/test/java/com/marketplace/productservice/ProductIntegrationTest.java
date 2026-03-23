package com.marketplace.productservice;

import com.marketplace.productservice.dto.ProductRequest;
import com.marketplace.productservice.dto.ProductResponse;
import com.marketplace.productservice.enums.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class ProductIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Test
    void shouldCreateProduct() {
        ProductRequest request = ProductRequest.builder()
                .name("iPhone 15")
                .description("Latest Apple smartphone")
                .price(BigDecimal.valueOf(999.99))
                .category(Category.ELECTRONICS)
                .stock(50)
                .build();

        ResponseEntity<ProductResponse> response = restTemplate.postForEntity(
                "/api/v1/products", request, ProductResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("iPhone 15", response.getBody().getName());
        assertEquals(BigDecimal.valueOf(999.99), response.getBody().getPrice());
        assertEquals(Category.ELECTRONICS, response.getBody().getCategory());
        assertEquals(50, response.getBody().getStock());
        assertTrue(response.getBody().getActive());
    }

    @Test
    void shouldGetProductById() {
        ProductRequest request = ProductRequest.builder()
                .name("Samsung Galaxy S24")
                .description("Android flagship")
                .price(BigDecimal.valueOf(899.99))
                .category(Category.ELECTRONICS)
                .stock(30)
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products", request, ProductResponse.class
        );
        String productId = createResponse.getBody().getId();

        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                "/api/v1/products/" + productId, ProductResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Samsung Galaxy S24", response.getBody().getName());
        assertEquals(productId, response.getBody().getId());
    }

    @Test
    void shouldGetAllActiveProducts() {
        ProductRequest request = ProductRequest.builder()
                .name("MacBook Pro")
                .description("Apple laptop")
                .price(BigDecimal.valueOf(2499.99))
                .category(Category.ELECTRONICS)
                .stock(10)
                .build();

        restTemplate.postForEntity("/api/v1/products", request, ProductResponse.class);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/products", String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("MacBook Pro"));
    }

    @Test
    void shouldReduceStock() {
        ProductRequest request = ProductRequest.builder()
                .name("AirPods Pro")
                .description("Wireless earbuds")
                .price(BigDecimal.valueOf(249.99))
                .category(Category.ELECTRONICS)
                .stock(20)
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products", request, ProductResponse.class
        );
        String productId = createResponse.getBody().getId();

        ResponseEntity<ProductResponse> reduceResponse = restTemplate.exchange(
                "/api/v1/products/" + productId + "/stock?quantity=5",
                HttpMethod.PUT, null, ProductResponse.class
        );

        assertEquals(HttpStatus.OK, reduceResponse.getStatusCode());
        assertEquals(15, reduceResponse.getBody().getStock());
    }

    @Test
    void shouldSearchProducts() {
        ProductRequest request = ProductRequest.builder()
                .name("Sony PlayStation 5")
                .description("Gaming console")
                .price(BigDecimal.valueOf(499.99))
                .category(Category.ELECTRONICS)
                .stock(15)
                .build();

        restTemplate.postForEntity("/api/v1/products", request, ProductResponse.class);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/products/search?keyword=PlayStation", String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Sony PlayStation 5"));
    }

    @Test
    void shouldReturn404ForNonExistentProduct() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/products/nonexistent123", String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
