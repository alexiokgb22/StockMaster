package com.backend.module.dashboard.service;

import com.backend.exception.ResourceNotFoundException;
import com.backend.module.alert.repository.AlertRepository;
import com.backend.module.dashboard.dto.*;
import com.backend.module.product.repository.ProductRepository;
import com.backend.module.purchaseorder.repository.PurchaseOrderRepository;
import com.backend.module.shared.enums.*;
import com.backend.module.stock.repository.StockRepository;
import com.backend.module.stockmovement.repository.StockMovementRepository;
import com.backend.module.supplier.repository.SupplierRepository;
import com.backend.module.transfer.repository.TransferRepository;
import com.backend.module.user.repository.UserRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final TransferRepository transferRepository;
    private final AlertRepository alertRepository;

    // ─────────────────────────────────────────────────────────────
    // DASHBOARD GLOBAL (Admin)
    // ─────────────────────────────────────────────────────────────

    public GlobalDashboardResponse getGlobalDashboard() {
        // Entrepôts
        long totalWarehouses  = warehouseRepository.count();
        long activeWarehouses = warehouseRepository.countByIsActive(true);

        // Référentiel
        long activeProducts  = productRepository.countByIsActive(true);
        long activeSuppliers = supplierRepository.countByIsActive(true);
        long activeUsers     = userRepository.countByIsActive(true);

        // Stocks
        long belowMinCount  = stockRepository.countBelowMinGlobal();
        long stockOutCount  = stockRepository.countStockOutGlobal();

        // Alertes
        long unreadAlerts   = alertRepository.countByIsReadFalseAndIsResolvedFalse();
        long criticalAlerts = alertRepository.countBySeverityAndIsResolvedFalse(AlertSeverity.CRITICAL);

        // Flux en attente
        long pendingPO      = purchaseOrderRepository.countByStatus(PurchaseOrderStatus.VALIDATED);
        long pendingTrans   = transferRepository.countByStatus(TransferStatus.VALIDATED);

        return GlobalDashboardResponse.builder()
                .totalWarehouses(totalWarehouses)
                .activeWarehouses(activeWarehouses)
                .activeProducts(activeProducts)
                .activeSuppliers(activeSuppliers)
                .activeUsers(activeUsers)
                .globalBelowMinCount(belowMinCount)
                .globalStockOutCount(stockOutCount)
                .totalUnreadAlerts(unreadAlerts)
                .totalCriticalAlerts(criticalAlerts)
                .pendingPurchaseOrders(pendingPO)
                .pendingTransfers(pendingTrans)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // DASHBOARD PAR ENTREPÔT (Gestionnaire / Admin)
    // ─────────────────────────────────────────────────────────────

    public WarehouseDashboardResponse getWarehouseDashboard(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + warehouseId));

        long totalStockLines = stockRepository.countByWarehouseId(warehouseId);
        long stockOutCount   = stockRepository.countStockOutByWarehouse(warehouseId);
        long belowMinCount   = stockRepository.countBelowMinByWarehouse(warehouseId);

        // Capacité
        Double totalCap = warehouse.getTotalCapacity();
        Double usedCap  = warehouse.getUsedCapacity();
        Double capPct   = (totalCap != null && totalCap > 0 && usedCap != null)
                ? Math.min(100.0, (usedCap / totalCap) * 100.0)
                : null;

        // Flux en attente
        long pendingPO     = purchaseOrderRepository.countByWarehouseIdAndStatus(warehouseId, PurchaseOrderStatus.VALIDATED);
        long incomingTrans = transferRepository.countByTargetWarehouseIdAndStatus(warehouseId, TransferStatus.VALIDATED);

        // Alertes
        long activeAlerts   = alertRepository.countUnread(warehouseId);
        long criticalAlerts = alertRepository.countBySeverityAndWarehouseIdAndIsResolvedFalse(
                AlertSeverity.CRITICAL, warehouseId);

        return WarehouseDashboardResponse.builder()
                .warehouseId(warehouse.getId())
                .warehouseName(warehouse.getName())
                .totalStockLines(totalStockLines)
                .stockOutCount(stockOutCount)
                .belowMinCount(belowMinCount)
                .totalCapacity(totalCap)
                .usedCapacity(usedCap)
                .capacityPercent(capPct)
                .pendingPurchaseOrders(pendingPO)
                .incomingTransfers(incomingTrans)
                .activeAlerts(activeAlerts)
                .criticalAlerts(criticalAlerts)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // GRAPHIQUE — Entrées / Sorties par jour (30 derniers jours)
    // ─────────────────────────────────────────────────────────────

    public List<MovementChartPoint> getMovementsChart(Long warehouseId, int days) {
        LocalDateTime from = LocalDate.now().minusDays(days).atStartOfDay();
        List<Object[]> raw = movementRepository.countMovementsByDay(warehouseId, from);

        // Construire une map date → données
        Map<String, long[]> map = new java.util.LinkedHashMap<>();

        // Pré-remplir tous les jours de la période (pour afficher même les jours à 0)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = days; i >= 0; i--) {
            map.put(LocalDate.now().minusDays(i).format(fmt), new long[]{0, 0, 0});
        }

        for (Object[] row : raw) {
            String day     = (String) row[0];
            String type    = (String) row[1];
            long   qty     = ((Number) row[2]).longValue();

            long[] vals = map.getOrDefault(day, new long[]{0, 0, 0});
            if ("ENTRY".equals(type))           vals[0] += qty;
            else if ("EXIT".equals(type))       vals[1] += qty;
            else if ("TRANSFER".equals(type))   vals[2] += qty;
            map.put(day, vals);
        }

        List<MovementChartPoint> result = new ArrayList<>();
        map.forEach((label, vals) ->
            result.add(MovementChartPoint.builder()
                .label(label)
                .entries(vals[0])
                .exits(vals[1])
                .transfers(vals[2])
                .build())
        );
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // GRAPHIQUE — Stock par catégorie
    // ─────────────────────────────────────────────────────────────

    public List<CategoryStockPoint> getStockByCategory(Long warehouseId) {
        List<Object[]> raw = stockRepository.countStockByCategory(warehouseId);
        List<CategoryStockPoint> result = new ArrayList<>();
        for (Object[] row : raw) {
            result.add(CategoryStockPoint.builder()
                .categoryId(((Number) row[0]).longValue())
                .categoryName((String) row[1])
                .stockLineCount(((Number) row[2]).longValue())
                .totalQuantity(((Number) row[3]).longValue())
                .build());
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // GRAPHIQUE — Top 5 produits les plus mouvementés
    // ─────────────────────────────────────────────────────────────

    public List<TopProductPoint> getTopProducts(Long warehouseId, int days, int limit) {
        LocalDateTime from = LocalDate.now().minusDays(days).atStartOfDay();
        List<Object[]> raw = movementRepository.findTopProductsByMovements(
                warehouseId, from,
                org.springframework.data.domain.PageRequest.of(0, limit));
        List<TopProductPoint> result = new ArrayList<>();
        for (Object[] row : raw) {
            result.add(TopProductPoint.builder()
                .productId(((Number) row[0]).longValue())
                .productName((String) row[1])
                .productReference((String) row[2])
                .categoryName((String) row[3])
                .movementCount(((Number) row[4]).longValue())
                .totalQuantity(((Number) row[5]).longValue())
                .build());
        }
        return result;
    }
}
