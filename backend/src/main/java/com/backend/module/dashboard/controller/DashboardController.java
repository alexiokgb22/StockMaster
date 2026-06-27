package com.backend.module.dashboard.controller;

import com.backend.module.dashboard.dto.*;
import com.backend.module.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // ── GET /api/dashboard/summary ─────────────────────────────────
    // KPIs globaux — Admin uniquement
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<GlobalDashboardResponse> getGlobalSummary() {
        return ResponseEntity.ok(dashboardService.getGlobalDashboard());
    }

    // ── GET /api/dashboard/warehouse/{id} ──────────────────────────
    // KPIs d'un entrepôt — Gestionnaire (son entrepôt) + Admin
    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<WarehouseDashboardResponse> getWarehouseSummary(
            @PathVariable Long warehouseId
    ) {
        return ResponseEntity.ok(dashboardService.getWarehouseDashboard(warehouseId));
    }

    // ── GET /api/dashboard/movements-chart ────────────────────────
    // Données graphique entrées/sorties (série temporelle)
    // Paramètres : warehouseId (optionnel), days (défaut 30)
    @GetMapping("/movements-chart")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<List<MovementChartPoint>> getMovementsChart(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "30") int days
    ) {
        // Limiter à 90 jours max pour éviter les requêtes trop lourdes
        int safeDays = Math.min(days, 90);
        return ResponseEntity.ok(dashboardService.getMovementsChart(warehouseId, safeDays));
    }

    // ── GET /api/dashboard/stock-by-category ──────────────────────
    // Répartition du stock par catégorie (camembert)
    @GetMapping("/stock-by-category")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<List<CategoryStockPoint>> getStockByCategory(
            @RequestParam(required = false) Long warehouseId
    ) {
        return ResponseEntity.ok(dashboardService.getStockByCategory(warehouseId));
    }

    // ── GET /api/dashboard/top-products ───────────────────────────
    // Top N produits les plus mouvementés
    // Paramètres : warehouseId (optionnel), days (défaut 30), limit (défaut 5)
    @GetMapping("/top-products")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<List<TopProductPoint>> getTopProducts(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "5")  int limit
    ) {
        int safeLimit = Math.min(limit, 20);
        return ResponseEntity.ok(dashboardService.getTopProducts(warehouseId, days, safeLimit));
    }
}
