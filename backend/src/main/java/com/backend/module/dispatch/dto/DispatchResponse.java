package com.backend.module.dispatch.dto;

import com.backend.module.shared.enums.DispatchStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DispatchResponse {

    private Long id;
    private String dispatchNumber;
    private DispatchStatus status;
    private String note;
    private String rejectionReason;
    private LocalDateTime validatedAt;
    private String clientFirstName;
    private String clientLastName;
    private String clientPhone;
    private String deliveryAddress;

    // Entrepôt
    private Long warehouseId;
    private String warehouseName;

    // Acteurs
    private String createdByUsername;
    private String validatedByUsername;

    // Lignes
    private List<DispatchLineResponse> lines;

    // Méta
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class DispatchLineResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productReference;
        private String categoryName;
        private Integer quantityRequested;
        private String note;
        private Long zoneId;
        private String zoneName;
        // Stock disponible au moment de la consultation (informatif)
        private Integer stockAvailable;
    }
}
