package com.backend.module.warehouse.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;
import com.backend.module.warehouse.dto.CreateWarehouseRequest;
import com.backend.module.warehouse.dto.UpdateWarehouseRequest;
import com.backend.module.warehouse.dto.WarehouseResponse;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // LECTURE
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
public Page<WarehouseResponse> findAll(String search, Boolean active, Pageable pageable) {

    Page<Warehouse> warehouses;

    if (search != null && !search.isBlank() && active != null) {
        warehouses = warehouseRepository
            .findByIsActiveAndNameContainingIgnoreCaseOrCityContainingIgnoreCase(
                active, search, search, pageable
            );

    } else if (search != null && !search.isBlank()) {
        warehouses = warehouseRepository
            .findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(search, search, pageable);

    } else if (active != null) {
        warehouses = warehouseRepository.findAll(pageable)
            .map(w -> w); // pas idéal, voir option 2

    } else {
        warehouses = warehouseRepository.findAll(pageable);
    }

    return warehouses.map(this::toResponse);
}

    @Transactional(readOnly = true)
    public WarehouseResponse findById(Long id) {
        return toResponse(getWarehouse(id));
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION
    // ─────────────────────────────────────────────────────────────

    public WarehouseResponse createWarehouse(CreateWarehouseRequest req) {
        if (warehouseRepository.existsByNameIgnoreCase(req.getName())) {
            throw new BusinessException("Un entrepôt avec ce nom existe déjà : " + req.getName());
        }

        User manager = null;
        if (req.getManagerId() != null) {
            manager = userRepository.findById(req.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gestionnaire introuvable : " + req.getManagerId()));
        }

        Warehouse warehouse = Warehouse.builder()
                .name(req.getName())
                .address(req.getAddress())
                .city(req.getCity())
                .totalCapacity(req.getTotalCapacity())
                .usedCapacity(0.0)
                .isActive(true)
                .manager(manager)
                .build();

        return toResponse(warehouseRepository.save(warehouse));
    }

    // ─────────────────────────────────────────────────────────────
    // MODIFICATION
    // ─────────────────────────────────────────────────────────────

    public WarehouseResponse updateWarehouse(Long id, UpdateWarehouseRequest req) {
        Warehouse warehouse = getWarehouse(id);

        if (req.getName() != null && !req.getName().equals(warehouse.getName())) {
            if (warehouseRepository.existsByNameIgnoreCase(req.getName())) {
                throw new BusinessException("Un entrepôt avec ce nom existe déjà : " + req.getName());
            }
            warehouse.setName(req.getName());
        }

        if (req.getAddress() != null) {
            warehouse.setAddress(req.getAddress());
        }

        if (req.getCity() != null) {
            warehouse.setCity(req.getCity());
        }

        if (req.getTotalCapacity() != null) {
            warehouse.setTotalCapacity(req.getTotalCapacity());
        }

        if (req.getManagerId() != null) {
            User manager = userRepository.findById(req.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gestionnaire introuvable : " + req.getManagerId()));
            warehouse.setManager(manager);
        }

        return toResponse(warehouseRepository.save(warehouse));
    }

    // ─────────────────────────────────────────────────────────────
    // ACTIVATION / DÉSACTIVATION
    // ─────────────────────────────────────────────────────────────

    public WarehouseResponse toggleWarehouse(Long id) {
        Warehouse warehouse = getWarehouse(id);
        warehouse.setIsActive(!warehouse.getIsActive());
        return toResponse(warehouseRepository.save(warehouse));
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────────

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private WarehouseResponse toResponse(Warehouse w) {
        return WarehouseResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .address(w.getAddress())
                .city(w.getCity())
                .totalCapacity(w.getTotalCapacity())
                .usedCapacity(w.getUsedCapacity())
                .isActive(w.getIsActive())
                .managerId(w.getManager() != null ? w.getManager().getId() : null)
                .managerName(w.getManager() != null ? w.getManager().getUsername() : null)
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}
