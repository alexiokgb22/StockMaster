package com.backend.module.supplier.repository;

import com.backend.module.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
        SELECT DISTINCT s FROM Supplier s
        LEFT JOIN FETCH s.warehouses
        WHERE (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(s.city) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:active IS NULL OR s.isActive = :active)
        """)
    Page<Supplier> findAllWithFilters(
        @Param("search") String search,
        @Param("active") Boolean active,
        Pageable pageable
    );

    @Query("""
        SELECT DISTINCT s FROM Supplier s
        LEFT JOIN FETCH s.warehouses w
        WHERE s.id = :id
        """)
    java.util.Optional<Supplier> findByIdWithWarehouses(@Param("id") Long id);
}
