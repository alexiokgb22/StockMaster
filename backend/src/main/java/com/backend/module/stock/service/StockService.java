package com.backend.module.stock.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.product.entity.Product;
import com.backend.module.product.repository.ProductRepository;
import com.backend.module.shared.enums.MovementType;
import com.backend.module.stock.dto.CreateStockRequest;
import com.backend.module.stock.dto.StockResponse;
import com.backend.module.stock.dto.UpdateStockRequest;
import com.backend.module.stock.entity.Stock;
import com.backend.module.stock.repository.StockRepository;
import com.backend.module.stockmovement.dto.StockMovementResponse;
import com.backend.module.stockmovement.entity.StockMovement;
import com.backend.module.stockmovement.repository.StockMovementRepository;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
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
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final ZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final CapacityService capacityService;

    // ─────────────────────────────────────────────────────────────
    // LECTURE — stocks d'un entrepôt
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<StockResponse> findByWarehouse(
            Long warehouseId, Long zoneId, Long categoryId,
            Boolean belowMin, String search, Pageable pageable) {
        getWarehouse(warehouseId);
        return stockRepository
                .findByWarehouseWithFilters(warehouseId, zoneId, categoryId, belowMin, search, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — historique des mouvements
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> findMovements(
            Long warehouseId, MovementType movementType,
            Long productId, Long zoneId, Pageable pageable) {
        getWarehouse(warehouseId);
        return movementRepository
                .findByWarehouseWithFilters(warehouseId, movementType, productId, zoneId, pageable)
                .map(this::toMovementResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — initialisation manuelle du stock
    //
    // Règles :
    //   1. La ligne (product, warehouse, zone) doit être unique.
    //   2. La zone doit appartenir à l'entrepôt.
    //   3. Si zone.category != null → product.category doit être identique.
    //   4. Génère un StockMovement ADJUSTMENT pour tracer l'init.
    //   5. Recalcule warehouse.usedCapacity si product.volume != null.
    // ─────────────────────────────────────────────────────────────

    public StockResponse create(Long warehouseId, CreateStockRequest req) {
        Warehouse warehouse = getWarehouse(warehouseId);
        Zone zone = getZone(req.getZoneId(), warehouseId);
        Product product = getProduct(req.getProductId());
        User creator = currentUserEntity();

        // Règle 3 : contrainte catégorie
        if (zone.getCategory() != null
                && !zone.getCategory().getId().equals(product.getCategory().getId())) {
            throw new BusinessException(
                "Ce produit est de catégorie \"" + product.getCategory().getName()
                + "\" mais la zone attend la catégorie \""
                + zone.getCategory().getName() + "\"");
        }

        // Règle 1 : unicité
        if (stockRepository.existsByProductIdAndWarehouseIdAndZoneId(
                product.getId(), warehouseId, zone.getId())) {
            throw new BusinessException(
                "Une ligne de stock existe déjà pour ce produit dans cette zone");
        }

        Stock stock = Stock.builder()
                .product(product)
                .warehouse(warehouse)
                .zone(zone)
                .quantityAvailable(req.getInitialQuantity())
                .quantityReserved(0)
                .quantityInTransit(0)
                .minStock(req.getMinStock())
                .maxStock(req.getMaxStock())
                .build();

        Stock saved = stockRepository.save(stock);

        // Règle 4 : tracer l'initialisation
        if (req.getInitialQuantity() > 0) {
            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .zone(zone)
                    .quantity(req.getInitialQuantity())
                    .movementType(MovementType.ADJUSTMENT)
                    .referenceDoc("INIT-" + saved.getId())
                    .note(req.getNote() != null ? req.getNote() : "Initialisation du stock")
                    .createdBy(creator)
                    .build();
            movementRepository.save(movement);
        }

        // Règle 5 : recalcul usedCapacity
        capacityService.recalculate(warehouse);

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    // MODIFICATION — quantités et seuils
    // Génère un StockMovement ADJUSTMENT si quantityAvailable change
    // ─────────────────────────────────────────────────────────────

    public StockResponse update(Long warehouseId, Long stockId, UpdateStockRequest req) {
        getWarehouse(warehouseId);
        Stock stock = getStock(stockId, warehouseId);
        User caller = currentUserEntity();

        if (req.getQuantityAvailable() != null
                && !req.getQuantityAvailable().equals(stock.getQuantityAvailable())) {
            int delta = req.getQuantityAvailable() - stock.getQuantityAvailable();

            StockMovement movement = StockMovement.builder()
                    .product(stock.getProduct())
                    .warehouse(stock.getWarehouse())
                    .zone(stock.getZone())
                    .quantity(Math.abs(delta))
                    .movementType(MovementType.ADJUSTMENT)
                    .referenceDoc("ADJ-" + stockId)
                    .note(req.getNote() != null ? req.getNote() : "Ajustement manuel")
                    .createdBy(caller)
                    .build();
            movementRepository.save(movement);

            stock.setQuantityAvailable(req.getQuantityAvailable());
        }

        if (req.getMinStock() != null) stock.setMinStock(req.getMinStock());
        if (req.getMaxStock() != null) stock.setMaxStock(req.getMaxStock());

        Stock saved = stockRepository.save(stock);

        // Recalcul capacité si volume renseigné
        capacityService.recalculate(stock.getWarehouse());

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────────

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private Zone getZone(Long zoneId, Long warehouseId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + zoneId));
        if (!zone.getWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException("Cette zone n'appartient pas à cet entrepôt");
        }
        return zone;
    }

    private Product getProduct(Long id) {
        return productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
    }

    private Stock getStock(Long stockId, Long warehouseId) {
        Stock stock = stockRepository.findByIdWithDetails(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne de stock introuvable : " + stockId));
        if (!stock.getWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException("Cette ligne de stock n'appartient pas à cet entrepôt");
        }
        return stock;
    }

    private User currentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(details.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private StockResponse toResponse(Stock s) {
        boolean belowMin = s.getMinStock() != null
                && s.getQuantityAvailable() < s.getMinStock();
        return StockResponse.builder()
                .id(s.getId())
                .quantityAvailable(s.getQuantityAvailable())
                .quantityReserved(s.getQuantityReserved())
                .quantityInTransit(s.getQuantityInTransit())
                .minStock(s.getMinStock())
                .maxStock(s.getMaxStock())
                .isBelowMin(belowMin)
                .productId(s.getProduct().getId())
                .productName(s.getProduct().getName())
                .productReference(s.getProduct().getReference())
                .productBarcode(s.getProduct().getBarcode())
                .productVolume(s.getProduct().getVolume())
                .categoryId(s.getProduct().getCategory().getId())
                .categoryName(s.getProduct().getCategory().getName())
                .warehouseId(s.getWarehouse().getId())
                .warehouseName(s.getWarehouse().getName())
                .zoneId(s.getZone().getId())
                .zoneName(s.getZone().getName())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private StockMovementResponse toMovementResponse(StockMovement sm) {
        return StockMovementResponse.builder()
                .id(sm.getId())
                .quantity(sm.getQuantity())
                .movementType(sm.getMovementType())
                .referenceDoc(sm.getReferenceDoc())
                .note(sm.getNote())
                .productId(sm.getProduct().getId())
                .productName(sm.getProduct().getName())
                .warehouseId(sm.getWarehouse().getId())
                .warehouseName(sm.getWarehouse().getName())
                .zoneId(sm.getZone().getId())
                .zoneName(sm.getZone().getName())
                .createdByUsername(sm.getCreatedBy().getUsername())
                .createdAt(sm.getCreatedAt())
                .build();
    }
}
