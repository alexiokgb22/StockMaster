package com.backend.module.supplier.controller;

import com.backend.module.supplier.dto.CreateSupplierRequest;
import com.backend.module.supplier.dto.SupplierResponse;
import com.backend.module.supplier.dto.UpdateSupplierRequest;
import com.backend.module.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    // GET /api/suppliers
    @GetMapping
    @PreAuthorize("hasAuthority('supplier.read')")
    public ResponseEntity<Page<SupplierResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(supplierService.findAll(search, active, pageable));
    }

    // GET /api/suppliers/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier.read')")
    public ResponseEntity<SupplierResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    // POST /api/suppliers
    @PostMapping
    @PreAuthorize("hasAuthority('supplier.create')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody CreateSupplierRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(req));
    }

    // PUT /api/suppliers/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier.update')")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequest req
    ) {
        return ResponseEntity.ok(supplierService.update(id, req));
    }

    // PATCH /api/suppliers/{id}/toggle
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('supplier.update')")
    public ResponseEntity<SupplierResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.toggle(id));
    }
}
