package com.backend.module.alert.service;

import com.backend.module.shared.enums.AlertSeverity;
import com.backend.module.shared.enums.AlertType;
import com.backend.module.stock.entity.Stock;
import com.backend.module.stock.repository.StockRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduler qui vérifie périodiquement les conditions d'alerte.
 * Tourne toutes les 5 minutes par défaut (configurable).
 *
 * Conditions vérifiées :
 *  - STOCK_OUT       : quantityAvailable == 0
 *  - STOCK_BELOW_MIN : quantityAvailable < minStock (et > 0)
 *  - ZONE_NEAR_CAPACITY       : volume utilisé dans la zone > 80% de zone.capacity
 *  - WAREHOUSE_NEAR_CAPACITY  : warehouse.usedCapacity > 85% de warehouse.totalCapacity
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private static final double ZONE_CAPACITY_THRESHOLD      = 0.80; // 80%
    private static final double WAREHOUSE_CAPACITY_THRESHOLD = 0.85; // 85%

    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final AlertService alertService;

    // ─────────────────────────────────────────────────────────────
    // CHECK STOCK — toutes les 5 minutes
    // ─────────────────────────────────────────────────────────────

    @Scheduled(fixedDelayString = "${alerts.stock-check-delay-ms:300000}")
    @Transactional
    public void checkStockAlerts() {
        log.debug("Scheduler : vérification des alertes stock...");

        List<Stock> allStocks = stockRepository.findAll();

        for (Stock stock : allStocks) {
            checkStockOut(stock);
            checkStockBelowMin(stock);
        }

        log.debug("Scheduler : vérification des alertes stock terminée ({} lignes)", allStocks.size());
    }

    // ─────────────────────────────────────────────────────────────
    // CHECK CAPACITÉ — toutes les 10 minutes
    // ─────────────────────────────────────────────────────────────

    @Scheduled(fixedDelayString = "${alerts.capacity-check-delay-ms:600000}")
    @Transactional
    public void checkCapacityAlerts() {
        log.debug("Scheduler : vérification des alertes capacité...");

        List<Warehouse> warehouses = warehouseRepository.findAll();

        for (Warehouse warehouse : warehouses) {
            if (Boolean.TRUE.equals(warehouse.getIsActive())) {
                checkWarehouseCapacity(warehouse);
                checkZoneCapacities(warehouse);
            }
        }

        log.debug("Scheduler : vérification des alertes capacité terminée");
    }

    // ─────────────────────────────────────────────────────────────
    // LOGIQUE DES VÉRIFICATIONS
    // ─────────────────────────────────────────────────────────────

    private void checkStockOut(Stock stock) {
        String dedupKey = buildDedupKey(AlertType.STOCK_OUT, "stock", stock.getId());

        if (stock.getQuantityAvailable() != null && stock.getQuantityAvailable() == 0) {
            alertService.createIfNotExists(
                AlertType.STOCK_OUT,
                AlertSeverity.CRITICAL,
                "Rupture de stock : \"" + stock.getProduct().getName()
                    + "\" (zone " + stock.getZone().getName() + ")",
                dedupKey,
                stock.getId(),
                stock.getProduct().getId(),
                stock.getProduct().getName(),
                stock.getZone().getId(),
                stock.getZone().getName(),
                stock.getWarehouse()
            );
        } else {
            // Condition disparue → résolution automatique
            alertService.autoResolveIfExists(dedupKey);
        }
    }

    private void checkStockBelowMin(Stock stock) {
        if (stock.getMinStock() == null) return;

        String dedupKey = buildDedupKey(AlertType.STOCK_BELOW_MIN, "stock", stock.getId());

        boolean belowMin = stock.getQuantityAvailable() != null
                && stock.getQuantityAvailable() > 0
                && stock.getQuantityAvailable() < stock.getMinStock();

        if (belowMin) {
            alertService.createIfNotExists(
                AlertType.STOCK_BELOW_MIN,
                AlertSeverity.WARNING,
                "Stock bas : \"" + stock.getProduct().getName()
                    + "\" — " + stock.getQuantityAvailable() + " unités"
                    + " (seuil min : " + stock.getMinStock() + ")"
                    + " — zone " + stock.getZone().getName(),
                dedupKey,
                stock.getId(),
                stock.getProduct().getId(),
                stock.getProduct().getName(),
                stock.getZone().getId(),
                stock.getZone().getName(),
                stock.getWarehouse()
            );
        } else {
            alertService.autoResolveIfExists(dedupKey);
        }
    }

    private void checkWarehouseCapacity(Warehouse warehouse) {
        if (warehouse.getTotalCapacity() == null || warehouse.getTotalCapacity() == 0) return;
        if (warehouse.getUsedCapacity() == null) return;

        String dedupKey = buildDedupKey(AlertType.WAREHOUSE_NEAR_CAPACITY, "warehouse", warehouse.getId());
        double ratio = warehouse.getUsedCapacity() / warehouse.getTotalCapacity();

        if (ratio >= WAREHOUSE_CAPACITY_THRESHOLD) {
            int percent = (int) (ratio * 100);
            alertService.createIfNotExists(
                AlertType.WAREHOUSE_NEAR_CAPACITY,
                percent >= 95 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING,
                "Entrepôt \"" + warehouse.getName() + "\" à " + percent + "% de sa capacité",
                dedupKey,
                null, null, null, null, null,
                warehouse
            );
        } else {
            alertService.autoResolveIfExists(dedupKey);
        }
    }

    private void checkZoneCapacities(Warehouse warehouse) {
        warehouse.getZones().forEach(zone -> {
            if (zone.getCapacity() == null || zone.getCapacity() == 0) return;

            Double usedVolume = stockRepository.sumVolumeByZone(zone.getId());
            if (usedVolume == null) return;

            String dedupKey = buildDedupKey(AlertType.ZONE_NEAR_CAPACITY, "zone", zone.getId());
            double ratio = usedVolume / zone.getCapacity();

            if (ratio >= ZONE_CAPACITY_THRESHOLD) {
                int percent = (int) (ratio * 100);
                alertService.createIfNotExists(
                    AlertType.ZONE_NEAR_CAPACITY,
                    percent >= 95 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING,
                    "Zone \"" + zone.getName() + "\" à " + percent + "% de sa capacité"
                        + " (entrepôt : " + warehouse.getName() + ")",
                    dedupKey,
                    null, null, null,
                    zone.getId(),
                    zone.getName(),
                    warehouse
                );
            } else {
                alertService.autoResolveIfExists(dedupKey);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private String buildDedupKey(AlertType type, String entityName, Long entityId) {
        return type.name() + "-" + entityName + "-" + entityId;
    }
}
