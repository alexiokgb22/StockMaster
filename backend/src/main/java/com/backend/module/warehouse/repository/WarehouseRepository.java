package com.backend.module.warehouse.repository;

import com.backend.module.warehouse.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    boolean existsByNameIgnoreCase(String name);

    Page<Warehouse> findByIsActiveAndNameContainingIgnoreCaseOrCityContainingIgnoreCase(Boolean isActive, String name, String city, Pageable pageable);

    Page<Warehouse> findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(String name, String city, Pageable pageable);

    /** Entrepôts sans gestionnaire assigné (pour le select de réassignation). */
    Page<Warehouse> findByManagerIsNull(Pageable pageable);

    /** Entrepôts sans gestionnaire OU dont le gestionnaire est l'utilisateur ciblé (entrepôt actuel inclus). */
    @Query("""
        SELECT w FROM Warehouse w
        WHERE w.manager IS NULL
           OR w.manager.id = :managerId
        """)
    Page<Warehouse> findUnassignedOrManagedBy(@Param("managerId") Long managerId, Pageable pageable);

    /** Trouver l'entrepôt dont le manager est l'utilisateur donné.
     *  Retourne une liste pour être robuste face à des données corrompues (doublons). */
    List<Warehouse> findByManagerId(Long managerId);

    /** Retourne le nombre de gestionnaires différents de excludeUserId sur cet entrepôt. */
    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.id = :warehouseId AND w.manager IS NOT NULL AND w.manager.id <> :excludeUserId")
    long countOtherManager(@Param("warehouseId") Long warehouseId, @Param("excludeUserId") Long excludeUserId);

    /** Retourne le nombre de gestionnaires assignés à cet entrepôt. */
    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.id = :warehouseId AND w.manager IS NOT NULL")
    long countManager(@Param("warehouseId") Long warehouseId);
}
