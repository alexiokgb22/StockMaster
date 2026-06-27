package com.backend.module.stock.repository;

import com.backend.module.stock.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    // ── Snapshot pour inventaire FULL ────────────────────────────
    @Query("""
        SELECT s FROM Stock s
        JOIN FETCH s.product p
        JOIN FETCH p.category
        JOIN FETCH s.warehouse
        JOIN FETCH s.zone
        WHERE s.warehouse.id = :warehouseId
        ORDER BY s.zone.name ASC, p.name ASC
        """)
    List<Stock> findAllByWarehouseId(@Param("warehouseId") Long warehouseId);

    // ── Snapshot pour inventaire PARTIAL (par zones) ─────────────
    @Query("""
        SELECT s FROM Stock s
        JOIN FETCH s.product p
        JOIN FETCH p.category
        JOIN FETCH s.warehouse
        JOIN FETCH s.zone z
        WHERE s.warehouse.id = :warehouseId
          AND z.id IN :zoneIds
        ORDER BY z.name ASC, p.name ASC
        """)
    List<Stock> findAllByWarehouseIdAndZoneIdIn(
        @Param("warehouseId") Long warehouseId,
        @Param("zoneIds") List<Long> zoneIds
    );

    // ── Agrégations pour le dashboard ─────────────────────────────

    /** Nombre total de lignes de stock pour un entrepôt. */
    long countByWarehouseId(Long warehouseId);

    /** Nombre de ruptures totales (qty == 0) pour un entrepôt. */
    @Query("""
        SELECT COUNT(s) FROM Stock s
        WHERE s.warehouse.id = :warehouseId
          AND s.quantityAvailable = 0
        """)
    long countStockOutByWarehouse(@Param("warehouseId") Long warehouseId);

    /** Nombre total de lignes sous seuil sur tout le système. */
    @Query("""
        SELECT COUNT(s) FROM Stock s
        WHERE s.minStock IS NOT NULL
          AND s.quantityAvailable < s.minStock
        """)
    long countBelowMinGlobal();

    /** Nombre total de ruptures sur tout le système. */
    @Query("""
        SELECT COUNT(s) FROM Stock s
        WHERE s.quantityAvailable = 0
        """)
    long countStockOutGlobal();

    /** Répartition du stock par catégorie pour un entrepôt.
     *  Retourne : [categoryId, categoryName, stockLineCount, totalQuantity] */
    @Query("""
        SELECT p.category.id, p.category.name,
               COUNT(s), COALESCE(SUM(s.quantityAvailable), 0)
        FROM Stock s
        JOIN s.product p
        WHERE (:warehouseId IS NULL OR s.warehouse.id = :warehouseId)
        GROUP BY p.category.id, p.category.name
        ORDER BY COUNT(s) DESC
        """)
    List<Object[]> countStockByCategory(@Param("warehouseId") Long warehouseId);
}
