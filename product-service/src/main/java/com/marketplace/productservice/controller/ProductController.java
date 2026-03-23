package com.marketplace.productservice.controller;

import com.marketplace.productservice.dto.ProductRequest;
import com.marketplace.productservice.dto.ProductResponse;
import com.marketplace.productservice.enums.Category;
import com.marketplace.productservice.service.FileStorageService;
import com.marketplace.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse productResponse = productService.createProduct(request, 1L);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        ProductResponse productResponse = productService.getProductById(id);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllActiveProducts() {
        List<ProductResponse> productResponse = productService.getAllActiveProducts();
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ProductResponse>> getAllActiveProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<ProductResponse> products = productService.getAllActiveProductsPaginated(pageRequest);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Category category) {
        List<ProductResponse> productResponse = productService.getProductsByCategory(category);
        return ResponseEntity.ok(productResponse);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> reduceStock(
            @PathVariable String id,
            @RequestParam int quantity) {
        ProductResponse productResponse = productService.reduceStock(id, quantity);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        List<ProductResponse> productResponse = productService.searchProducts(keyword);
        return ResponseEntity.ok(productResponse);
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<ProductResponse> uploadProductImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        String imageUrl = fileStorageService.uploadFile(file);
        ProductResponse response = productService.updateImageUrl(id, imageUrl);
        return ResponseEntity.ok(response);
    }

}
