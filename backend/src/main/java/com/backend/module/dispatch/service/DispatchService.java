package com.backend.module.dispatch.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.dispatch.dto.CreateDispatchRequest;
import com.backend.module.dispatch.dto.DispatchResponse;
import com.backend.module.dispatch.dto.RejectDispatchRequest;
import com.backend.module.dispatch.entity.Dispatch;
import com.backend.module.dispatch.entity.DispatchLine;
import com.backend.module.dispatch.repository.DispatchRepository;
import com.backend.module.product.entity.Product;
import com.backend.module.product.repository.ProductRepository;
import com.backend.module.shared.enums.DispatchStatus;
import com.backend.module.shared.enums.MovementType;
import com.backend.module.stock.entity.Stock;
import com.backend.module.stock.repository.StockRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final ZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // LECTURE — liste paginée pour un entrepôt
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<DispatchResponse> findByWarehouse(
            Long warehouseId, DispatchStatus status, Pageable pageable) {
        getWarehouse(warehouseId);
        return dispatchRepository
                .findByWarehouseWithFilters(warehouseId, status, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — détail d'un bon
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DispatchResponse findById(Long warehouseId, Long dispatchId) {
        Dispatch dispatch = getDispatchInWarehouse(dispatchId, warehouseId);
        return toResponse(dispatch);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — compteur PENDING pour le badge gestionnaire
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public long countPending(Long warehouseId) {
        return dispatchRepository.countByWarehouseIdAndStatus(warehouseId, DispatchStatus.PENDING);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — Magasinier crée un bon de sortie (PENDING)
    //
    // Règles :
    //   1. Vérifier que le stock existe et est suffisant pour chaque ligne.
    //   2. Pas de décrément immédiat — le gestionnaire valide avant.
    //   3. Chaque (produit, zone) doit être unique dans le bon.
    // ─────────────────────────────────────────────────────────────

    public DispatchResponse create(Long warehouseId, CreateDispatchRequest req) {
        Warehouse warehouse = getWarehouse(warehouseId);
        User creator = currentUserEntity();

        Dispatch dispatch = Dispatch.builder()
                .dispatchNumber(generateDispatchNumber())
                .status(DispatchStatus.PENDING)
                .note(req.getNote())
                .warehouse(warehouse)
                .createdBy(creator)
                .build();

        for (CreateDispatchRequest.DispatchLineRequest lineReq : req.getLines()) {
            Product product = productRepository.findByIdWithDetails(lineReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produit introuvable : " + lineReq.getProductId()));

            Zone zone = getZone(lineReq.getZoneId(), warehouseId);

            // Vérifier que le stock existe et est suffisant
            Stock stock = stockRepository
                    .findByProductIdAndWarehouseIdAndZoneId(
                            product.getId(), warehouseId, zone.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Aucun stock trouvé pour " + product.getName()
                            + " dans la zone " + zone.getName()));

            if (stock.getQuantityAvailable() < lineReq.getQuantityRequested()) {
                throw new BusinessException(
                        "Stock insuffisant pour " + product.getName()
                        + " — disponible : " + stock.getQuantityAvailable()
                        + ", demandé : " + lineReq.getQuantityRequested());
            }

            DispatchLine line = DispatchLine.builder()
                    .dispatch(dispatch)
                    .product(product)
                    .zone(zone)
                    .quantityRequested(lineReq.getQuantityRequested())
                    .note(lineReq.getNote())
                    .build();

            dispatch.getLines().add(line);
        }

        return toResponse(dispatchRepository.save(dispatch));
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDATION — Gestionnaire valide (PENDING → VALIDATED)
    // Décrémente le stock + génère StockMovement EXIT
    // ─────────────────────────────────────────────────────────────

    public DispatchResponse validate(Long warehouseId, Long dispatchId) {
        Dispatch dispatch = getDispatchInWarehouse(dispatchId, warehouseId);
        User validator = currentUserEntity();

        if (dispatch.getStatus() != DispatchStatus.PENDING) {
            throw new BusinessException("Seuls les bons PENDING peuvent être validés");
        }

        // Vérifier à nouveau la disponibilité du stock au moment de la validation
        for (DispatchLine line : dispatch.getLines()) {
            Stock stock = stockRepository
                    .findByProductIdAndWarehouseIdAndZoneId(
                            line.getProduct().getId(), warehouseId, line.getZone().getId())
                    .orElseThrow(() -> new BusinessException(
                            "Stock introuvable pour " + line.getProduct().getName()));

            if (stock.getQuantityAvailable() < line.getQuantityRequested()) {
                throw new BusinessException(
                        "Stock insuffisant pour " + line.getProduct().getName()
                        + " au moment de la validation — disponible : "
                        + stock.getQuantityAvailable()
                        + ", demandé : " + line.getQuantityRequested());
            }

            // Décrémenter le stock
            stock.setQuantityAvailable(stock.getQuantityAvailable() - line.getQuantityRequested());
            stockRepository.save(stock);

            // Générer un StockMovement EXIT
            StockMovement movement = StockMovement.builder()
                    .product(line.getProduct())
                    .warehouse(dispatch.getWarehouse())
                    .zone(line.getZone())
                    .quantity(line.getQuantityRequested())
                    .movementType(MovementType.EXIT)
                    .referenceDoc(dispatch.getDispatchNumber())
                    .note("Sortie de stock — bon " + dispatch.getDispatchNumber())
                    .createdBy(validator)
                    .build();
            stockMovementRepository.save(movement);
        }

        dispatch.setStatus(DispatchStatus.VALIDATED);
        dispatch.setValidatedAt(LocalDateTime.now());
        dispatch.setValidatedBy(validator);

        return toResponse(dispatchRepository.save(dispatch));
    }

    // ─────────────────────────────────────────────────────────────
    // REJET — Gestionnaire rejette (PENDING → REJECTED)
    // Aucune modification de stock
    // ─────────────────────────────────────────────────────────────

    public DispatchResponse reject(Long warehouseId, Long dispatchId, RejectDispatchRequest req) {
        Dispatch dispatch = getDispatchInWarehouse(dispatchId, warehouseId);
        User validator = currentUserEntity();

        if (dispatch.getStatus() != DispatchStatus.PENDING) {
            throw new BusinessException("Seuls les bons PENDING peuvent être rejetés");
        }

        dispatch.setStatus(DispatchStatus.REJECTED);
        dispatch.setRejectionReason(req != null ? req.getReason() : null);
        dispatch.setValidatedAt(LocalDateTime.now());
        dispatch.setValidatedBy(validator);

        return toResponse(dispatchRepository.save(dispatch));
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────────

    private String generateDispatchNumber() {
        String prefix = "DSP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String number;
        do {
            int rand = (int) (Math.random() * 9000) + 1000;
            number = prefix + rand;
        } while (dispatchRepository.existsByDispatchNumber(number));
        return number;
    }

    private Dispatch getDispatch(Long id) {
        return dispatchRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de sortie introuvable : " + id));
    }

    private Dispatch getDispatchInWarehouse(Long dispatchId, Long warehouseId) {
        Dispatch dispatch = getDispatch(dispatchId);
        if (!dispatch.getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException(
                    "Bon de sortie introuvable dans cet entrepôt : " + dispatchId);
        }
        return dispatch;
    }

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

    private User currentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(details.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    public DispatchResponse toResponse(Dispatch d) {
        // Charger le stock disponible par ligne pour affichage informatif
        List<DispatchResponse.DispatchLineResponse> lines = d.getLines().stream()
                .map(l -> {
                    Integer stockAvailable = stockRepository
                            .findByProductIdAndWarehouseIdAndZoneId(
                                    l.getProduct().getId(),
                                    d.getWarehouse().getId(),
                                    l.getZone().getId())
                            .map(Stock::getQuantityAvailable)
                            .orElse(0);

                    return DispatchResponse.DispatchLineResponse.builder()
                            .id(l.getId())
                            .productId(l.getProduct().getId())
                            .productName(l.getProduct().getName())
                            .productReference(l.getProduct().getReference())
                            .categoryName(l.getProduct().getCategory().getName())
                            .quantityRequested(l.getQuantityRequested())
                            .note(l.getNote())
                            .zoneId(l.getZone().getId())
                            .zoneName(l.getZone().getName())
                            .stockAvailable(stockAvailable)
                            .build();
                })
                .toList();

        return DispatchResponse.builder()
                .id(d.getId())
                .dispatchNumber(d.getDispatchNumber())
                .status(d.getStatus())
                .note(d.getNote())
                .rejectionReason(d.getRejectionReason())
                .validatedAt(d.getValidatedAt())
                .warehouseId(d.getWarehouse().getId())
                .warehouseName(d.getWarehouse().getName())
                .createdByUsername(d.getCreatedBy().getUsername())
                .validatedByUsername(d.getValidatedBy() != null
                        ? d.getValidatedBy().getUsername() : null)
                .lines(lines)
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
