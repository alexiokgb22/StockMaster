package com.backend.module.alert.service;

import com.backend.exception.ResourceNotFoundException;
import com.backend.module.alert.dto.AlertResponse;
import com.backend.module.alert.entity.Alert;
import com.backend.module.alert.repository.AlertRepository;
import com.backend.module.shared.enums.AlertSeverity;
import com.backend.module.shared.enums.AlertType;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final WarehouseRepository warehouseRepository;

    // ─────────────────────────────────────────────────────────────
    // LECTURE
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AlertResponse> findAll(
            Long warehouseId,
            AlertType type,
            AlertSeverity severity,
            Boolean isRead,
            Boolean isResolved,
            Pageable pageable) {
        return alertRepository
                .findWithFilters(warehouseId, type, severity, isRead, isResolved, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long warehouseId) {
        if (warehouseId != null) {
            return alertRepository.countUnread(warehouseId);
        }
        return alertRepository.countByIsReadFalseAndIsResolvedFalse();
    }

    // ─────────────────────────────────────────────────────────────
    // ACTIONS UTILISATEUR
    // ─────────────────────────────────────────────────────────────

    public AlertResponse markAsRead(Long alertId) {
        Alert alert = getAlert(alertId);
        alert.setIsRead(true);
        return toResponse(alertRepository.save(alert));
    }

    public AlertResponse resolve(Long alertId) {
        Alert alert = getAlert(alertId);
        alert.setIsResolved(true);
        alert.setIsRead(true);
        return toResponse(alertRepository.save(alert));
    }

    public int markAllAsRead(Long warehouseId) {
        return alertRepository.markAllAsRead(warehouseId);
    }

    // ─────────────────────────────────────────────────────────────
    // API INTERNE — utilisée par AlertScheduler
    // ─────────────────────────────────────────────────────────────

    /**
     * Crée une alerte si aucune alerte active (non résolue) avec la même
     * dedupKey n'existe déjà. Évite le spam du scheduler.
     */
    public void createIfNotExists(
            AlertType type,
            AlertSeverity severity,
            String message,
            String dedupKey,
            Long stockId,
            Long productId,
            String productName,
            Long zoneId,
            String zoneName,
            Warehouse warehouse) {

        if (alertRepository.existsByDedupKeyAndIsResolvedFalse(dedupKey)) {
            return; // Alerte déjà active — on ne spam pas
        }

        Alert alert = Alert.builder()
                .type(type)
                .severity(severity)
                .message(message)
                .dedupKey(dedupKey)
                .stockId(stockId)
                .productId(productId)
                .productName(productName)
                .zoneId(zoneId)
                .zoneName(zoneName)
                .warehouse(warehouse)
                .isRead(false)
                .isResolved(false)
                .build();

        alertRepository.save(alert);
        log.debug("Alerte créée : [{}] {} — {}", severity, type, message);
    }

    /**
     * Résout automatiquement une alerte quand la condition qui l'a déclenchée
     * n'est plus vraie (ex : stock remonté au-dessus du seuil).
     */
    public void autoResolveIfExists(String dedupKey) {
        alertRepository.findByDedupKeyAndIsResolvedFalse(dedupKey).ifPresent(alert -> {
            alert.setIsResolved(true);
            alertRepository.save(alert);
            log.debug("Alerte auto-résolue : {}", dedupKey);
        });
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private Alert getAlert(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte introuvable : " + id));
    }

    private AlertResponse toResponse(Alert a) {
        return AlertResponse.builder()
                .id(a.getId())
                .type(a.getType())
                .severity(a.getSeverity())
                .message(a.getMessage())
                .stockId(a.getStockId())
                .productId(a.getProductId())
                .productName(a.getProductName())
                .zoneId(a.getZoneId())
                .zoneName(a.getZoneName())
                .warehouseId(a.getWarehouse().getId())
                .warehouseName(a.getWarehouse().getName())
                .isRead(a.getIsRead())
                .isResolved(a.getIsResolved())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
