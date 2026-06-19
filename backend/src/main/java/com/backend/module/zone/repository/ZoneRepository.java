package com.backend.module.zone.repository;

import com.backend.module.zone.entity.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    @Query("""
        SELECT z FROM Zone z
        JOIN FETCH z.warehouse w
        JOIN FETCH z.createdBy u
        JOIN FETCH u.role
        LEFT JOIN FETCH z.category c
        WHERE z.warehouse.id = :warehouseId
        """)
    Page<Zone> findByWarehouseId(@Param("warehouseId") Long warehouseId, Pageable pageable);

    /** Nombre de zones dans l'entrepôt — pour le numéro séquentiel. */
    long countByWarehouseId(Long warehouseId);

    /** Nombre de zones créées par l'Admin dans cet entrepôt. */
    @Query("""
        SELECT COUNT(z) FROM Zone z
        WHERE z.warehouse.id = :warehouseId
          AND z.createdBy.role.name = 'Administrateur'
        """)
    long countAdminZonesByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * Vérifie si une catégorie donnée est déjà couverte par une zone Admin
     * dans cet entrepôt.
     * Permet la règle par catégorie : le gestionnaire peut créer une zone
     * pour une catégorie Admin non encore couverte.
     */
    @Query("""
        SELECT COUNT(z) FROM Zone z
        WHERE z.warehouse.id = :warehouseId
          AND z.category.id = :categoryId
          AND z.createdBy.role.name = 'Administrateur'
        """)
    long countAdminZonesByWarehouseAndCategory(
        @Param("warehouseId") Long warehouseId,
        @Param("categoryId") Long categoryId
    );

    /**
     * IDs des catégories déjà couvertes par au moins une zone Admin dans cet entrepôt.
     * Exposé au frontend pour griser/masquer les catégories déjà traitées.
     */
    @Query("""
        SELECT DISTINCT z.category.id FROM Zone z
        WHERE z.warehouse.id = :warehouseId
          AND z.category IS NOT NULL
          AND z.createdBy.role.name = 'Administrateur'
        """)
    java.util.List<Long> findCategoryIdsCoveredByAdmin(@Param("warehouseId") Long warehouseId);

    /**
     * Somme des capacités de toutes les zones d'un entrepôt.
     * Seules les zones avec une capacité déclarée sont comptées (NULL ignoré).
     * Utilisé pour la règle 1 : vérifier que la capacité allouée aux zones
     * ne dépasse pas la capacité totale de l'entrepôt.
     */
    @Query("""
        SELECT COALESCE(SUM(z.capacity), 0.0) FROM Zone z
        WHERE z.warehouse.id = :warehouseId
          AND z.capacity IS NOT NULL
        """)
    Double sumCapacityByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * Somme des capacités de toutes les zones d'un entrepôt, en excluant une zone donnée.
     * Utilisé lors de la modification d'une zone pour recalculer le total sans compter
     * l'ancienne valeur de la zone en cours de mise à jour.
     */
    @Query("""
        SELECT COALESCE(SUM(z.capacity), 0.0) FROM Zone z
        WHERE z.warehouse.id = :warehouseId
          AND z.id <> :excludeZoneId
          AND z.capacity IS NOT NULL
        """)
    Double sumCapacityByWarehouseIdExcluding(
        @Param("warehouseId") Long warehouseId,
        @Param("excludeZoneId") Long excludeZoneId
    );
}
