package com.backend.module.warehouse.repository;

import com.backend.module.warehouse.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    boolean existsByNameIgnoreCase(String name);
    
    Page<Warehouse> findByIsActiveAndNameContainingIgnoreCaseOrCityContainingIgnoreCase(Boolean isActive, String name, String city, Pageable pageable);

    Page<Warehouse> findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(String name, String city, Pageable pageable);
}
