package com.backend.module.product.repository;

import com.backend.module.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ── Détail avec joins pour éviter N+1 ────────────────────────
    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
        LEFT JOIN FETCH p.warehouses
        WHERE p.id = :id
        """)
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    // ── Catalogue global (admin) ──────────────────────────────────
    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
        LEFT JOIN FETCH p.warehouses
        WHERE (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:active IS NULL OR p.isActive = :active)
        """)
    Page<Product> findAllWithFilters(
        @Param("search") String search,
        @Param("categoryId") Long categoryId,
        @Param("active") Boolean active,
        Pageable pageable
    );

    // ── Produits affectés à un entrepôt (via product_warehouses) ──
    // Remplace l'ancien filtre via category.warehouse_id
    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
        JOIN p.warehouses w
        WHERE w.id = :warehouseId
          AND p.isActive = true
          AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:categoryId IS NULL OR c.id = :categoryId)
        """)
    Page<Product> findByWarehouseContext(
        @Param("warehouseId") Long warehouseId,
        @Param("search") String search,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );

    // ── Select produits pour création de stock ────────────────────
    // Filtre sur l'entrepôt ET la catégorie de la zone (si fournie)
    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
        JOIN p.warehouses w
        WHERE w.id = :warehouseId
          AND p.isActive = true
          AND (:categoryId IS NULL OR c.id = :categoryId)
        ORDER BY p.name ASC
        """)
    List<Product> findForStockSelect(
        @Param("warehouseId") Long warehouseId,
        @Param("categoryId") Long categoryId
    );

    // ── Entrepôts qui ont la catégorie donnée (pour les checkboxes) ──
    // Utilisé dans le modal de création : afficher les entrepôts disponibles
    @Query("""
        SELECT w.id FROM Warehouse w
        JOIN w.categories c
        WHERE c.id = :categoryId
        """)
    List<Long> findWarehouseIdsByCategoryId(@Param("categoryId") Long categoryId);

    // ── Mise à jour des entrepôts d'un produit ───────────────────
    @Modifying
    @Query(value = "DELETE FROM product_warehouses WHERE product_id = :productId", nativeQuery = true)
    void deleteAllWarehouseLinks(@Param("productId") Long productId);

    @Modifying
    @Query(value = "INSERT INTO product_warehouses (product_id, warehouse_id) VALUES (:productId, :warehouseId)", nativeQuery = true)
    void insertWarehouseLink(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    // ── Vérifications unicité ─────────────────────────────────────
    boolean existsByReference(String reference);
    boolean existsByBarcode(String barcode);
    boolean existsByNameIgnoreCaseAndCategoryId(String name, Long categoryId);
    boolean existsByNameIgnoreCaseAndCategoryIdAndIdNot(String name, Long categoryId, Long id);

    // ── Stock actif sur ce produit (bloque désactivation gestionnaire) ──
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.product.id = :productId AND s.quantityAvailable > 0")
    long countActiveStock(@Param("productId") Long productId);
}
