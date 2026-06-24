package com.backend.module.product.repository;

import com.backend.module.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ── Détail avec joins pour éviter N+1 ────────────────────────
    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
        WHERE p.id = :id
        """)
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    // ── Catalogue global (admin) ──────────────────────────────────
    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
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

    // ── Produits dans le contexte d'un entrepôt ───────────────────
    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
        WHERE c.warehouse.id = :warehouseId
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

    // ── Produits d'une catégorie et d'un entrepôt (pour select stock) ──
    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
        WHERE c.id = :categoryId
          AND c.warehouse.id = :warehouseId
          AND p.isActive = true
        ORDER BY p.name ASC
        """)
    java.util.List<Product> findByCategoryAndWarehouse(
        @Param("categoryId") Long categoryId,
        @Param("warehouseId") Long warehouseId
    );

    // ── Produits actifs d'un entrepôt sans filtre catégorie ──
    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category c
        JOIN FETCH p.createdBy u
        JOIN FETCH u.role
        WHERE c.warehouse.id = :warehouseId
          AND p.isActive = true
        ORDER BY p.name ASC
        """)
    java.util.List<Product> findActiveByWarehouse(@Param("warehouseId") Long warehouseId);

    boolean existsByReference(String reference);
    boolean existsByBarcode(String barcode);
    boolean existsByNameIgnoreCaseAndCategoryId(String name, Long categoryId);
    boolean existsByNameIgnoreCaseAndCategoryIdAndIdNot(String name, Long categoryId, Long id);

    // ── Nombre de lignes stock actives sur ce produit ─────────────
    // Bloque la suppression logique par le gestionnaire si > 0
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.product.id = :productId AND s.quantityAvailable > 0")
    long countActiveStock(@Param("productId") Long productId);
}
