package com.marketplace.productservice.dto;

import com.marketplace.productservice.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private Long sellerId;
    private String imageUrl;
    private Integer stock;
    private Boolean active;
    private Map<String, String> attributes;
    private LocalDateTime createdAt;
}
