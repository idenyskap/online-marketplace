package com.marketplace.productservice.entity;

import com.marketplace.productservice.enums.Category;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    private String name;

    private String description;

    private BigDecimal price;

    private Category category;

    @Indexed
    private Long sellerId;

    private String imageUrl;

    private Integer stock;

    private Boolean active;

    @Version
    private Long version;

    private Map<String, String> attributes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
