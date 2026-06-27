package com.backend.module.alert.repository;

import com.backend.module.alert.entity.Alert;
import com.backend.module.shared.enums.AlertSeverity;
import com.backend.module.shared.enums.AlertType;import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // ── Lecture filtrée ────────────────────────────────────────────
    @Query("""
        SELECT a FROM Alert a
        JOIN FETCH a.warehouse w
        WHERE (:warehouseId IS NULL OR w.id = :warehouseId)
          AND (:type IS NULL OR a.type = :type)
          AND (:severity IS NULL OR a.severity = :severity)
          AND (:isRead IS NULL OR a.isRead = :isRead)
          AND (:isResolved IS NULL OR a.isResolved = :isResolved)
        ORDER BY a.createdAt DESC
        """)
    Page<Alert> findWithFilters(
        @Param("warehouseId") Long warehouseId,
        @Param("type") AlertType type,
        @Param("severity") AlertSeverity severity,
        @Param("isRead") Boolean isRead,
        @Param("isResolved") Boolean isResolved,
        Pageable pageable
    );

    // ── Déduplication — vérifie si une alerte active existe déjà ──
    boolean existsByDedupKeyAndIsResolvedFalse(String dedupKey);

    // ── Résolution automatique quand la condition disparaît ────────
    Optional<Alert> findByDedupKeyAndIsResolvedFalse(String dedupKey);

    // ── Comptage non lus pour le badge de notification ─────────────
    @Query("""
        SELECT COUNT(a) FROM Alert a
        WHERE a.isRead = false
          AND a.isResolved = false
          AND (:warehouseId IS NULL OR a.warehouse.id = :warehouseId)
        """)
    long countUnread(@Param("warehouseId") Long warehouseId);

    // ── Comptage non lus globaux (admin) ───────────────────────────
    long countByIsReadFalseAndIsResolvedFalse();

    // ── Marquer toutes comme lues (par entrepôt ou global) ─────────
    @Modifying
    @Query("""
        UPDATE Alert a SET a.isRead = true
        WHERE a.isRead = false
          AND (:warehouseId IS NULL OR a.warehouse.id = :warehouseId)
        """)
    int markAllAsRead(@Param("warehouseId") Long warehouseId);

    // ── Agrégations pour le dashboard ─────────────────────────────

    /** Alertes critiques non résolues sur tout le système. */
    long countBySeverityAndIsResolvedFalse(AlertSeverity severity);

    /** Alertes critiques non résolues pour un entrepôt. */
    @Query("""
        SELECT COUNT(a) FROM Alert a
        WHERE a.severity = :severity
          AND a.isResolved = false
          AND a.warehouse.id = :warehouseId
        """)
    long countBySeverityAndWarehouseIdAndIsResolvedFalse(
        @Param("severity") AlertSeverity severity,
        @Param("warehouseId") Long warehouseId
    );
}
