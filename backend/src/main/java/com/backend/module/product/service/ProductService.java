package com.backend.module.product.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.category.entity.Category;
import com.backend.module.category.repository.CategoryRepository;
import com.backend.module.product.dto.CreateProductRequest;
import com.backend.module.product.dto.ProductResponse;
import com.backend.module.product.dto.UpdateProductRequest;
import com.backend.module.product.entity.Product;
import com.backend.module.product.repository.ProductRepository;
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
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // LECTURE — Admin : catalogue global
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(String search, Long categoryId, Boolean active, Pageable pageable) {
        return productRepository.findAllWithFilters(search, categoryId, active, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — contexte entrepôt (admin + gestionnaire)
    // Retourne les produits actifs dont la catégorie est affectée à cet entrepôt
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProductResponse> findByWarehouse(Long warehouseId, String search, Long categoryId, Pageable pageable) {
        getWarehouse(warehouseId);
        return productRepository.findByWarehouseContext(warehouseId, search, categoryId, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — select produits pour création de stock
    // Filtre par catégorie de la zone (si renseignée)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductResponse> findForStockSelect(Long warehouseId, Long categoryId) {
        getWarehouse(warehouseId);
        return productRepository.findForStockSelect(warehouseId, categoryId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(getProduct(id));
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — Admin (catalogue global)
    // Pas de contrainte d'entrepôt — la catégorie peut être libre ou affectée
    // ─────────────────────────────────────────────────────────────

    public ProductResponse create(CreateProductRequest req) {
        Category category = getCategoryById(req.getCategoryId());
        validateProductName(req.getName(), req.getCategoryId(), null);

        User creator = currentUserEntity();

        Product product = Product.builder()
                .name(req.getName().trim())
                .description(req.getDescription())
                .reference(generateReference())
                .barcode(generateBarcode())
                .purchasePrice(req.getPurchasePrice())
                .salePrice(req.getSalePrice())
                .weight(req.getWeight())
                .volume(req.getVolume())
                .isActive(true)
                .category(category)
                .createdBy(creator)
                .build();

        if (req.getWarehouseIds() != null && !req.getWarehouseIds().isEmpty()) {
            for (Long wId : req.getWarehouseIds()) {
                Warehouse w = getWarehouse(wId);
                product.getWarehouses().add(w);
            }
        }

        return toResponse(productRepository.save(product));
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — entrepôts ayant la catégorie donnée (pour checkboxes admin)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Long> findWarehouseIdsByCategory(Long categoryId) {
        return productRepository.findWarehouseIdsByCategoryId(categoryId);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — Gestionnaire (contexte entrepôt)
    // La catégorie DOIT être affectée à l'entrepôt du gestionnaire
    // ─────────────────────────────────────────────────────────────

    public ProductResponse createInWarehouse(Long warehouseId, CreateProductRequest req) {
        getWarehouse(warehouseId);
        User creator = currentUserEntity();

        // Vérification de périmètre — uniquement pour le gestionnaire
        // L'admin peut créer dans n'importe quel entrepôt
        if (isManager(creator)) {
            if (creator.getAssignedWarehouse() == null
                    || !creator.getAssignedWarehouse().getId().equals(warehouseId)) {
                throw new BusinessException("Vous ne pouvez gérer que les produits de votre propre entrepôt");
            }
        }

        Category category = getCategoryById(req.getCategoryId());

        // La catégorie doit être affectée à cet entrepôt
        if (category.getWarehouse() == null || !category.getWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException(
                "La catégorie \"" + category.getName() + "\" n'est pas affectée à votre entrepôt");
        }

        validateProductName(req.getName(), req.getCategoryId(), null);

        Warehouse warehouse = getWarehouse(warehouseId);

        Product product = Product.builder()
                .name(req.getName().trim())
                .description(req.getDescription())
                .reference(generateReference())
                .barcode(generateBarcode())
                .purchasePrice(req.getPurchasePrice())
                .salePrice(req.getSalePrice())
                .weight(req.getWeight())
                .volume(req.getVolume())
                .isActive(true)
                .category(category)
                .createdBy(creator)
                .build();


        // Lier le produit à l'entrepôt — indispensable pour que findByWarehouseContext le remonte
        product.getWarehouses().add(warehouse);


        return toResponse(productRepository.save(product));
    }

    // ─────────────────────────────────────────────────────────────
    // MISE À JOUR AFFECTATION ENTREPÔTS (Admin)
    // ─────────────────────────────────────────────────────────────

    public ProductResponse updateWarehouses(Long id, Set<Long> warehouseIds) {
        Product product = getProduct(id);
        product.getWarehouses().clear();
        for (Long wId : warehouseIds) {
            product.getWarehouses().add(getWarehouse(wId));
        }
        return toResponse(productRepository.save(product));
    }

    // ─────────────────────────────────────────────────────────────
    // MODIFICATION
    // Admin → tous les produits
    // Gestionnaire → uniquement ses propres produits
    // ─────────────────────────────────────────────────────────────

    public ProductResponse update(Long id, UpdateProductRequest req) {
        Product product = getProduct(id);
        User caller = currentUserEntity();

        if (isManager(caller) && isAdminDefined(product)) {
            throw new BusinessException(
                "Vous ne pouvez pas modifier un produit créé par l'Administrateur");
        }

        if (req.getName() != null && !req.getName().isBlank()) {
            String trimmed = req.getName().trim();
            if (!trimmed.equalsIgnoreCase(product.getName())) {
                validateProductName(trimmed, product.getCategory().getId(), id);
            }
            product.setName(trimmed);
        }
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getPurchasePrice() != null) product.setPurchasePrice(req.getPurchasePrice());
        if (req.getSalePrice() != null)     product.setSalePrice(req.getSalePrice());
        if (req.getWeight() != null)        product.setWeight(req.getWeight());
        if (req.getVolume() != null)        product.setVolume(req.getVolume());

        return toResponse(productRepository.save(product));
    }

    // ─────────────────────────────────────────────────────────────
    // TOGGLE (suppression logique)
    // Admin → tous les produits
    // Gestionnaire → ses produits uniquement, et seulement si pas de stock actif
    // ─────────────────────────────────────────────────────────────

    public ProductResponse toggle(Long id) {
        Product product = getProduct(id);
        User caller = currentUserEntity();

        if (isManager(caller)) {
            if (isAdminDefined(product)) {
                throw new BusinessException(
                    "Vous ne pouvez pas désactiver un produit créé par l'Administrateur");
            }
            // Bloquer la désactivation si le produit a du stock actif
            if (!product.getIsActive()) {
                long activeStock = productRepository.countActiveStock(id);
                if (activeStock > 0) {
                    throw new BusinessException(
                        "Impossible de désactiver \"" + product.getName()
                        + "\" : ce produit a du stock actif dans " + activeStock + " zone(s)");
                }
            }
        }

        product.setIsActive(!product.getIsActive());
        return toResponse(productRepository.save(product));
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────────

    private Product getProduct(Long id) {
        return productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
    }

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private User currentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(details.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private boolean isManager(User user) {
        return "Gestionnaire d'entrepôt".equals(user.getRole().getName());
    }

    private boolean isAdminDefined(Product product) {
        return "Administrateur".equals(product.getCreatedBy().getRole().getName());
    }

    private void validateProductName(String name, Long categoryId, Long excludeId) {
        boolean exists = excludeId == null
                ? productRepository.existsByNameIgnoreCaseAndCategoryId(name, categoryId)
                : productRepository.existsByNameIgnoreCaseAndCategoryIdAndIdNot(name, categoryId, excludeId);
        if (exists) {
            throw new BusinessException(
                "Un produit \"" + name + "\" existe déjà dans cette catégorie");
        }
    }

    /**
     * Génère une référence unique : PRD-{6 chiffres aléatoires}.
     * Boucle jusqu'à trouver une référence libre (collision rarissime).
     */
    private String generateReference() {
        String ref;
        do {
            int rand = (int) (Math.random() * 900_000) + 100_000;
            ref = "PRD-" + rand;
        } while (productRepository.existsByReference(ref));
        return ref;
    }

    /**
     * Génère un code-barres unique : PRD-{timestamp}-{4 hex aléatoires}.
     */
    private String generateBarcode() {
        String barcode;
        do {
            String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            barcode = "PRD-" + System.currentTimeMillis() + "-" + suffix;
        } while (productRepository.existsByBarcode(barcode));
        return barcode;
    }

    public ProductResponse toResponse(Product p) {
        Set<Long> warehouseIds = p.getWarehouses().stream()
                .map(Warehouse::getId)
                .collect(java.util.stream.Collectors.toSet());
        return ProductResponse.builder()
                .id(p.getId())
                .reference(p.getReference())
                .barcode(p.getBarcode())
                .name(p.getName())
                .description(p.getDescription())
                .purchasePrice(p.getPurchasePrice())
                .salePrice(p.getSalePrice())
                .weight(p.getWeight())
                .volume(p.getVolume())
                .isActive(p.getIsActive())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .createdByUsername(p.getCreatedBy().getUsername())
                .isAdminDefined(isAdminDefined(p))
                .warehouseIds(warehouseIds)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
