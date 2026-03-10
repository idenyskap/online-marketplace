package com.marketplace.orderservice.client;

import com.marketplace.orderservice.dto.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product-service.url}")
    private String productServerUrl;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductResponse getProduct(String productId) {

        String url = productServerUrl + "/api/v1/products/" + productId;

        ProductResponse product = restTemplate.getForObject(url, ProductResponse.class);

        log.info("Fetched product from Product Service: id={}, name={}, price={}, stock={}",
                product.getId(), product.getName(), product.getPrice(), product.getStock());

        return product;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "reduceStockFallback")
    public void reduceStock(String productId, int quantity) {
        String url = productServerUrl + "/api/v1/products/" + productId + "/stock?quantity=" + quantity;
        restTemplate.put(url, null);
        log.info("Stock reduced for product {}: quantity={}", productId, quantity);
    }

    private void reduceStockFallback(String productId, int quantity, Throwable throwable) {
        log.error("Failed to reduce stock for product {}. Error: {}", productId, throwable.getMessage());
    }

    private ProductResponse getProductFallback(String productId, Throwable throwable) {

        log.error("Product Service is unavailable. ProductId: {}, Error: {}", productId, throwable.getMessage());

        throw new RuntimeException("Product Service currently is not available");
    }
}
