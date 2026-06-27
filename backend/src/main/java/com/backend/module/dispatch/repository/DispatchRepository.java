package com.backend.module.dispatch.repository;

import com.backend.module.dispatch.entity.Dispatch;
import com.backend.module.shared.enums.DispatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    // Récupération complète avec associations (pour le détail)
    @Query("""
        SELECT d FROM Dispatch d
        JOIN FETCH d.warehouse
        JOIN FETCH d.createdBy
        LEFT JOIN FETCH d.validatedBy
        LEFT JOIN FETCH d.lines l
        LEFT JOIN FETCH l.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH l.zone
        WHERE d.id = :id
        """)
    Optional<Dispatch> findByIdWithDetails(@Param("id") Long id);

    // Liste paginée pour un entrepôt avec filtre statut
    @Query("""
        SELECT d FROM Dispatch d
        JOIN FETCH d.warehouse
        JOIN FETCH d.createdBy
        LEFT JOIN FETCH d.validatedBy
        WHERE d.warehouse.id = :warehouseId
          AND (:status IS NULL OR d.status = :status)
        """)
    Page<Dispatch> findByWarehouseWithFilters(
        @Param("warehouseId") Long warehouseId,
        @Param("status") DispatchStatus status,
        Pageable pageable
    );

    // Compte les bons PENDING d'un entrepôt (pour le badge gestionnaire)
    long countByWarehouseIdAndStatus(Long warehouseId, DispatchStatus status);

    boolean existsByDispatchNumber(String dispatchNumber);

    /** Compte tous les bons d'un statut donné (dashboard global). */
    long countByStatus(DispatchStatus status);
}
