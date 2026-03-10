package com.marketplace.productservice.repository;

import com.marketplace.productservice.entity.Product;
import com.marketplace.productservice.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByCategory(Category category);

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByActiveTrue();

    Page<Product> findByActiveTrue(Pageable pageable);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
