package com.backend.module.category.repository;

import com.backend.module.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ── Toutes les catégories (vue Admin globale) ─────────────────
    @Query("""
        SELECT c FROM Category c
        JOIN FETCH c.createdBy u
        JOIN FETCH u.role
        LEFT JOIN FETCH c.warehouse w
        WHERE (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Category> findAllWithDetails(
        @Param("search") String search,
        Pageable pageable
    );

    // ── Catégories non encore affectées à un entrepôt ─────────────
    // Utilisées pour la modale "Affecter une catégorie" sur un entrepôt
    @Query("""
        SELECT c FROM Category c
        JOIN FETCH c.createdBy u
        JOIN FETCH u.role
        WHERE c.warehouse IS NULL
          AND (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    List<Category> findUnassigned(@Param("search") String search);

    // ── Catégories d'un entrepôt spécifique ──────────────────────
    @Query("""
        SELECT c FROM Category c
        JOIN FETCH c.warehouse w
        JOIN FETCH c.createdBy u
        JOIN FETCH u.role
        WHERE c.warehouse.id = :warehouseId
          AND (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Category> findByWarehouseId(
        @Param("warehouseId") Long warehouseId,
        @Param("search") String search,
        Pageable pageable
    );

    @Query("""
        SELECT c FROM Category c
        JOIN FETCH c.createdBy u
        JOIN FETCH u.role
        LEFT JOIN FETCH c.warehouse w
        WHERE c.id = :id
        """)
    Optional<Category> findByIdWithDetails(@Param("id") Long id);

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByNameIgnoreCaseAndWarehouseId(String name, Long warehouseId);
    boolean existsByNameIgnoreCaseAndWarehouseIdAndIdNot(String name, Long warehouseId, Long id);

    /** Nombre de catégories Admin affectées à cet entrepôt. */
    @Query("""
        SELECT COUNT(c) FROM Category c
        WHERE c.warehouse.id = :warehouseId
          AND c.createdBy.role.name = 'Administrateur'
        """)
    long countAdminCategoriesByWarehouse(@Param("warehouseId") Long warehouseId);

    /** Nombre de produits rattachés à cette catégorie. */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countProducts(@Param("categoryId") Long categoryId);
}
