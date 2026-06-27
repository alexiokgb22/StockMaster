package com.backend.module.inventory.dto;

import com.backend.module.shared.enums.InventoryStatus;
import com.backend.module.shared.enums.InventoryType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InventoryResponse {

    private Long id;
    private String inventoryNumber;
    private InventoryType inventoryType;
    private InventoryStatus inventoryStatus;
    private String note;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Entrepôt
    private Long warehouseId;
    private String warehouseName;

    // Acteur
    private String createdByUsername;

    // Lignes
    private List<InventoryLineResponse> lines;

    // Stats rapides
    private int totalLines;
    private int countedLines;    // lignes avec actualQty renseignée
    private int gapLines;        // lignes avec écart ≠ 0
    private int totalGap;        // somme absolue des écarts

    // Méta
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class InventoryLineResponse {
        private Long id;
        private Long stockId;
        private Long productId;
        private String productName;
        private String productReference;
        private String categoryName;
        private Long zoneId;
        private String zoneName;
        private Integer theoreticalQty;
        private Integer actualQty;      // null si non encore saisie
        private Integer gap;            // null si non encore saisie
        private boolean counted;
        private String note;
    }
}
