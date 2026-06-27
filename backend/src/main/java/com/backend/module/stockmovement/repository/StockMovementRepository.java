package com.backend.module.stockmovement.repository;

import com.backend.module.shared.enums.MovementType;
import com.backend.module.stockmovement.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * Mouvements agrégés par jour pour le graphique du dashboard.
     * Retourne : [day (String yyyy-MM-dd), movementType (String), totalQuantity (Long)]
     */
    @Query("""
        SELECT FUNCTION('DATE_FORMAT', sm.createdAt, '%Y-%m-%d'),
               CAST(sm.movementType AS string),
               COALESCE(SUM(sm.quantity), 0)
        FROM StockMovement sm
        WHERE (:warehouseId IS NULL OR sm.warehouse.id = :warehouseId)
          AND sm.createdAt >= :from
        GROUP BY FUNCTION('DATE_FORMAT', sm.createdAt, '%Y-%m-%d'), sm.movementType
        ORDER BY FUNCTION('DATE_FORMAT', sm.createdAt, '%Y-%m-%d') ASC
        """)
    List<Object[]> countMovementsByDay(
        @Param("warehouseId") Long warehouseId,
        @Param("from") LocalDateTime from
    );

    /**
     * Top N produits les plus mouvementés sur la période.
     * Retourne : [productId, productName, productReference, categoryName, movementCount, totalQuantity]
     */
    @Query("""
        SELECT p.id, p.name, p.reference, p.category.name,
               COUNT(sm), COALESCE(SUM(sm.quantity), 0)
        FROM StockMovement sm
        JOIN sm.product p
        WHERE (:warehouseId IS NULL OR sm.warehouse.id = :warehouseId)
          AND sm.createdAt >= :from
        GROUP BY p.id, p.name, p.reference, p.category.name
        ORDER BY COUNT(sm) DESC
        """)
    List<Object[]> findTopProductsByMovements(
        @Param("warehouseId") Long warehouseId,
        @Param("from") LocalDateTime from,
        org.springframework.data.domain.Pageable pageable
    );
}
