package com.backend.module.inventoryline.repository;

import com.backend.module.inventoryline.entity.InventoryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryLineRepository extends JpaRepository<InventoryLine, Long> {

    @Query("""
        SELECT l FROM InventoryLine l
        JOIN FETCH l.inventory i
        JOIN FETCH l.stock s
        JOIN FETCH l.product p
        JOIN FETCH p.category
        JOIN FETCH l.zone
        WHERE l.id = :id
          AND i.warehouse.id = :warehouseId
        """)
    Optional<InventoryLine> findByIdAndWarehouseId(
        @Param("id") Long id,
        @Param("warehouseId") Long warehouseId
    );

    // Compter les lignes non encore saisies (actualQty IS NULL)
    @Query("SELECT COUNT(l) FROM InventoryLine l WHERE l.inventory.id = :inventoryId AND l.actualQty IS NULL")
    long countUncounted(@Param("inventoryId") Long inventoryId);
}
