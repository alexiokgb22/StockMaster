package com.backend.module.inventory.controller;

import com.backend.module.inventory.dto.CreateInventoryRequest;
import com.backend.module.inventory.dto.InventoryResponse;
import com.backend.module.inventory.dto.UpdateInventoryLineRequest;
import com.backend.module.inventory.service.InventoryService;
import com.backend.module.shared.enums.InventoryStatus;
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
@RequestMapping("/api/warehouses/{warehouseId}/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ── GET liste paginée ─────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAuthority('inventory.create') or hasAuthority('inventory.view_gap')")
    public ResponseEntity<Page<InventoryResponse>> getAll(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) InventoryStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(inventoryService.findByWarehouse(warehouseId, status, pageable));
    }

    // ── GET détail ────────────────────────────────────────────────
    @GetMapping("/{inventoryId}")
    @PreAuthorize("hasAuthority('inventory.create') or hasAuthority('inventory.start') or hasAuthority('inventory.view_gap')")
    public ResponseEntity<InventoryResponse> getById(
            @PathVariable Long warehouseId,
            @PathVariable Long inventoryId
    ) {
        return ResponseEntity.ok(inventoryService.findById(warehouseId, inventoryId));
    }

    // ── POST création + snapshot ──────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAuthority('inventory.create')")
    public ResponseEntity<InventoryResponse> create(
            @PathVariable Long warehouseId,
            @Valid @RequestBody CreateInventoryRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.create(warehouseId, req));
    }

    // ── PATCH saisie quantité physique sur une ligne ──────────────
    // Magasinier ou gestionnaire — ligne par ligne
    @PatchMapping("/{inventoryId}/lines/{lineId}")
    @PreAuthorize("hasAuthority('inventory.start') or hasAuthority('inventory.create')")
    public ResponseEntity<InventoryResponse> updateLine(
            @PathVariable Long warehouseId,
            @PathVariable Long inventoryId,
            @PathVariable Long lineId,
            @Valid @RequestBody UpdateInventoryLineRequest req
    ) {
        return ResponseEntity.ok(
                inventoryService.updateLine(warehouseId, inventoryId, lineId, req));
    }

    // ── PATCH clôture (irréversible) ──────────────────────────────
    @PatchMapping("/{inventoryId}/complete")
    @PreAuthorize("hasAuthority('inventory.complete')")
    public ResponseEntity<InventoryResponse> complete(
            @PathVariable Long warehouseId,
            @PathVariable Long inventoryId
    ) {
        return ResponseEntity.ok(inventoryService.complete(warehouseId, inventoryId));
    }

    // ── PATCH annulation ──────────────────────────────────────────
    @PatchMapping("/{inventoryId}/cancel")
    @PreAuthorize("hasAuthority('inventory.create')")
    public ResponseEntity<InventoryResponse> cancel(
            @PathVariable Long warehouseId,
            @PathVariable Long inventoryId
    ) {
        return ResponseEntity.ok(inventoryService.cancel(warehouseId, inventoryId));
    }
}
