package com.backend.module.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {

    private Long id;

    private Integer quantityAvailable;
    private Integer quantityReserved;
    private Integer quantityInTransit;
    private Integer minStock;
    private Integer maxStock;

    // Indicateur d'alerte seuil bas
    private Boolean isBelowMin;

    private Long productId;
    private String productName;
    private String productReference;
    private String productBarcode;
    private Double productVolume;

    private Long categoryId;
    private String categoryName;

    private Long warehouseId;
    private String warehouseName;

    private Long zoneId;
    private String zoneName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
