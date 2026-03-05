package com.marketplace.productservice.service;

import com.marketplace.productservice.dto.ProductRequest;
import com.marketplace.productservice.dto.ProductResponse;
import com.marketplace.productservice.entity.Product;
import com.marketplace.productservice.enums.Category;
import com.marketplace.productservice.exception.ResourceNotFoundException;
import com.marketplace.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        ProductRequest request = ProductRequest.builder()
                .name("iPhone15")
                .description("Latest smartphone")
                .price(BigDecimal.valueOf(100))
                .category(Category.ELECTRONICS)
                .imageUrl("pic")
                .stock(1)
                .build();

        when(productRepository.save(any(Product.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        ProductResponse result = productService.createProduct(request, 1L);

        assertNotNull(result);
        assertEquals("iPhone15", result.getName());
        assertEquals("Latest smartphone", result.getDescription());
        assertEquals(BigDecimal.valueOf(100), result.getPrice());
        assertEquals(Category.ELECTRONICS, result.getCategory());
        assertEquals("pic", result.getImageUrl());
        assertEquals(1, result.getStock());
        assertEquals(1L, result.getSellerId());
        assertEquals(true, result.getActive());
    }

    @Test
    void shouldGetProductById() {
        Product product = Product.builder()
                .id("abc123")
                .name("iPhone15")
                .price(BigDecimal.valueOf(100))
                .category(Category.ELECTRONICS)
                .build();

        when(productRepository.findById("abc123")).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProductById("abc123");

        assertNotNull(result);
        assertEquals("abc123", result.getId());
        assertEquals("iPhone15", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById("abc123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.getProductById("abc123"));
    }
}