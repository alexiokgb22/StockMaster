package com.backend.module.purchaseorder.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.product.entity.Product;
import com.backend.module.product.repository.ProductRepository;
import com.backend.module.purchaseorder.dto.CreatePurchaseOrderRequest;
import com.backend.module.purchaseorder.dto.PurchaseOrderResponse;
import com.backend.module.purchaseorder.dto.ValidatePurchaseOrderRequest;
import com.backend.module.purchaseorder.entity.PurchaseOrder;
import com.backend.module.purchaseorder.repository.PurchaseOrderRepository;
import com.backend.module.purchaseorderline.entity.PurchaseOrderLine;
import com.backend.module.shared.enums.PurchaseOrderStatus;
import com.backend.module.supplier.entity.Supplier;
import com.backend.module.supplier.repository.SupplierRepository;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // LECTURE — Gestionnaire : commandes de son entrepôt
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> findByWarehouse(
            Long warehouseId, PurchaseOrderStatus status, Pageable pageable) {
        getWarehouse(warehouseId);
        return purchaseOrderRepository
                .findByWarehouseWithFilters(warehouseId, status, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — Admin : toutes les commandes
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> findAll(
            PurchaseOrderStatus status, Long warehouseId, Pageable pageable) {
        return purchaseOrderRepository
                .findAllWithFilters(status, warehouseId, pageable)
                .map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — détail d'une commande
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PurchaseOrderResponse findById(Long warehouseId, Long orderId) {
        PurchaseOrder order = getOrder(orderId);
        if (!order.getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException("Commande introuvable dans cet entrepôt : " + orderId);
        }
        return toResponse(order);
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — Admin : historique d'un fournisseur
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> findBySupplierId(Long supplierId, Pageable pageable) {
        // Vérifier que le fournisseur existe
        supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + supplierId));
        return purchaseOrderRepository.findBySupplierId(supplierId, pageable).map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — Gestionnaire crée une demande (DRAFT)
    // ─────────────────────────────────────────────────────────────

    public PurchaseOrderResponse create(Long warehouseId, CreatePurchaseOrderRequest req) {
        Warehouse warehouse = getWarehouse(warehouseId);
        User creator = currentUserEntity();

        // Vérification périmètre gestionnaire
        if (isManager(creator) && !creator.getAssignedWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException("Vous ne pouvez créer des commandes que pour votre propre entrepôt");
        }

        PurchaseOrder order = PurchaseOrder.builder()
                .orderNumber(generateOrderNumber())
                .warehouse(warehouse)
                .createdBy(creator)
                .status(PurchaseOrderStatus.DRAFT)
                .orderDate(LocalDate.now())
                .expectedDate(req.getExpectedDate())
                .note(req.getNote())
                .totalAmount(0.0)
                .build();

        // Construction des lignes
        double total = 0.0;
        for (CreatePurchaseOrderRequest.OrderLineRequest lineReq : req.getLines()) {
            Product product = getProduct(lineReq.getProductId());

            // Le produit doit appartenir à l'entrepôt (catégorie affectée à cet entrepôt)
            if (product.getCategory().getWarehouse() == null
                    || !product.getCategory().getWarehouse().getId().equals(warehouseId)) {
                throw new BusinessException(
                    "Le produit \"" + product.getName() + "\" n'est pas disponible dans cet entrepôt");
            }

            PurchaseOrderLine line = PurchaseOrderLine.builder()
                    .purchaseOrder(order)
                    .product(product)
                    .quantity(lineReq.getQuantity())
                    .unitPrice(lineReq.getUnitPrice())
                    .receivedQty(0)
                    .build();

            order.getLines().add(line);

            if (lineReq.getUnitPrice() != null) {
                total += lineReq.getQuantity() * lineReq.getUnitPrice();
            }
        }

        order.setTotalAmount(total);
        return toResponse(purchaseOrderRepository.save(order));
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDATION — Admin valide et assigne un fournisseur (DRAFT → VALIDATED)
    // ─────────────────────────────────────────────────────────────

    public PurchaseOrderResponse validate(Long warehouseId, Long orderId, ValidatePurchaseOrderRequest req) {
        PurchaseOrder order = getOrderInWarehouse(orderId, warehouseId);

        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BusinessException("Seules les commandes en DRAFT peuvent être validées");
        }

        Supplier supplier = supplierRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + req.getSupplierId()));

        if (!supplier.getIsActive()) {
            throw new BusinessException("Ce fournisseur est inactif");
        }

        order.setSupplier(supplier);
        order.setStatus(PurchaseOrderStatus.VALIDATED);
        if (req.getExpectedDate() != null) order.setExpectedDate(req.getExpectedDate());
        if (req.getNote() != null) order.setNote(req.getNote());

        return toResponse(purchaseOrderRepository.save(order));
    }

    // ─────────────────────────────────────────────────────────────
    // LIVRAISON — Admin marque la commande comme livrée (VALIDATED → DELIVERED)
    // ─────────────────────────────────────────────────────────────

    public PurchaseOrderResponse markDelivered(Long warehouseId, Long orderId) {
        PurchaseOrder order = getOrderInWarehouse(orderId, warehouseId);

        if (order.getStatus() != PurchaseOrderStatus.VALIDATED) {
            throw new BusinessException("Seules les commandes VALIDÉES peuvent être marquées comme livrées");
        }

        order.setStatus(PurchaseOrderStatus.DELIVERED);
        return toResponse(purchaseOrderRepository.save(order));
    }

    // ─────────────────────────────────────────────────────────────
    // CLÔTURE — Gestionnaire confirme la réception (DELIVERED → CLOSED)
    // Appelé après validation du bon de réception (Module 8)
    // ─────────────────────────────────────────────────────────────

    public PurchaseOrderResponse close(Long warehouseId, Long orderId) {
        PurchaseOrder order = getOrderInWarehouse(orderId, warehouseId);

        if (order.getStatus() != PurchaseOrderStatus.DELIVERED) {
            throw new BusinessException("Seules les commandes LIVRÉES peuvent être clôturées");
        }

        order.setStatus(PurchaseOrderStatus.CLOSED);
        return toResponse(purchaseOrderRepository.save(order));
    }

    // ─────────────────────────────────────────────────────────────
    // ANNULATION (DRAFT ou VALIDATED → CANCELLED)
    // ─────────────────────────────────────────────────────────────

    public PurchaseOrderResponse cancel(Long warehouseId, Long orderId) {
        PurchaseOrder order = getOrderInWarehouse(orderId, warehouseId);

        if (order.getStatus() == PurchaseOrderStatus.DELIVERED
                || order.getStatus() == PurchaseOrderStatus.CLOSED) {
            throw new BusinessException("Une commande livrée ou clôturée ne peut pas être annulée");
        }

        order.setStatus(PurchaseOrderStatus.CANCELLED);
        return toResponse(purchaseOrderRepository.save(order));
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private String generateOrderNumber() {
        String prefix = "CMD-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String number;
        do {
            int rand = (int) (Math.random() * 9000) + 1000;
            number = prefix + rand;
        } while (purchaseOrderRepository.existsByOrderNumber(number));
        return number;
    }

    private PurchaseOrder getOrder(Long id) {
        return purchaseOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable : " + id));
    }

    private PurchaseOrder getOrderInWarehouse(Long orderId, Long warehouseId) {
        PurchaseOrder order = getOrder(orderId);
        if (!order.getWarehouse().getId().equals(warehouseId)) {
            throw new ResourceNotFoundException("Commande introuvable dans cet entrepôt : " + orderId);
        }
        return order;
    }

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private Product getProduct(Long id) {
        return productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
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

    public PurchaseOrderResponse toResponse(PurchaseOrder o) {
        List<PurchaseOrderResponse.OrderLineResponse> lines = o.getLines().stream()
                .map(l -> PurchaseOrderResponse.OrderLineResponse.builder()
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

        return PurchaseOrderResponse.builder()
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
