package com.backend.module.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KPIs opérationnels d'un entrepôt spécifique.
 * Destiné au gestionnaire (son entrepôt) et à l'admin (n'importe quel entrepôt).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDashboardResponse {

    private Long warehouseId;
    private String warehouseName;

    // ── Stock ──────────────────────────────────────────────────────
    /** Nombre total de lignes de stock dans cet entrepôt. */
    private long totalStockLines;

    /** Nombre de lignes en rupture totale (quantityAvailable == 0). */
    private long stockOutCount;

    /** Nombre de lignes sous le seuil minimum. */
    private long belowMinCount;

    // ── Capacité ───────────────────────────────────────────────────
    /** Capacité totale de l'entrepôt (en m³ ou unité choisie). */
    private Double totalCapacity;

    /** Capacité utilisée. */
    private Double usedCapacity;

    /** Taux d'occupation en % (0-100), null si totalCapacity non renseignée. */
    private Double capacityPercent;

    // ── Flux en attente ────────────────────────────────────────────
    /** Commandes fournisseurs en état VALIDATED (livrables à réceptionner). */
    private long pendingPurchaseOrders;

    /** Transferts entrants en transit (VALIDATED, pas encore reçus). */
    private long incomingTransfers;

    // ── Alertes actives ────────────────────────────────────────────
    /** Nombre d'alertes non résolues sur cet entrepôt. */
    private long activeAlerts;

    /** Nombre d'alertes critiques non résolues. */
    private long criticalAlerts;
}
