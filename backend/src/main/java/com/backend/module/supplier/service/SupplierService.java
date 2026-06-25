package com.backend.module.supplier.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.supplier.dto.CreateSupplierRequest;
import com.backend.module.supplier.dto.SupplierResponse;
import com.backend.module.supplier.dto.UpdateSupplierRequest;
import com.backend.module.supplier.entity.Supplier;
import com.backend.module.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public Page<SupplierResponse> findAll(String search, Boolean active, Pageable pageable) {
        return supplierRepository.findAllWithFilters(search, active, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return toResponse(getSupplier(id));
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

        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse toggle(Long id) {
        Supplier supplier = getSupplier(id);
        supplier.setIsActive(!supplier.getIsActive());
        return toResponse(supplierRepository.save(supplier));
    }

    // ── Helpers ──────────────────────────────────────────────────

    private Supplier getSupplier(Long id) {
        return supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + id));
    }

    public SupplierResponse toResponse(Supplier s) {
        return SupplierResponse.builder()
            .id(s.getId())
            .name(s.getName())
            .address(s.getAddress())
            .city(s.getCity())
            .phone(s.getPhone())
            .email(s.getEmail())
            .contactName(s.getContactName())
            .isActive(s.getIsActive())
            .createdAt(s.getCreatedAt())
            .updatedAt(s.getUpdatedAt())
            .build();
    }
}
