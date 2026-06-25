package com.backend.module.reception.dto;

import com.backend.module.shared.enums.ReceptionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReceptionResponse {

    private Long id;
    private String receptionNumber;
    private ReceptionStatus status;
    private String note;
    private String rejectionReason;
    private LocalDateTime validatedAt;

    // Commande associée
    private Long purchaseOrderId;
    private String purchaseOrderNumber;

    // Fournisseur (peut être null si commande non encore validée — cas impossible ici
    // car on ne réceptionne que des commandes DELIVERED, donc fournisseur toujours présent)
    private Long supplierId;
    private String supplierName;

    // Entrepôt
    private Long warehouseId;
    private String warehouseName;

    // Acteurs
    private String createdByUsername;
    private String validatedByUsername;  // null si pas encore validé

    // Lignes
    private List<ReceptionLineResponse> lines;

    // Méta
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Statistiques rapides ──────────────────────────────────────
    // Nombre de lignes avec écart (reçu ≠ attendu)
    private int gapCount;

    @Data
    @Builder
    public static class ReceptionLineResponse {
        private Long id;
        private Long purchaseOrderLineId;
        private Long productId;
        private String productName;
        private String productReference;
        private String categoryName;
        private Integer quantityExpected;
        private Integer quantityReceived;
        private int gap;             // reçu - attendu (négatif = manquant)
        private String note;
        private Long zoneId;
        private String zoneName;
    }
}
