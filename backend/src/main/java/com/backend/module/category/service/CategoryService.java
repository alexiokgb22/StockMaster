package com.backend.module.category.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.category.dto.AssignWarehouseRequest;
import com.backend.module.category.dto.CategoryResponse;
import com.backend.module.category.dto.CreateCategoryRequest;
import com.backend.module.category.dto.UpdateCategoryRequest;
import com.backend.module.category.entity.Category;
import com.backend.module.category.repository.CategoryRepository;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import com.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // LECTURE — Admin : toutes les catégories (avec ou sans entrepôt)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAll(String search, Pageable pageable) {
        return categoryRepository.findAllWithDetails(search, pageable).map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — catégories non affectées (pour la modale d'affectation)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CategoryResponse> findUnassigned(String search) {
        return categoryRepository.findUnassigned(search).stream().map(this::toResponse).toList();
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — catégories d'un entrepôt (Admin + Gestionnaire)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<CategoryResponse> findByWarehouse(Long warehouseId, String search, Pageable pageable) {
        getWarehouse(warehouseId);
        return categoryRepository.findByWarehouseId(warehouseId, search, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long warehouseId, Long id) {
        Category category = getCategory(id);
        if (category.getWarehouse() == null || !category.getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException("Catégorie introuvable dans cet entrepôt : " + id);
        }
        return toResponse(category);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — Admin uniquement, sans entrepôt (étape 1)
    // ─────────────────────────────────────────────────────────────

    public CategoryResponse create(CreateCategoryRequest req) {
        if (categoryRepository.existsByNameIgnoreCase(req.getName())) {
            throw new BusinessException("Une catégorie \"" + req.getName() + "\" existe déjà");
        }

        User creator = currentUserEntity();

        Category category = Category.builder()
                .name(req.getName().trim())
                .description(req.getDescription())
                .warehouse(null) // pas encore affectée
                .createdBy(creator)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    // ─────────────────────────────────────────────────────────────
    // AFFECTATION — Admin affecte une catégorie globale à un entrepôt (étape 2)
    // ─────────────────────────────────────────────────────────────

    public CategoryResponse assignToWarehouse(Long categoryId, AssignWarehouseRequest req) {
        Category category = getCategory(categoryId);

        if (category.getWarehouse() != null) {
            throw new BusinessException(
                "La catégorie \"" + category.getName() + "\" est déjà affectée à l'entrepôt \""
                + category.getWarehouse().getName() + "\"");
        }

        Warehouse warehouse = getWarehouse(req.getWarehouseId());

        // Vérifier unicité du nom dans l'entrepôt cible
        if (categoryRepository.existsByNameIgnoreCaseAndWarehouseId(category.getName(), warehouse.getId())) {
            throw new BusinessException(
                "Une catégorie \"" + category.getName() + "\" existe déjà dans l'entrepôt \""
                + warehouse.getName() + "\"");
        }

        category.setWarehouse(warehouse);
        categoryRepository.save(category);
        return toResponse(categoryRepository.findByIdWithDetails(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + categoryId)));
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — Gestionnaire dans son entrepôt (workflow direct)
    // ─────────────────────────────────────────────────────────────

    public CategoryResponse createInWarehouse(Long warehouseId, CreateCategoryRequest req) {
        Warehouse warehouse = getWarehouse(warehouseId);
        User creator = currentUserEntity();

        // Vérifier que le gestionnaire agit sur son propre entrepôt
        if (creator.getAssignedWarehouse() == null
                || !creator.getAssignedWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException("Vous ne pouvez gérer que les catégories de votre propre entrepôt");
        }

        // Vérifier qu'aucune catégorie Admin n'est affectée à cet entrepôt
        if (categoryRepository.countAdminCategoriesByWarehouse(warehouseId) > 0) {
            throw new BusinessException(
                "L'Administrateur a déjà défini les catégories de cet entrepôt. Vous ne pouvez pas en créer de nouvelles.");
        }

        if (categoryRepository.existsByNameIgnoreCaseAndWarehouseId(req.getName(), warehouseId)) {
            throw new BusinessException("Une catégorie \"" + req.getName() + "\" existe déjà dans cet entrepôt");
        }

        Category category = Category.builder()
                .name(req.getName().trim())
                .description(req.getDescription())
                .warehouse(warehouse)
                .createdBy(creator)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    // ─────────────────────────────────────────────────────────────
    // MODIFICATION
    // ─────────────────────────────────────────────────────────────

    public CategoryResponse update(Long id, UpdateCategoryRequest req) {
        Category category = getCategory(id);
        User caller = currentUserEntity();

        boolean isManager = "Gestionnaire d'entrepôt".equals(caller.getRole().getName());
        if (isManager && isAdminDefined(category)) {
            throw new BusinessException("Vous ne pouvez pas modifier une catégorie définie par l'Administrateur");
        }

        if (req.getName() != null && !req.getName().isBlank()) {
            String trimmed = req.getName().trim();
            boolean nameConflict = category.getWarehouse() != null
                ? categoryRepository.existsByNameIgnoreCaseAndWarehouseIdAndIdNot(trimmed, category.getWarehouse().getId(), id)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(trimmed, id);
            if (!trimmed.equalsIgnoreCase(category.getName()) && nameConflict) {
                throw new BusinessException("Une catégorie \"" + trimmed + "\" existe déjà");
            }
            category.setName(trimmed);
        }

        if (req.getDescription() != null) category.setDescription(req.getDescription());

        return toResponse(categoryRepository.save(category));
    }

    // ─────────────────────────────────────────────────────────────
    // DÉSAFFECTATION — Admin retire une catégorie d'un entrepôt
    // La catégorie reste en base, warehouse_id est remis à null.
    // Bloqué si des produits sont rattachés à cette catégorie
    // dans cet entrepôt.
    // ─────────────────────────────────────────────────────────────

    public CategoryResponse unassignFromWarehouse(Long warehouseId, Long categoryId) {
        Category category = getCategory(categoryId);

        if (category.getWarehouse() == null || !category.getWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException("Cette catégorie n'est pas affectée à cet entrepôt");
        }

        long productCount = categoryRepository.countProducts(categoryId);
        if (productCount > 0) {
            throw new BusinessException(
                "Impossible de désaffecter la catégorie \"" + category.getName()
                + "\" : " + productCount + " produit(s) y sont rattachés dans cet entrepôt.");
        }

        category.setWarehouse(null);
        return toResponse(categoryRepository.save(category));
    }

    // ─────────────────────────────────────────────────────────────
    // SUPPRESSION
    // ─────────────────────────────────────────────────────────────

    public void delete(Long id) {
        Category category = getCategory(id);
        User caller = currentUserEntity();

        boolean isManager = "Gestionnaire d'entrepôt".equals(caller.getRole().getName());
        if (isManager && isAdminDefined(category)) {
            throw new BusinessException("Vous ne pouvez pas supprimer une catégorie définie par l'Administrateur");
        }

        long productCount = categoryRepository.countProducts(id);
        if (productCount > 0) {
            throw new BusinessException(
                "Impossible de supprimer la catégorie \"" + category.getName()
                + "\" : " + productCount + " produit(s) y sont rattachés.");
        }

        categoryRepository.delete(category);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private boolean isAdminDefined(Category category) {
        return "Administrateur".equals(category.getCreatedBy().getRole().getName());
    }

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
    }

    private User currentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(details.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    public CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .productCount((int) categoryRepository.countProducts(c.getId()))
                .warehouseId(c.getWarehouse() != null ? c.getWarehouse().getId() : null)
                .warehouseName(c.getWarehouse() != null ? c.getWarehouse().getName() : null)
                .createdByUsername(c.getCreatedBy().getUsername())
                .isAdminDefined(isAdminDefined(c))
                .isAssigned(c.getWarehouse() != null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
