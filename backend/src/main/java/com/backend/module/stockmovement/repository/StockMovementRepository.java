package com.backend.module.stockmovement.repository;

import com.backend.module.shared.enums.MovementType;
import com.backend.module.stockmovement.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("""
        SELECT sm FROM StockMovement sm
        JOIN FETCH sm.product p
        JOIN FETCH sm.warehouse w
        JOIN FETCH sm.zone z
        JOIN FETCH sm.createdBy u
        WHERE sm.warehouse.id = :warehouseId
          AND (:movementType IS NULL OR sm.movementType = :movementType)
          AND (:productId IS NULL OR p.id = :productId)
          AND (:zoneId IS NULL OR z.id = :zoneId)
        ORDER BY sm.createdAt DESC
        """)
    Page<StockMovement> findByWarehouseWithFilters(
        @Param("warehouseId") Long warehouseId,
        @Param("movementType") MovementType movementType,
        @Param("productId") Long productId,
        @Param("zoneId") Long zoneId,
        Pageable pageable
    );
}
