package com.backend.module.activityreport.dto;

import com.backend.module.activityreport.dto.ActivityReportResponse.OperationSummary;
import com.backend.module.shared.enums.ActivityReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityReportResponse {

    private Long id;

    // ── Période ───────────────────────────────────────────────────
    private LocalDate reportDate;
    private LocalTime arrivalTime;
    private LocalTime departureTime;

    // ── Champs structurés ─────────────────────────────────────────
    private String incidents;
    private String observations;

    // ── Résumé opérationnel ───────────────────────────────────────
    private Integer receptionCount;
    private Integer dispatchCount;

    /** Liste des réceptions effectuées ce jour-là par ce magasinier. */
    private List<OperationSummary> receptions;

    /** Liste des sorties effectuées ce jour-là par ce magasinier. */
    private List<OperationSummary> dispatches;

    // ── Statut ────────────────────────────────────────────────────
    private ActivityReportStatus status;

    // ── Acteurs ───────────────────────────────────────────────────
    private Long storekeeperId;
    private String storekeeperUsername;
    private Long warehouseId;
    private String warehouseName;

    // ── Méta ──────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Sous-objets ───────────────────────────────────────────────

    /** Résumé d'une opération (réception ou sortie) listée dans le rapport. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationSummary {
        private Long id;
        private String number;       // receptionNumber ou dispatchNumber
        private String type;         // "RECEPTION" ou "DISPATCH"
        private int lineCount;       // nombre de produits
        private LocalDateTime doneAt;
    }
}
