package com.backend.module.inventory.repository;

import com.backend.module.inventory.entity.Inventory;
import com.backend.module.shared.enums.InventoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Récupération complète avec lignes (pour le détail)
    @Query("""
        SELECT i FROM Inventory i
        JOIN FETCH i.warehouse
        JOIN FETCH i.createdBy
        LEFT JOIN FETCH i.lines l
<<<<<<< HEAD
        LEFT JOIN FETCH l.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH l.stock
>>>>>>> e23d4fc9a67e8abf52d5575486bdb7f802881c1d
        LEFT JOIN FETCH l.zone
        WHERE i.id = :id
        """)

    // Liste paginée pour un entrepôt
    @Query("""
        SELECT i FROM Inventory i
        JOIN FETCH i.warehouse
        JOIN FETCH i.createdBy
        WHERE i.warehouse.id = :warehouseId
        @Param("warehouseId") Long warehouseId,    
        Pageable pageable
    );

}
}