package com.backend.module.stock.repository;

import com.backend.module.stock.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    // ── Détail avec joins ─────────────────────────────────────────
    @Query("""
        SELECT s FROM Stock s
        JOIN FETCH s.product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
        JOIN FETCH s.warehouse w
        JOIN FETCH s.zone z
        WHERE s.id = :id
        """)
    Optional<Stock> findByIdWithDetails(@Param("id") Long id);

    // ── Stocks d'un entrepôt avec filtres ────────────────────────
    @Query("""
        SELECT s FROM Stock s
        JOIN FETCH s.product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
        JOIN FETCH s.warehouse w
        JOIN FETCH s.zone z
        WHERE s.warehouse.id = :warehouseId
          AND (:zoneId IS NULL OR z.id = :zoneId)
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:belowMin IS NULL OR (:belowMin = true AND s.minStock IS NOT NULL AND s.quantityAvailable < s.minStock))
          AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Stock> findByWarehouseWithFilters(
        @Param("warehouseId") Long warehouseId,
        @Param("zoneId") Long zoneId,
        @Param("categoryId") Long categoryId,
        @Param("belowMin") Boolean belowMin,
        @Param("search") String search,
        Pageable pageable
    );

    // ── Vérifier si une ligne stock existe déjà ───────────────────
    boolean existsByProductIdAndWarehouseIdAndZoneId(Long productId, Long warehouseId, Long zoneId);

    Optional<Stock> findByProductIdAndWarehouseIdAndZoneId(Long productId, Long warehouseId, Long zoneId);

    // ── Volume total utilisé dans une zone (pour usedCapacity) ───
    @Query("""
        SELECT COALESCE(SUM(s.quantityAvailable * p.volume), 0.0)
        FROM Stock s
        JOIN s.product p
        WHERE s.zone.id = :zoneId
          AND p.volume IS NOT NULL
        """)
    Double sumVolumeByZone(@Param("zoneId") Long zoneId);

    // ── Volume total utilisé dans un entrepôt ────────────────────
    @Query("""
        SELECT COALESCE(SUM(s.quantityAvailable * p.volume), 0.0)
        FROM Stock s
        JOIN s.product p
        WHERE s.warehouse.id = :warehouseId
          AND p.volume IS NOT NULL
        """)
    Double sumVolumeByWarehouse(@Param("warehouseId") Long warehouseId);

    // ── Nombre de lignes stock en dessous du seuil ───────────────
    @Query("""
        SELECT COUNT(s) FROM Stock s
        WHERE s.warehouse.id = :warehouseId
          AND s.minStock IS NOT NULL
          AND s.quantityAvailable < s.minStock
        """)
    long countBelowMinByWarehouse(@Param("warehouseId") Long warehouseId);
}
