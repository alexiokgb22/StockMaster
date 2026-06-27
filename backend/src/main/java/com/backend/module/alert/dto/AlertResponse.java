package com.backend.module.alert.dto;

import com.backend.module.shared.enums.AlertSeverity;
import com.backend.module.shared.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Long id;
    private AlertType type;
    private AlertSeverity severity;
    private String message;

    // Références contextuelles
    private Long stockId;
    private Long productId;
    private String productName;
    private Long zoneId;
    private String zoneName;
    private Long warehouseId;
    private String warehouseName;

    private Boolean isRead;
    private Boolean isResolved;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
