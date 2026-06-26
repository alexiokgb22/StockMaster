package com.backend.module.transfer.dto;

import com.backend.module.shared.enums.TransferStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TransferResponse {

    private Long id;
    private String transferNumber;
    private TransferStatus status;
    private String note;
    private String cancellationReason;
    private LocalDateTime validatedAt;
    private LocalDateTime receivedAt;

    // Entrepôt source
    private Long sourceWarehouseId;
    private String sourceWarehouseName;

    // Entrepôt cible
    private Long targetWarehouseId;
    private String targetWarehouseName;

    // Acteurs
    private String createdByUsername;
    private String validatedByUsername;
    private String receivedByUsername;

    // Lignes
    private List<TransferLineResponse> lines;

    // Méta
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class TransferLineResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productReference;
        private String categoryName;
        private Integer quantity;
        private String note;
        // Zone source
        private Long sourceZoneId;
        private String sourceZoneName;
        // Zone cible (null jusqu'à la réception)
        private Long targetZoneId;
        private String targetZoneName;
    }
}
