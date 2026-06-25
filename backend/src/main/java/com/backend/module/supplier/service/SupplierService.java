package com.backend.module.supplier.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.purchaseorder.entity.PurchaseOrder;
import com.backend.module.purchaseorder.repository.PurchaseOrderRepository;
import com.backend.module.supplier.dto.*;
import com.backend.module.supplier.entity.Supplier;
import com.backend.module.supplier.repository.SupplierRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Transactional(readOnly = true)
    public Page<SupplierResponse> findAll(String search, Boolean active, Pageable pageable) {
        return supplierRepository.findAllWithFilters(search, active, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return toResponse(getSupplier(id));
    }

    // Entrepôts du fournisseur avec le nb de livraisons
    @Transactional(readOnly = true)
    public List<SupplierWarehouseResponse> findWarehouses(Long supplierId) {
        Supplier supplier = getSupplier(supplierId);
        return supplier.getWarehouses().stream().map(w -> {
            long deliveryCount = purchaseOrderRepository
                .findBySuppliersAndWarehouse(supplierId, w.getId(), Pageable.unpaged())
                .getTotalElements();
            return SupplierWarehouseResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .city(w.getCity())
                .deliveryCount((int) deliveryCount)
                .build();
        }).toList();
    }

    // Historique de livraisons : fournisseur × entrepôt
    @Transactional(readOnly = true)
    public Page<DeliveryHistoryResponse> findDeliveryHistory(Long supplierId, Long warehouseId, Pageable pageable) {
        getSupplier(supplierId);
        return purchaseOrderRepository
            .findBySuppliersAndWarehouse(supplierId, warehouseId, pageable)
            .map(this::toDeliveryHistory);
    }

    public SupplierResponse create(CreateSupplierRequest req) {
        if (supplierRepository.existsByNameIgnoreCase(req.getName())) {
            throw new BusinessException("Un fournisseur \"" + req.getName() + "\" existe déjà");
        }

        Supplier supplier = Supplier.builder()
            .name(req.getName().trim())
            .address(req.getAddress())
            .city(req.getCity())
            .phone(req.getPhone())
            .email(req.getEmail())
            .contactName(req.getContactName())
            .isActive(true)
            .build();

        if (req.getWarehouseIds() != null) {
            for (Long wId : req.getWarehouseIds()) {
                supplier.getWarehouses().add(getWarehouse(wId));
            }
        }

        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse update(Long id, UpdateSupplierRequest req) {
        Supplier supplier = getSupplier(id);

        if (req.getName() != null && !req.getName().isBlank()) {
            String trimmed = req.getName().trim();
            if (!trimmed.equalsIgnoreCase(supplier.getName())
                    && supplierRepository.existsByNameIgnoreCaseAndIdNot(trimmed, id)) {
                throw new BusinessException("Un fournisseur \"" + trimmed + "\" existe déjà");
            }
            supplier.setName(trimmed);
        }
        if (req.getAddress() != null)     supplier.setAddress(req.getAddress());
        if (req.getCity() != null)        supplier.setCity(req.getCity());
        if (req.getPhone() != null)       supplier.setPhone(req.getPhone());
        if (req.getEmail() != null)       supplier.setEmail(req.getEmail());
        if (req.getContactName() != null) supplier.setContactName(req.getContactName());

        if (req.getWarehouseIds() != null) {
            supplier.getWarehouses().clear();
            for (Long wId : req.getWarehouseIds()) {
                supplier.getWarehouses().add(getWarehouse(wId));
            }
        }

        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse toggle(Long id) {
        Supplier supplier = getSupplier(id);
        supplier.setIsActive(!supplier.getIsActive());
        return toResponse(supplierRepository.save(supplier));
    }

    // ── Helpers ──────────────────────────────────────────────────

    private Supplier getSupplier(Long id) {
        return supplierRepository.findByIdWithWarehouses(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + id));
    }

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private SupplierResponse toResponse(Supplier s) {
        Set<Long> wIds = s.getWarehouses().stream().map(Warehouse::getId).collect(Collectors.toSet());
        Set<String> wNames = s.getWarehouses().stream().map(Warehouse::getName).collect(Collectors.toSet());
        return SupplierResponse.builder()
            .id(s.getId())
            .name(s.getName())
            .address(s.getAddress())
            .city(s.getCity())
            .phone(s.getPhone())
            .email(s.getEmail())
            .contactName(s.getContactName())
            .isActive(s.getIsActive())
            .warehouseIds(wIds)
            .warehouseNames(wNames)
            .createdAt(s.getCreatedAt())
            .updatedAt(s.getUpdatedAt())
            .build();
    }

    private DeliveryHistoryResponse toDeliveryHistory(PurchaseOrder o) {
        List<DeliveryHistoryResponse.DeliveryLineResponse> lines = o.getLines().stream().map(l ->
            DeliveryHistoryResponse.DeliveryLineResponse.builder()
                .productId(l.getProduct().getId())
                .productName(l.getProduct().getName())
                .productReference(l.getProduct().getReference())
                .quantity(l.getQuantity())
                .receivedQty(l.getReceivedQty())
                .unitPrice(l.getUnitPrice())
                .build()
        ).toList();

        return DeliveryHistoryResponse.builder()
            .orderId(o.getId())
            .orderNumber(o.getOrderNumber())
            .status(o.getStatus())
            .orderDate(o.getOrderDate())
            .expectedDate(o.getExpectedDate())
            .totalAmount(o.getTotalAmount())
            .createdByUsername(o.getCreatedBy().getUsername())
            .lines(lines)
            .createdAt(o.getCreatedAt())
            .build();
    }
}
