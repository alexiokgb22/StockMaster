package com.backend.module.category.repository;

import com.backend.module.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /** Nombre de produits rattachés à cette catégorie — évite de charger la collection en mémoire. */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countProducts(@Param("categoryId") Long categoryId);
}
