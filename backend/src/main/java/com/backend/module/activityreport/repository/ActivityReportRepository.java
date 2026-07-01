package com.backend.module.activityreport.repository;

import com.backend.module.activityreport.entity.ActivityReport;
import com.backend.module.shared.enums.ActivityReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ActivityReportRepository extends JpaRepository<ActivityReport, Long> {

    // ── Détail complet ─────────────────────────────────────────────
    @Query("""
        SELECT r FROM ActivityReport r
        JOIN FETCH r.storekeeper u
        JOIN FETCH u.role
        JOIN FETCH r.warehouse
        WHERE r.id = :id
        """)
    Optional<ActivityReport> findByIdWithDetails(@Param("id") Long id);

    // ── Vue magasinier : ses propres rapports ──────────────────────
    @Query("""
        SELECT r FROM ActivityReport r
        JOIN FETCH r.storekeeper u
        JOIN FETCH r.warehouse
        WHERE r.storekeeper.id = :storekeeperId
          AND (:status IS NULL OR r.status = :status)
        ORDER BY r.reportDate DESC
        """)
    Page<ActivityReport> findByStorekeeper(
        @Param("storekeeperId") Long storekeeperId,
        @Param("status") ActivityReportStatus status,
        Pageable pageable
    );

    // ── Vue gestionnaire : tous les rapports de son entrepôt ───────
    @Query("""
        SELECT r FROM ActivityReport r
        JOIN FETCH r.storekeeper u
        JOIN FETCH r.warehouse
        WHERE r.warehouse.id = :warehouseId
          AND (:storekeeperId IS NULL OR r.storekeeper.id = :storekeeperId)
          AND (:status IS NULL OR r.status = :status)
          AND (:from IS NULL OR r.reportDate >= :from)
          AND (:to IS NULL OR r.reportDate <= :to)
        ORDER BY r.reportDate DESC
        """)
    Page<ActivityReport> findByWarehouse(
        @Param("warehouseId") Long warehouseId,
        @Param("storekeeperId") Long storekeeperId,
        @Param("status") ActivityReportStatus status,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to,
        Pageable pageable
    );

    // ── Vérifier doublon (un rapport par magasinier par jour) ──────
    boolean existsByStorekeeperIdAndReportDate(Long storekeeperId, LocalDate reportDate);

    // ── Rapport du jour en DRAFT (pour reprise) ────────────────────
    Optional<ActivityReport> findByStorekeeperIdAndReportDateAndStatus(
        Long storekeeperId, LocalDate reportDate, ActivityReportStatus status
    );
}
