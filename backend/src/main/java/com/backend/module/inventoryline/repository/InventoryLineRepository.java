package com.backend.module.inventoryline.repository;

import com.backend.module.inventoryline.entity.InventoryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryLineRepository extends JpaRepository<InventoryLine, Long> {

    /** Vérifie qu'une ligne pour ce stock n'existe pas déjà dans l'inventaire. */
    boolean existsByInventoryIdAndStockId(Long inventoryId, Long stockId);

    /** Charge une ligne avec toutes ses associations pour mise à jour. */
    @Query("""
        SELECT l FROM InventoryLine l
        JOIN FETCH l.inventory
        JOIN FETCH l.product p
        JOIN FETCH p.category
        JOIN FETCH l.stock s
        JOIN FETCH s.warehouse
        JOIN FETCH l.zone
        WHERE l.id = :id
        """)
    Optional<InventoryLine> findByIdWithDetails(@Param("id") Long id);
}
