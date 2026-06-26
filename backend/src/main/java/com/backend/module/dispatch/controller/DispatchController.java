package com.backend.module.dispatch.controller;

import com.backend.module.dispatch.dto.CreateDispatchRequest;
import com.backend.module.dispatch.dto.DispatchResponse;
import com.backend.module.dispatch.dto.RejectDispatchRequest;
import com.backend.module.dispatch.service.DispatchService;
import com.backend.module.shared.enums.DispatchStatus;
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
@RequestMapping("/api/warehouses/{warehouseId}/dispatches")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    // ── GET /api/warehouses/{wId}/dispatches ─────────────────────
    // Magasinier : ses bons / Gestionnaire : tous les bons de l'entrepôt
    @GetMapping
    @PreAuthorize("hasAuthority('dispatch.create') or hasAuthority('dispatch.validate')")
    public ResponseEntity<Page<DispatchResponse>> getAll(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) DispatchStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(dispatchService.findByWarehouse(warehouseId, status, pageable));
    }

    // ── GET /api/warehouses/{wId}/dispatches/{dId} ───────────────
    @GetMapping("/{dispatchId}")
    @PreAuthorize("hasAuthority('dispatch.create') or hasAuthority('dispatch.validate')")
    public ResponseEntity<DispatchResponse> getById(
            @PathVariable Long warehouseId,
            @PathVariable Long dispatchId
    ) {
        return ResponseEntity.ok(dispatchService.findById(warehouseId, dispatchId));
    }

    // ── GET /api/warehouses/{wId}/dispatches/pending-count ───────
    // Compteur PENDING pour le badge gestionnaire
    @GetMapping("/pending-count")
    @PreAuthorize("hasAuthority('dispatch.validate')")
    public ResponseEntity<Long> getPendingCount(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(dispatchService.countPending(warehouseId));
    }

    // ── POST /api/warehouses/{wId}/dispatches ────────────────────
    // Magasinier crée un bon de sortie (PENDING)
    @PostMapping
    @PreAuthorize("hasAuthority('dispatch.create')")
    public ResponseEntity<DispatchResponse> create(
            @PathVariable Long warehouseId,
            @Valid @RequestBody CreateDispatchRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dispatchService.create(warehouseId, req));
    }

    // ── PATCH /api/warehouses/{wId}/dispatches/{dId}/validate ────
    // Gestionnaire valide → stock décrémenté, StockMovement EXIT généré
    @PatchMapping("/{dispatchId}/validate")
    @PreAuthorize("hasAuthority('dispatch.validate')")
    public ResponseEntity<DispatchResponse> validate(
            @PathVariable Long warehouseId,
            @PathVariable Long dispatchId
    ) {
        return ResponseEntity.ok(dispatchService.validate(warehouseId, dispatchId));
    }

    // ── PATCH /api/warehouses/{wId}/dispatches/{dId}/reject ──────
    // Gestionnaire rejette → stock inchangé
    @PatchMapping("/{dispatchId}/reject")
    @PreAuthorize("hasAuthority('dispatch.validate')")
    public ResponseEntity<DispatchResponse> reject(
            @PathVariable Long warehouseId,
            @PathVariable Long dispatchId,
            @RequestBody(required = false) RejectDispatchRequest req
    ) {
        return ResponseEntity.ok(dispatchService.reject(warehouseId, dispatchId, req));
    }
}
