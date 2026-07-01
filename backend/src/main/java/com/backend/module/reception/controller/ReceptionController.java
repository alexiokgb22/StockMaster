package com.backend.module.reception.controller;

import com.backend.module.purchaseorder.dto.PurchaseOrderResponse;
import com.backend.module.reception.dto.CreateReceptionRequest;
import com.backend.module.reception.dto.ReceptionResponse;
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

    // ── GET — liste des bons (magasinier + gestionnaire en lecture)
    @GetMapping
    @PreAuthorize("hasAuthority('receipt.create') or hasAuthority('warehouse.read')")
    public ResponseEntity<Page<ReceptionResponse>> getAll(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) ReceptionStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(receptionService.findByWarehouse(warehouseId, status, pageable));
    }

    // ── GET — commandes DELIVERED disponibles pour réception
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

    // ── GET — détail d'un bon
    @GetMapping("/{receptionId}")
    @PreAuthorize("hasAuthority('receipt.create') or hasAuthority('warehouse.read')")
    public ResponseEntity<ReceptionResponse> getById(
            @PathVariable Long warehouseId,
            @PathVariable Long receptionId
    ) {
        return ResponseEntity.ok(receptionService.findById(warehouseId, receptionId));
    }

    // ── POST — magasinier crée et valide le bon en une seule opération
    @PostMapping
    @PreAuthorize("hasAuthority('receipt.create')")
    public ResponseEntity<ReceptionResponse> create(
            @PathVariable Long warehouseId,
            @Valid @RequestBody CreateReceptionRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(receptionService.create(warehouseId, req));
    }
}
