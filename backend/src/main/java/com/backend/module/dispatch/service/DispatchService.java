package com.backend.module.dispatch.service;

import com.backend.module.auditlog.annotation.Auditable;
import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.dispatch.dto.CreateDispatchRequest;
import com.backend.module.dispatch.dto.DispatchResponse;
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
    private final com.backend.module.stock.service.CapacityService capacityService;

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
    // CRÉATION — Magasinier crée ET valide le bon de sortie directement
    //
    // Le stock est décrémenté immédiatement. Le magasinier est
    // responsable de l'opération via ses rapports d'activité.
    // ─────────────────────────────────────────────────────────────

    @Auditable(module = "dispatch", action = "CREATE", entity = "Dispatch",
               description = "Bon de sortie créé et validé par le magasinier")
    public DispatchResponse create(Long warehouseId, CreateDispatchRequest req) {
        Warehouse warehouse = getWarehouse(warehouseId);
        User creator = currentUserEntity();

        Dispatch dispatch = Dispatch.builder()
                .dispatchNumber(generateDispatchNumber())
                .status(DispatchStatus.VALIDATED)    // immédiatement validé
                .note(req.getNote())
                .clientFirstName(req.getClientFirstName())
                .clientLastName(req.getClientLastName())
                .clientPhone(req.getClientPhone())
                .deliveryAddress(req.getDeliveryAddress())
                .warehouse(warehouse)
                .createdBy(creator)
                .validatedBy(creator)               // le magasinier est aussi le validateur
                .validatedAt(LocalDateTime.now())
                .build();

        for (CreateDispatchRequest.DispatchLineRequest lineReq : req.getLines()) {
            Product product = productRepository.findByIdWithDetails(lineReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produit introuvable : " + lineReq.getProductId()));

            Zone zone = getZone(lineReq.getZoneId(), warehouseId);

            // Vérifier que le stock est suffisant
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

            // Décrémenter le stock immédiatement
            stock.setQuantityAvailable(stock.getQuantityAvailable() - lineReq.getQuantityRequested());
            stockRepository.save(stock);

            // StockMovement EXIT
            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .zone(zone)
                    .quantity(lineReq.getQuantityRequested())
                    .movementType(MovementType.EXIT)
                    .referenceDoc(dispatch.getDispatchNumber())
                    .note("Sortie de stock — bon " + dispatch.getDispatchNumber())
                    .createdBy(creator)
                    .build();
            stockMovementRepository.save(movement);

            DispatchLine line = DispatchLine.builder()
                    .dispatch(dispatch)
                    .product(product)
                    .zone(zone)
                    .quantityRequested(lineReq.getQuantityRequested())
                    .note(lineReq.getNote())
                    .build();

            dispatch.getLines().add(line);
        }

        DispatchResponse response = toResponse(dispatchRepository.save(dispatch));

        // Recalcul capacité après décrément
        capacityService.recalculate(warehouse);

        return response;
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

    public Dispatch getDispatchForBordereau(Long dispatchId, Long warehouseId) {
        return getDispatchInWarehouse(dispatchId, warehouseId);
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
                .clientFirstName(d.getClientFirstName())
                .clientLastName(d.getClientLastName())
                .clientPhone(d.getClientPhone())
                .deliveryAddress(d.getDeliveryAddress())
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
