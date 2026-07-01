package com.backend.module.reception.repository;

import com.backend.module.reception.entity.Reception;
import com.backend.module.shared.enums.ReceptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceptionRepository extends JpaRepository<Reception, Long> {

    // Récupération complète avec toutes les associations (pour le détail)
    @Query("""
        SELECT r FROM Reception r
        JOIN FETCH r.warehouse
        JOIN FETCH r.createdBy
        JOIN FETCH r.purchaseOrder po
        JOIN FETCH po.supplier
        LEFT JOIN FETCH r.validatedBy
        LEFT JOIN FETCH r.lines l
        LEFT JOIN FETCH l.purchaseOrderLine pol
        LEFT JOIN FETCH pol.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH l.zone
        WHERE r.id = :id
        """)
    Optional<Reception> findByIdWithDetails(@Param("id") Long id);

    // Liste paginée pour un entrepôt avec filtre statut
    @Query("""
        SELECT r FROM Reception r
        JOIN FETCH r.warehouse
        JOIN FETCH r.createdBy
        JOIN FETCH r.purchaseOrder po
        LEFT JOIN FETCH po.supplier
        LEFT JOIN FETCH r.validatedBy
        WHERE r.warehouse.id = :warehouseId
          AND (:status IS NULL OR r.status = :status)
        """)
    Page<Reception> findByWarehouseWithFilters(
        @Param("warehouseId") Long warehouseId,
        @Param("status") ReceptionStatus status,
        Pageable pageable
    );

    // Compte les bons PENDING d'un entrepôt (pour le badge gestionnaire)
    long countByWarehouseIdAndStatus(Long warehouseId, ReceptionStatus status);

    // Vérifie si un bon existe déjà pour une commande donnée (éviter les doublons PENDING)
    boolean existsByPurchaseOrderIdAndStatus(Long purchaseOrderId, ReceptionStatus status);

    boolean existsByReceptionNumber(String receptionNumber);

    /** Comptage pour le dashboard global. */
    long countByStatus(ReceptionStatus status);

    /** Comptage pour les rapports d'activité. */
    int countByCreatedByIdAndWarehouseIdAndCreatedAtBetween(
        Long createdById, Long warehouseId,
        java.time.LocalDateTime from, java.time.LocalDateTime to
    );

    /** Liste pour le détail d'un rapport d'activité. */
    java.util.List<Reception> findByCreatedByIdAndWarehouseIdAndCreatedAtBetween(
        Long createdById, Long warehouseId,
        java.time.LocalDateTime from, java.time.LocalDateTime to
    );
}
