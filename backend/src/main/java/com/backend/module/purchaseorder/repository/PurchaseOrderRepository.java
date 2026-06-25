package com.backend.module.purchaseorder.repository;

import com.backend.module.purchaseorder.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("""
        SELECT o FROM PurchaseOrder o
        JOIN FETCH o.supplier s
        JOIN FETCH o.warehouse w
        JOIN FETCH o.createdBy u
        LEFT JOIN FETCH o.lines l
        LEFT JOIN FETCH l.product
        WHERE o.supplier.id = :supplierId
          AND o.warehouse.id = :warehouseId
        ORDER BY o.orderDate DESC
        """)
    Page<PurchaseOrder> findBySuppliersAndWarehouse(
        @Param("supplierId") Long supplierId,
        @Param("warehouseId") Long warehouseId,
        Pageable pageable
    );
}
