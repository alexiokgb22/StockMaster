package com.backend.module.zone.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.category.entity.Category;
import com.backend.module.category.repository.CategoryRepository;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import com.backend.module.zone.dto.AssignCategoryRequest;
import com.backend.module.zone.dto.CreateZoneRequest;
import com.backend.module.zone.dto.UpdateZoneRequest;
import com.backend.module.zone.dto.ZoneResponse;
import com.backend.module.zone.entity.Zone;
import com.backend.module.zone.repository.ZoneRepository;
import com.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // LECTURE
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ZoneResponse> findByWarehouse(Long warehouseId, Pageable pageable) {
        getWarehouse(warehouseId);
        return zoneRepository.findByWarehouseId(warehouseId, pageable).map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — catégories déjà couvertes par une zone Admin
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public java.util.List<Long> getCoveredCategoryIds(Long warehouseId) {
        getWarehouse(warehouseId);
        return zoneRepository.findCategoryIdsCoveredByAdmin(warehouseId);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION
    //
    // Règles par catégorie (remplace l'ancienne règle globale) :
    //
    //   Admin      → toujours autorisé.
    //
    //   Gestionnaire → autorisé si et seulement si :
    //     1. Il agit sur son propre entrepôt.
    //     2. a) Aucune catégorie fournie (zone sans catégorie) ET
    //           l'Admin n'a pas encore défini de zones pour cet entrepôt.
    //        OU
    //        b) Une catégorie est fournie ET cette catégorie n'est pas
    //           encore couverte par une zone Admin dans cet entrepôt.
    //           (l'Admin a affecté la catégorie à l'entrepôt mais n'a pas
    //           encore créé la zone correspondante → le gestionnaire complète)
    // ─────────────────────────────────────────────────────────────

    public ZoneResponse createZone(Long warehouseId, CreateZoneRequest req) {
        Warehouse warehouse = getWarehouse(warehouseId);
        User creator = currentUserEntity();

        boolean isManager = "Gestionnaire d'entrepôt".equals(creator.getRole().getName());

        if (isManager) {
            // Vérification de périmètre
            if (creator.getAssignedWarehouse() == null
                    || !creator.getAssignedWarehouse().getId().equals(warehouseId)) {
                throw new BusinessException("Vous ne pouvez gérer que les zones de votre propre entrepôt");
            }

            // La capacité est obligatoire pour le gestionnaire
            if (req.getCapacity() == null || req.getCapacity() <= 0) {
                throw new BusinessException("La capacité est obligatoire et doit être supérieure à 0");
            }

            if (req.getCategoryId() != null) {                // Cas b) — zone avec catégorie :
                // autorisé si l'Admin n'a pas encore créé de zone pour cette catégorie
                long adminCoverage = zoneRepository
                        .countAdminZonesByWarehouseAndCategory(warehouseId, req.getCategoryId());
                if (adminCoverage > 0) {
                    Category cat = categoryRepository.findById(req.getCategoryId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Catégorie introuvable : " + req.getCategoryId()));
                    throw new BusinessException(
                            "La catégorie \"" + cat.getName()
                            + "\" est déjà couverte par une zone définie par l'Administrateur.");
                }
            } else {
                // Cas a) — zone sans catégorie :
                // bloqué si l'Admin a déjà défini des zones (comportement original préservé)
                if (zoneRepository.countAdminZonesByWarehouse(warehouseId) > 0) {
                    throw new BusinessException(
                            "L'Administrateur a déjà défini des zones pour cet entrepôt. "
                            + "Pour créer une zone supplémentaire, sélectionnez une catégorie "
                            + "non encore couverte par l'Administrateur.");
                }
            }
        }

        // ── Règle 1 : capacité allouée + nouvelle zone ≤ totalCapacity entrepôt ──
        if (req.getCapacity() != null) {
            checkWarehouseCapacity(warehouse, req.getCapacity(), null);
        }

        // Numéro séquentiel dans l'entrepôt
        int sequence = (int) zoneRepository.countByWarehouseId(warehouseId) + 1;
        // Résoudre la catégorie si fournie
        Category category = null;
        if (req.getCategoryId() != null) {
            category = getCategory(req.getCategoryId(), warehouseId);
        }

        // ── Règle 1 : capacité allouée aux zones ≤ totalCapacity de l'entrepôt ──
        // Vérification uniquement si une capacité est fournie.
        if (req.getCapacity() != null && req.getCapacity() > 0) {
            double alreadyAllocated = zoneRepository.sumCapacityByWarehouseId(warehouseId);
            double afterCreation    = alreadyAllocated + req.getCapacity();
            if (warehouse.getTotalCapacity() != null
                    && afterCreation > warehouse.getTotalCapacity()) {
                double remaining = warehouse.getTotalCapacity() - alreadyAllocated;
                throw new BusinessException(String.format(
                    "Capacité insuffisante dans l'entrepôt. "
                    + "Espace restant disponible : %.1f m³ (demandé : %.1f m³).",
                    remaining, req.getCapacity()));
            }
        }

        String name = buildName(sequence, category);

        Zone zone = Zone.builder()
                .name(name)
                .sequenceNumber(sequence)
                .capacity(req.getCapacity())
                .zoneType(req.getZoneType())
                .warehouse(warehouse)
                .category(category)
                .createdBy(creator)
                .build();

        return toResponse(zoneRepository.save(zone));
    }

    // ─────────────────────────────────────────────────────────────
    // MODIFICATION
    //
    // Le gestionnaire ne peut modifier que les zones qu'il a créées.
    // L'admin peut modifier n'importe quelle zone.
    // Champs modifiables : zoneType, capacity (patch sémantique).
    // Le nom est recalculé automatiquement si zoneType change.
    // La catégorie est gérée séparément via assignCategory.
    // ─────────────────────────────────────────────────────────────

    public ZoneResponse updateZone(Long warehouseId, Long zoneId, UpdateZoneRequest req) {
        Zone zone = getZone(zoneId, warehouseId);
        User caller = currentUserEntity();

        boolean isManager = "Gestionnaire d'entrepôt".equals(caller.getRole().getName());

        if (isManager) {
            // Périmètre
            if (caller.getAssignedWarehouse() == null
                    || !caller.getAssignedWarehouse().getId().equals(warehouseId)) {
                throw new BusinessException("Vous ne pouvez gérer que les zones de votre propre entrepôt");
            }
            // Seules ses propres zones
            if (isAdminDefined(zone)) {
                throw new BusinessException(
                        "Vous ne pouvez pas modifier une zone définie par l'Administrateur");
            }
            // Capacité obligatoire pour le gestionnaire
            if (req.getCapacity() != null && req.getCapacity() <= 0) {
                throw new BusinessException("La capacité doit être supérieure à 0");
            }
        }

        if (req.getZoneType() != null) {
            zone.setZoneType(req.getZoneType());
        }
        if (req.getCapacity() != null) {
            // ── Règle 1 : recalcul sans l'ancienne valeur de cette zone ──
            checkWarehouseCapacity(zone.getWarehouse(), req.getCapacity(), zone.getId());
            zone.setCapacity(req.getCapacity());
        }

        // Recalculer le nom si le type a changé
        zone.setName(buildName(zone.getSequenceNumber(), zone.getCategory()));

        return toResponse(zoneRepository.save(zone));
    }

    // ─────────────────────────────────────────────────────────────
    // AFFECTATION DE CATÉGORIE
    // Règle : le gestionnaire ne peut affecter une catégorie qu'à
    // une zone qu'il a lui-même créée et sans catégorie définie
    // par l'Admin.
    // ─────────────────────────────────────────────────────────────

    public ZoneResponse assignCategory(Long warehouseId, Long zoneId, AssignCategoryRequest req) {
        Zone zone = getZone(zoneId, warehouseId);
        User caller = currentUserEntity();

        boolean isManager = "Gestionnaire d'entrepôt".equals(caller.getRole().getName());

        if (isManager) {
            if (caller.getAssignedWarehouse() == null
                    || !caller.getAssignedWarehouse().getId().equals(warehouseId)) {
                throw new BusinessException("Vous ne pouvez gérer que les zones de votre propre entrepôt");
            }
            // La zone existe et a été définie par l'Admin avec une catégorie → interdit
            if (isAdminDefined(zone) && zone.getCategory() != null) {
                throw new BusinessException(
                    "Cette zone a déjà une catégorie définie par l'Administrateur. Vous ne pouvez pas la modifier.");
            }
        }

        Category category = getCategory(req.getCategoryId(), warehouseId);
        zone.setCategory(category);
        zone.setName(buildName(zone.getSequenceNumber(), category));

        return toResponse(zoneRepository.save(zone));
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private String buildName(int sequence, Category category) {
        String seq = String.format("%02d", sequence);
        if (category != null) {
            return "ZONE-" + seq + "-" + category.getName();
        }
        return "ZONE-" + seq;
    }

    private boolean isAdminDefined(Zone zone) {
        return "Administrateur".equals(zone.getCreatedBy().getRole().getName());
    }

    /**
     * Règle 1 — vérifie que la somme des capacités des zones existantes
     * + la nouvelle capacité ne dépasse pas la capacité totale de l'entrepôt.
     *
     * @param warehouse      l'entrepôt cible
     * @param newCapacity    la capacité à ajouter ou substituer
     * @param excludeZoneId  id de la zone à exclure du calcul (modification),
     *                       null à la création
     */
    private void checkWarehouseCapacity(Warehouse warehouse, double newCapacity, Long excludeZoneId) {
        if (warehouse.getTotalCapacity() == null) {
            // Pas de capacité totale définie → pas de contrainte
            return;
        }

        double alreadyAllocated = excludeZoneId == null
                ? zoneRepository.sumCapacityByWarehouseId(warehouse.getId())
                : zoneRepository.sumCapacityByWarehouseIdExcluding(warehouse.getId(), excludeZoneId);

        double afterAllocation = alreadyAllocated + newCapacity;

        if (afterAllocation > warehouse.getTotalCapacity()) {
            double remaining = warehouse.getTotalCapacity() - alreadyAllocated;
            throw new BusinessException(String.format(
                "Capacité insuffisante dans l'entrepôt \"%s\" : "
                + "%.1f m³ demandé, %.1f m³ disponible (total %.1f m³, déjà alloué %.1f m³).",
                warehouse.getName(),
                newCapacity,
                Math.max(remaining, 0.0),
                warehouse.getTotalCapacity(),
                alreadyAllocated
            ));
        }
    }

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private Zone getZone(Long zoneId, Long warehouseId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + zoneId));
        if (!zone.getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException("Zone introuvable dans cet entrepôt : " + zoneId);
        }
        return zone;
    }

    private Category getCategory(Long categoryId, Long warehouseId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + categoryId));
        if (!category.getWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException("Cette catégorie n'appartient pas à cet entrepôt");
        }
        return category;
    }

    private User currentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(details.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private ZoneResponse toResponse(Zone z) {
        return ZoneResponse.builder()
                .id(z.getId())
                .name(z.getName())
                .sequenceNumber(z.getSequenceNumber())
                .capacity(z.getCapacity())
                .zoneType(z.getZoneType() != null ? z.getZoneType().name() : null)
                .warehouseId(z.getWarehouse().getId())
                .warehouseName(z.getWarehouse().getName())
                .categoryId(z.getCategory() != null ? z.getCategory().getId() : null)
                .categoryName(z.getCategory() != null ? z.getCategory().getName() : null)
                .createdByUsername(z.getCreatedBy().getUsername())
                .isAdminDefined(isAdminDefined(z))
                .createdAt(z.getCreatedAt())
                .updatedAt(z.getUpdatedAt())
                .build();
    }
}
