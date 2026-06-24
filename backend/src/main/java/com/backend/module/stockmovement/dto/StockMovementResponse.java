package com.backend.module.stockmovement.dto;

import com.backend.module.shared.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {

    private Long id;
    private Integer quantity;
    private MovementType movementType;
    private String referenceDoc;
    private String note;

    private Long productId;
    private String productName;

    private Long warehouseId;
    private String warehouseName;

    private Long zoneId;
    private String zoneName;

    private String createdByUsername;
    private LocalDateTime createdAt;
}
