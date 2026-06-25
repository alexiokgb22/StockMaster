package com.backend.module.reception.controller;

import com.backend.module.purchaseorder.dto.PurchaseOrderResponse;
import com.backend.module.reception.dto.CreateReceptionRequest;
import com.backend.module.reception.dto.ReceptionResponse;
import com.backend.module.reception.dto.RejectReceptionRequest;
import com.backend.module.reception.service.ReceptionService;
import com.backend.module.shared.enums.ReceptionStatus;
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
@RequestMapping("/api/warehouses/{warehouseId}/receptions")
@RequiredArgsConstructor
public class ReceptionController {

    private final ReceptionService receptionService;

    // ── GET /api/warehouses/{wId}/receptions ─────────────────────
    // Magasinier : ses bons / Gestionnaire : tous les bons de l'entrepôt
    @GetMapping
    @PreAuthorize("hasAuthority('receipt.create') or hasAuthority('receipt.validate')")
    public ResponseEntity<Page<ReceptionResponse>> getAll(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) ReceptionStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(receptionService.findByWarehouse(warehouseId, status, pageable));
    }

    // ── GET /api/warehouses/{wId}/receptions/deliverable ─────────
    // Commandes DELIVERED disponibles pour réception (Magasinier)
    @GetMapping("/deliverable")
    @PreAuthorize("hasAuthority('receipt.create')")
    public ResponseEntity<Page<PurchaseOrderResponse>> getDeliverable(
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(receptionService.findDeliverable(warehouseId, pageable));
    }

    // ── GET /api/warehouses/{wId}/receptions/pending-count ───────
    // Compteur PENDING pour le badge gestionnaire
    @GetMapping("/pending-count")
    @PreAuthorize("hasAuthority('receipt.validate')")
    public ResponseEntity<Long> getPendingCount(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(receptionService.countPending(warehouseId));
    }

    // ── GET /api/warehouses/{wId}/receptions/{rId} ───────────────
    @GetMapping("/{receptionId}")
    @PreAuthorize("hasAuthority('receipt.create') or hasAuthority('receipt.validate')")
    public ResponseEntity<ReceptionResponse> getById(
            @PathVariable Long warehouseId,
            @PathVariable Long receptionId
    ) {
        return ResponseEntity.ok(receptionService.findById(warehouseId, receptionId));
    }

    // ── POST /api/warehouses/{wId}/receptions ────────────────────
    // Magasinier crée un bon de réception
    @PostMapping
    @PreAuthorize("hasAuthority('receipt.create')")
    public ResponseEntity<ReceptionResponse> create(
            @PathVariable Long warehouseId,
            @Valid @RequestBody CreateReceptionRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(receptionService.create(warehouseId, req));
    }

    // ── PATCH /api/warehouses/{wId}/receptions/{rId}/validate ────
    // Gestionnaire valide → stock mis à jour, commande clôturée
    @PatchMapping("/{receptionId}/validate")
    @PreAuthorize("hasAuthority('receipt.validate')")
    public ResponseEntity<ReceptionResponse> validate(
            @PathVariable Long warehouseId,
            @PathVariable Long receptionId
    ) {
        return ResponseEntity.ok(receptionService.validate(warehouseId, receptionId));
    }

    // ── PATCH /api/warehouses/{wId}/receptions/{rId}/reject ──────
    // Gestionnaire rejette → commande reste DELIVERED
    @PatchMapping("/{receptionId}/reject")
    @PreAuthorize("hasAuthority('receipt.validate')")
    public ResponseEntity<ReceptionResponse> reject(
            @PathVariable Long warehouseId,
            @PathVariable Long receptionId,
            @RequestBody(required = false) RejectReceptionRequest req
    ) {
        return ResponseEntity.ok(receptionService.reject(warehouseId, receptionId, req));
    }
}
