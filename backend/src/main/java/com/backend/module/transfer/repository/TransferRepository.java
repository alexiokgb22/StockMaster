package com.backend.module.transfer.repository;

import com.backend.module.shared.enums.TransferStatus;
import com.backend.module.transfer.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    // Récupération complète avec toutes les associations
    @Query("""
        SELECT t FROM Transfer t
        JOIN FETCH t.sourceWarehouse
        JOIN FETCH t.targetWarehouse
        JOIN FETCH t.createdBy
        LEFT JOIN FETCH t.validatedBy
        LEFT JOIN FETCH t.receivedBy
        LEFT JOIN FETCH t.lines l
        LEFT JOIN FETCH l.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH l.sourceZone
        LEFT JOIN FETCH l.targetZone
        WHERE t.id = :id
        """)
    Optional<Transfer> findByIdWithDetails(@Param("id") Long id);

    // Transferts dont l'entrepôt source est warehouseId (vue gestionnaire source)
    @Query("""
        SELECT t FROM Transfer t
        JOIN FETCH t.sourceWarehouse
        JOIN FETCH t.targetWarehouse
        JOIN FETCH t.createdBy
        LEFT JOIN FETCH t.validatedBy
        LEFT JOIN FETCH t.receivedBy
        WHERE t.sourceWarehouse.id = :warehouseId
          AND (:status IS NULL OR t.status = :status)
        """)
    Page<Transfer> findBySourceWarehouseWithFilters(
        @Param("warehouseId") Long warehouseId,
        @Param("status") TransferStatus status,
        Pageable pageable
    );

    // Transferts entrants vers l'entrepôt cible (vue gestionnaire cible)
    @Query("""
        SELECT t FROM Transfer t
        JOIN FETCH t.sourceWarehouse
        JOIN FETCH t.targetWarehouse
        JOIN FETCH t.createdBy
        LEFT JOIN FETCH t.validatedBy
        LEFT JOIN FETCH t.receivedBy
        WHERE t.targetWarehouse.id = :warehouseId
          AND (:status IS NULL OR t.status = :status)
        """)
    Page<Transfer> findByTargetWarehouseWithFilters(
        @Param("warehouseId") Long warehouseId,
        @Param("status") TransferStatus status,
        Pageable pageable
    );

    // Vue Admin : tous les transferts (source ou cible = n'importe quel entrepôt)
    @Query("""
        SELECT t FROM Transfer t
        JOIN FETCH t.sourceWarehouse
        JOIN FETCH t.targetWarehouse
        JOIN FETCH t.createdBy
        LEFT JOIN FETCH t.validatedBy
        LEFT JOIN FETCH t.receivedBy
        WHERE (:status IS NULL OR t.status = :status)
          AND (:warehouseId IS NULL
               OR t.sourceWarehouse.id = :warehouseId
               OR t.targetWarehouse.id = :warehouseId)
        """)
    Page<Transfer> findAllWithFilters(
        @Param("status") TransferStatus status,
        @Param("warehouseId") Long warehouseId,
        Pageable pageable
    );

    // Compteur de transferts VALIDATED entrants (badge gestionnaire cible)
    long countByTargetWarehouseIdAndStatus(Long warehouseId, TransferStatus status);

    boolean existsByTransferNumber(String transferNumber);
}
