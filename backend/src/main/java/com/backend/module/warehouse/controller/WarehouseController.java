package com.backend.module.warehouse.controller;

import com.backend.module.warehouse.dto.CreateWarehouseRequest;
import com.backend.module.warehouse.dto.UpdateWarehouseRequest;
import com.backend.module.warehouse.dto.WarehouseResponse;
import com.backend.module.warehouse.service.WarehouseService;
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
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    // ──────────────────────────────────────────────────────────────
    // LECTURE
    // ──────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAuthority('warehouse.read')")
    public ResponseEntity<Page<WarehouseResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(warehouseService.findAll(search, active, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse.read')")
    public ResponseEntity<WarehouseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.findById(id));
    }

    // ──────────────────────────────────────────────────────────────
    // CRÉATION
    // ──────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('warehouse.create')")
    public ResponseEntity<WarehouseResponse> create(
            @Valid @RequestBody CreateWarehouseRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(warehouseService.createWarehouse(req));
    }

    // ──────────────────────────────────────────────────────────────
    // MODIFICATION
    // ──────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse.update')")
    public ResponseEntity<WarehouseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWarehouseRequest req
    ) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, req));
    }

    // ──────────────────────────────────────────────────────────────
    // ACTIVATION / DÉSACTIVATION
    // ──────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('warehouse.disable')")
    public ResponseEntity<WarehouseResponse> toggleWarehouse(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.toggleWarehouse(id));
    }
}
