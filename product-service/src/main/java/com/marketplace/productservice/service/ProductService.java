package com.marketplace.productservice.service;

import com.marketplace.productservice.dto.ProductRequest;
import com.marketplace.productservice.dto.ProductResponse;
import com.marketplace.productservice.entity.Product;
import com.marketplace.productservice.enums.Category;
import com.marketplace.productservice.exception.ResourceNotFoundException;
import com.marketplace.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request, Long sellerId) {
        log.info("Creating product: {} by seller: {}", request.getName(), sellerId);

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .sellerId(sellerId)
                .imageUrl(request.getImageUrl())
                .stock(request.getStock())
                .active(true)
                .attributes(request.getAttributes())
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Cacheable(value = "products", key = "'allActive'")
    public List<ProductResponse> getAllActiveProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "products", key = "#category")
    public List<ProductResponse> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> getAllActiveProductsPaginated(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse reduceStock(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Not enough stock for product: " + product.getName()
                    + ". Available: " + product.getStock() + ", requested: " + quantity);
        }

        product.setStock(product.getStock() - quantity);
        Product updated = productRepository.save(product);

        log.info("Stock reduced for product {}: {} → {}", productId,
                product.getStock() + quantity, updated.getStock());

        return mapToResponse(updated);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .sellerId(product.getSellerId())
                .imageUrl(product.getImageUrl())
                .stock(product.getStock())
                .active(product.getActive())
                .attributes(product.getAttributes())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
