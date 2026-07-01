package com.backend.module.reception.service;

import com.backend.module.auditlog.annotation.Auditable;
import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.purchaseorder.entity.PurchaseOrder;
import com.backend.module.purchaseorder.repository.PurchaseOrderRepository;
import com.backend.module.purchaseorderline.entity.PurchaseOrderLine;
import com.backend.module.purchaseorderline.repository.PurchaseOrderLineRepository;
import com.backend.module.reception.dto.CreateReceptionRequest;
import com.backend.module.reception.dto.ReceptionResponse;
import com.backend.module.reception.entity.Reception;
import com.backend.module.reception.entity.ReceptionLine;
import com.backend.module.reception.repository.ReceptionRepository;
import com.backend.module.shared.enums.MovementType;
import com.backend.module.shared.enums.PurchaseOrderStatus;
import com.backend.module.shared.enums.ReceptionStatus;
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
public class ReceptionService {

    private final ReceptionRepository receptionRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository zoneRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final com.backend.module.stock.service.CapacityService capacityService;

    // ─────────────────────────────────────────────────────────────
    // LECTURE — liste paginée pour un entrepôt
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ReceptionResponse> findByWarehouse(
            Long warehouseId, ReceptionStatus status, Pageable pageable) {
        getWarehouse(warehouseId);
        return receptionRepository
                .findByWarehouseWithFilters(warehouseId, status, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — détail d'un bon
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ReceptionResponse findById(Long warehouseId, Long receptionId) {
        Reception reception = getReception(receptionId);
        if (!reception.getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException("Bon de réception introuvable dans cet entrepôt : " + receptionId);
        }
        return toResponse(reception);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — commandes DELIVERED disponibles pour réception
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<com.backend.module.purchaseorder.dto.PurchaseOrderResponse> findDeliverable(
            Long warehouseId, Pageable pageable) {
        getWarehouse(warehouseId);
        return purchaseOrderRepository
                .findByWarehouseWithFilters(warehouseId, PurchaseOrderStatus.DELIVERED, pageable)
                .map(this::toPurchaseOrderResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — Magasinier crée ET valide le bon en une seule opération
    //
    // Le magasinier est physiquement présent à la réception et fait
    // le contrôle qualité lui-même. Le stock est mis à jour immédiatement.
    // Le gestionnaire consulte l'historique via ses rapports d'activité.
    // ─────────────────────────────────────────────────────────────

    @Auditable(module = "reception", action = "CREATE", entity = "Reception",
               description = "Bon de réception créé et validé par le magasinier")
    public ReceptionResponse create(Long warehouseId, CreateReceptionRequest req) {
        Warehouse warehouse = getWarehouse(warehouseId);
        User creator = currentUserEntity();

        PurchaseOrder order = purchaseOrderRepository.findByIdWithDetails(req.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande introuvable : " + req.getPurchaseOrderId()));

        if (!order.getWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException("Cette commande ne concerne pas votre entrepôt");
        }
        if (order.getStatus() != PurchaseOrderStatus.DELIVERED) {
            throw new BusinessException("Seules les commandes livrées peuvent être réceptionnées");
        }

        // Vérifier qu'aucun bon n'existe déjà pour cette commande
        if (receptionRepository.existsByPurchaseOrderIdAndStatus(
                order.getId(), ReceptionStatus.VALIDATED)) {
            throw new BusinessException(
                    "Un bon de réception a déjà été créé pour cette commande");
        }

        Reception reception = Reception.builder()
                .receptionNumber(generateReceptionNumber())
                .status(ReceptionStatus.VALIDATED)   // immédiatement validé
                .note(req.getNote())
                .purchaseOrder(order)
                .warehouse(warehouse)
                .createdBy(creator)
                .validatedBy(creator)                // le magasinier est aussi le validateur
                .validatedAt(LocalDateTime.now())
                .build();

        // Construire les lignes ET mettre à jour le stock directement
        for (CreateReceptionRequest.ReceptionLineRequest lineReq : req.getLines()) {
            PurchaseOrderLine pol = purchaseOrderLineRepository.findById(lineReq.getPurchaseOrderLineId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Ligne de commande introuvable : " + lineReq.getPurchaseOrderLineId()));

            if (!pol.getPurchaseOrder().getId().equals(order.getId())) {
                throw new BusinessException("La ligne " + lineReq.getPurchaseOrderLineId()
                        + " n'appartient pas à cette commande");
            }

            Zone zone = getZone(lineReq.getZoneId(), warehouseId);

            ReceptionLine line = ReceptionLine.builder()
                    .reception(reception)
                    .purchaseOrderLine(pol)
                    .quantityExpected(pol.getQuantity())
                    .quantityReceived(lineReq.getQuantityReceived())
                    .zone(zone)
                    .note(lineReq.getNote())
                    .build();

            reception.getLines().add(line);
        }

        Reception saved = receptionRepository.save(reception);

        // Mettre à jour le stock pour chaque ligne
        for (ReceptionLine line : saved.getLines()) {
            if (line.getQuantityReceived() == 0) continue;

            Long productId = line.getPurchaseOrderLine().getProduct().getId();
            Long zoneId    = line.getZone().getId();

            Stock stock = stockRepository
                    .findByProductIdAndWarehouseIdAndZoneId(productId, warehouseId, zoneId)
                    .orElseGet(() -> Stock.builder()
                            .product(line.getPurchaseOrderLine().getProduct())
                            .warehouse(warehouse)
                            .zone(line.getZone())
                            .quantityAvailable(0)
                            .quantityReserved(0)
                            .quantityInTransit(0)
                            .build());

            stock.setQuantityAvailable(stock.getQuantityAvailable() + line.getQuantityReceived());
            stockRepository.save(stock);

            // StockMovement ENTRY
            StockMovement movement = StockMovement.builder()
                    .product(line.getPurchaseOrderLine().getProduct())
                    .warehouse(warehouse)
                    .zone(line.getZone())
                    .quantity(line.getQuantityReceived())
                    .movementType(MovementType.ENTRY)
                    .referenceDoc(saved.getReceptionNumber())
                    .note("Réception commande " + order.getOrderNumber())
                    .createdBy(creator)
                    .build();
            stockMovementRepository.save(movement);

            // Mettre à jour receivedQty sur la ligne de commande
            PurchaseOrderLine pol = line.getPurchaseOrderLine();
            pol.setReceivedQty(pol.getReceivedQty() + line.getQuantityReceived());
            purchaseOrderLineRepository.save(pol);
        }

        // Clôturer la commande
        order.setStatus(PurchaseOrderStatus.CLOSED);
        purchaseOrderRepository.save(order);

        // Recalcul de la capacité
        capacityService.recalculate(warehouse);

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private String generateReceptionNumber() {
        String prefix = "REC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String number;
        do {
            int rand = (int) (Math.random() * 9000) + 1000;
            number = prefix + rand;
        } while (receptionRepository.existsByReceptionNumber(number));
        return number;
    }

    private Reception getReception(Long id) {
        return receptionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de réception introuvable : " + id));
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

    public ReceptionResponse toResponse(Reception r) {
        List<ReceptionResponse.ReceptionLineResponse> lines = r.getLines().stream()
                .map(l -> ReceptionResponse.ReceptionLineResponse.builder()
                        .id(l.getId())
                        .purchaseOrderLineId(l.getPurchaseOrderLine().getId())
                        .productId(l.getPurchaseOrderLine().getProduct().getId())
                        .productName(l.getPurchaseOrderLine().getProduct().getName())
                        .productReference(l.getPurchaseOrderLine().getProduct().getReference())
                        .categoryName(l.getPurchaseOrderLine().getProduct().getCategory().getName())
                        .quantityExpected(l.getQuantityExpected())
                        .quantityReceived(l.getQuantityReceived())
                        .gap(l.getGap())
                        .note(l.getNote())
                        .zoneId(l.getZone().getId())
                        .zoneName(l.getZone().getName())
                        .build())
                .toList();

        int gapCount = (int) r.getLines().stream()
                .filter(l -> l.getGap() != 0)
                .count();

        return ReceptionResponse.builder()
                .id(r.getId())
                .receptionNumber(r.getReceptionNumber())
                .status(r.getStatus())
                .note(r.getNote())
                .rejectionReason(r.getRejectionReason())
                .validatedAt(r.getValidatedAt())
                .purchaseOrderId(r.getPurchaseOrder().getId())
                .purchaseOrderNumber(r.getPurchaseOrder().getOrderNumber())
                .supplierId(r.getPurchaseOrder().getSupplier() != null
                        ? r.getPurchaseOrder().getSupplier().getId() : null)
                .supplierName(r.getPurchaseOrder().getSupplier() != null
                        ? r.getPurchaseOrder().getSupplier().getName() : null)
                .warehouseId(r.getWarehouse().getId())
                .warehouseName(r.getWarehouse().getName())
                .createdByUsername(r.getCreatedBy().getUsername())
                .validatedByUsername(r.getValidatedBy() != null
                        ? r.getValidatedBy().getUsername() : null)
                .lines(lines)
                .gapCount(gapCount)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    // Réutilise le mapper du PurchaseOrderService pour la liste des livrables
    private com.backend.module.purchaseorder.dto.PurchaseOrderResponse toPurchaseOrderResponse(
            PurchaseOrder o) {
        List<com.backend.module.purchaseorder.dto.PurchaseOrderResponse.OrderLineResponse> lines =
                o.getLines().stream()
                        .map(l -> com.backend.module.purchaseorder.dto.PurchaseOrderResponse
                                .OrderLineResponse.builder()
                                .id(l.getId())
                                .productId(l.getProduct().getId())
                                .productName(l.getProduct().getName())
                                .productReference(l.getProduct().getReference())
                                .categoryName(l.getProduct().getCategory().getName())
                                .quantity(l.getQuantity())
                                .receivedQty(l.getReceivedQty())
                                .unitPrice(l.getUnitPrice())
                                .build())
                        .toList();

        return com.backend.module.purchaseorder.dto.PurchaseOrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(o.getStatus())
                .orderDate(o.getOrderDate())
                .expectedDate(o.getExpectedDate())
                .note(o.getNote())
                .totalAmount(o.getTotalAmount())
                .warehouseId(o.getWarehouse().getId())
                .warehouseName(o.getWarehouse().getName())
                .supplierId(o.getSupplier() != null ? o.getSupplier().getId() : null)
                .supplierName(o.getSupplier() != null ? o.getSupplier().getName() : null)
                .supplierContactName(o.getSupplier() != null ? o.getSupplier().getContactName() : null)
                .supplierPhone(o.getSupplier() != null ? o.getSupplier().getPhone() : null)
                .supplierEmail(o.getSupplier() != null ? o.getSupplier().getEmail() : null)
                .createdByUsername(o.getCreatedBy().getUsername())
                .lines(lines)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
