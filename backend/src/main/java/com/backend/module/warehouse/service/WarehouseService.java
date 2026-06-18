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
    public Page<WarehouseResponse> findAll(String search, Boolean active, Boolean unassigned, Pageable pageable) {

        // Cas spécial : entrepôts sans gestionnaire (pour le select de réassignation)
        if (Boolean.TRUE.equals(unassigned)) {
            return warehouseRepository.findByManagerIsNull(pageable).map(this::toResponse);
        }

        Page<Warehouse> warehouses;

        if (search != null && !search.isBlank() && active != null) {
            warehouses = warehouseRepository
                .findByIsActiveAndNameContainingIgnoreCaseOrCityContainingIgnoreCase(
                    active, search, search, pageable
                );
        } else if (search != null && !search.isBlank()) {
            warehouses = warehouseRepository
                .findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(search, search, pageable);
        } else {
            warehouses = warehouseRepository.findAll(pageable);
        }

        return warehouses.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public WarehouseResponse findById(Long id) {
        return toResponse(getWarehouse(id));
    }

    /**
     * Entrepôts disponibles pour l'assignation d'un gestionnaire.
     * - Si managerId est fourni : retourne les entrepôts sans manager + l'entrepôt actuel du manager
     *   (pour que le select affiche la valeur courante et les cibles disponibles).
     * - Sinon : retourne uniquement les entrepôts sans manager.
     */
    @Transactional(readOnly = true)
    public Page<WarehouseResponse> findUnassigned(Long managerId, Pageable pageable) {
        Page<Warehouse> warehouses = managerId != null
            ? warehouseRepository.findUnassignedOrManagedBy(managerId, pageable)
            : warehouseRepository.findByManagerIsNull(pageable);
        return warehouses.map(this::toResponse);
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

        if (req.getAddress() != null) warehouse.setAddress(req.getAddress());
        if (req.getCity() != null)    warehouse.setCity(req.getCity());
        if (req.getTotalCapacity() != null) warehouse.setTotalCapacity(req.getTotalCapacity());

        // ── Changement de gestionnaire ──────────────────────────────────────────
        // Le champ managerId dans le DTO est traité en "patch sémantique" :
        //   • non présent dans la requête (null)   → on ne touche pas au manager actuel
        //   • présent mais valeur 0 ou -1 (sentinel) → désaffectation
        //   • présent avec un id valide              → réassignation
        //
        // Ici on utilise la convention : managerId = null → pas de changement,
        // managerId = 0 → désaffectation, managerId > 0 → réassignation.
        if (req.getManagerId() != null) {

            User currentManager = warehouse.getManager();

            if (req.getManagerId() == 0) {
                // ── Désaffectation pure ──────────────────────────────────────────
                if (currentManager != null) {
                    currentManager.setAssignedWarehouse(null);
                    userRepository.save(currentManager);
                    warehouse.setManager(null);
                }

            } else {
                // ── Réassignation ────────────────────────────────────────────────
                Long newManagerId = req.getManagerId();

                // Pas de changement si c'est déjà le même gestionnaire
                if (currentManager == null || !currentManager.getId().equals(newManagerId)) {

                    User newManager = userRepository.findById(newManagerId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Gestionnaire introuvable : " + newManagerId));

                    // Vérifier (en BDD fraîche) que le nouveau gestionnaire
                    // n'est pas déjà assigné à un autre entrepôt
                    if (warehouseRepository.countOtherManager(id, newManagerId) > 0) {
                        throw new BusinessException(
                                "Ce gestionnaire est déjà assigné à un autre entrepôt");
                    }

                    // 1. Désaffecter l'ancien manager de cet entrepôt
                    if (currentManager != null) {
                        currentManager.setAssignedWarehouse(null);
                        userRepository.save(currentManager);
                    }

                    // 2. Désaffecter le nouveau manager de son entrepôt précédent
                    warehouseRepository.findByManagerId(newManagerId).forEach(oldWarehouse -> {
                        oldWarehouse.setManager(null);
                        warehouseRepository.save(oldWarehouse);
                    });

                    // 3. Affecter le nouveau manager
                    warehouse.setManager(newManager);
                    newManager.setAssignedWarehouse(warehouse);
                    userRepository.save(newManager);
                }
            }
        }

        return toResponse(warehouseRepository.save(warehouse));
    }

    // ─────────────────────────────────────────────────────────────
    // DÉSAFFECTATION DU MANAGER
    // Retire le manager d'un entrepôt sans supprimer l'entrepôt.
    // ─────────────────────────────────────────────────────────────

    public WarehouseResponse removeManager(Long warehouseId) {
        Warehouse warehouse = getWarehouse(warehouseId);
        warehouse.setManager(null);
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
