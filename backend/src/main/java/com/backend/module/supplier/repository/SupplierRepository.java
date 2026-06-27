package com.backend.module.supplier.repository;

import com.backend.module.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
        SELECT s FROM Supplier s
        WHERE (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(s.city) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:active IS NULL OR s.isActive = :active)
        """)
    Page<Supplier> findAllWithFilters(
        @Param("search") String search,
        @Param("active") Boolean active,
        Pageable pageable
    );

    Optional<Supplier> findById(Long id);

    /** Comptage pour le dashboard global. */
    long countByIsActive(Boolean isActive);
}
