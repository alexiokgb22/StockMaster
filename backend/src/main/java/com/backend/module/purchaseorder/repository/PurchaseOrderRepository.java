package com.backend.module.purchaseorder.repository;

import com.backend.module.purchaseorder.entity.PurchaseOrder;
import com.backend.module.shared.enums.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Récupération complète d'une commande avec toutes ses associations
    @Query("""
        SELECT o FROM PurchaseOrder o
        JOIN FETCH o.warehouse
        JOIN FETCH o.createdBy
        LEFT JOIN FETCH o.supplier
        LEFT JOIN FETCH o.lines l
        LEFT JOIN FETCH l.product p
        LEFT JOIN FETCH p.category
        WHERE o.id = :id
        """)
    Optional<PurchaseOrder> findByIdWithDetails(@Param("id") Long id);

    // Liste des commandes d'un entrepôt avec filtres
    @Query("""
        SELECT o FROM PurchaseOrder o
        JOIN FETCH o.warehouse
        JOIN FETCH o.createdBy
        LEFT JOIN FETCH o.supplier
        WHERE o.warehouse.id = :warehouseId
          AND (:status IS NULL OR o.status = :status)
        """)
    Page<PurchaseOrder> findByWarehouseWithFilters(
        @Param("warehouseId") Long warehouseId,
        @Param("status") PurchaseOrderStatus status,
        Pageable pageable
    );

    // Toutes les commandes (vue admin) avec filtres
    @Query("""
        SELECT o FROM PurchaseOrder o
        JOIN FETCH o.warehouse
        JOIN FETCH o.createdBy
        LEFT JOIN FETCH o.supplier
        WHERE (:status IS NULL OR o.status = :status)
          AND (:warehouseId IS NULL OR o.warehouse.id = :warehouseId)
        """)
    Page<PurchaseOrder> findAllWithFilters(
        @Param("status") PurchaseOrderStatus status,
        @Param("warehouseId") Long warehouseId,
        Pageable pageable
    );

    // Historique des commandes d'un fournisseur (pour SupplierDetail)
    @Query("""
        SELECT o FROM PurchaseOrder o
        JOIN FETCH o.warehouse
        JOIN FETCH o.createdBy
        LEFT JOIN FETCH o.lines l
        LEFT JOIN FETCH l.product
        WHERE o.supplier.id = :supplierId
        ORDER BY o.createdAt DESC
        """)
    Page<PurchaseOrder> findBySupplierId(
        @Param("supplierId") Long supplierId,
        Pageable pageable
    );

    boolean existsByOrderNumber(String orderNumber);
}
