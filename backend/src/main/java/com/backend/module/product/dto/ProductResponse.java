package com.backend.module.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String reference;
    private String barcode;
    private String name;
    private String description;
    private Double purchasePrice;
    private Double salePrice;
    private Double weight;
    private Double volume;
    private Boolean isActive;

    private Long categoryId;
    private String categoryName;

    private String createdByUsername;
    private Boolean isAdminDefined;

    // IDs des entrepôts dans lesquels ce produit est disponible
    private Set<Long> warehouseIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
