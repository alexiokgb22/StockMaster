package com.backend.module.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KPIs globaux du système.
 * Réservé à l'Administrateur.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalDashboardResponse {

    // ── Référentiel ────────────────────────────────────────────────
    private long activeWarehouses;
    private long totalWarehouses;
    private long activeProducts;
    private long activeSuppliers;
    private long activeUsers;

    // ── Stocks ─────────────────────────────────────────────────────
    /** Total de lignes de stock en dessous du seuil sur tout le système. */
    private long globalBelowMinCount;

    /** Total de ruptures de stock sur tout le système. */
    private long globalStockOutCount;

    // ── Alertes ────────────────────────────────────────────────────
    private long totalUnreadAlerts;
    private long totalCriticalAlerts;

    // ── Flux globaux en attente ────────────────────────────────────
    private long pendingPurchaseOrders;
    private long pendingTransfers;
}
