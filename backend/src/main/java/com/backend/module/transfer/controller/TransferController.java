package com.backend.module.transfer.controller;

import com.backend.module.shared.enums.TransferStatus;
import com.backend.module.transfer.dto.CancelTransferRequest;
import com.backend.module.transfer.dto.CreateTransferRequest;
import com.backend.module.transfer.dto.ReceiveTransferRequest;
import com.backend.module.transfer.dto.TransferResponse;
import com.backend.module.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

public class TransferController {

    // ─────────────────────────────────────────────────────────────
    // CONTEXTE ENTREPÔT — transferts sortants (gestionnaire source)
    // POST   /api/warehouses/{id}/transfers
    // GET    /api/warehouses/{id}/transfers/outgoing
    // GET    /api/warehouses/{id}/transfers/incoming
    // GET    /api/warehouses/{id}/transfers/incoming/count
    // PATCH  /api/warehouses/{id}/transfers/{tId}/receive
    // ─────────────────────────────────────────────────────────────

    @RestController
    @RequestMapping("/api/warehouses/{warehouseId}/transfers")
    @RequiredArgsConstructor
    public static class WarehouseTransferController {

        private final TransferService transferService;

        // Gestionnaire source : ses transferts sortants
        @GetMapping("/outgoing")
        @PreAuthorize("hasAuthority('transfer.create')")
        public ResponseEntity<Page<TransferResponse>> getOutgoing(
                @PathVariable Long warehouseId,
                @RequestParam(required = false) TransferStatus status,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size
        ) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(transferService.findOutgoing(warehouseId, status, pageable));
        }

        // Gestionnaire cible : transferts entrants vers son entrepôt
        @GetMapping("/incoming")
        @PreAuthorize("hasAuthority('transfer.receive')")
        public ResponseEntity<Page<TransferResponse>> getIncoming(
                @PathVariable Long warehouseId,
                @RequestParam(required = false) TransferStatus status,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size
        ) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(transferService.findIncoming(warehouseId, status, pageable));
        }

        // Compteur de transferts VALIDATED entrants (badge dashboard)
        @GetMapping("/incoming/count")
        @PreAuthorize("hasAuthority('transfer.receive')")
        public ResponseEntity<Long> countIncoming(@PathVariable Long warehouseId) {
            return ResponseEntity.ok(transferService.countIncomingValidated(warehouseId));
        }

        // Gestionnaire source crée un transfert (PENDING)
        @PostMapping
        @PreAuthorize("hasAuthority('transfer.create')")
        public ResponseEntity<TransferResponse> create(
                @PathVariable Long warehouseId,
                @Valid @RequestBody CreateTransferRequest req
        ) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(transferService.create(warehouseId, req));
        }

        // Gestionnaire cible confirme la réception (VALIDATED → RECEIVED)
        @PatchMapping("/{transferId}/receive")
        @PreAuthorize("hasAuthority('transfer.receive')")
        public ResponseEntity<TransferResponse> receive(
                @PathVariable Long warehouseId,
                @PathVariable Long transferId,
                @Valid @RequestBody ReceiveTransferRequest req
        ) {
            return ResponseEntity.ok(transferService.receive(warehouseId, transferId, req));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VUE ADMIN GLOBALE
    // GET    /api/transfers
    // GET    /api/transfers/{id}
    // PATCH  /api/transfers/{id}/validate
    // PATCH  /api/transfers/{id}/cancel
    // ─────────────────────────────────────────────────────────────

    @RestController
    @RequestMapping("/api/transfers")
    @RequiredArgsConstructor
    public static class AdminTransferController {

        private final TransferService transferService;

        @GetMapping
        @PreAuthorize("hasAuthority('transfer.validate')")
        public ResponseEntity<Page<TransferResponse>> getAll(
                @RequestParam(required = false) TransferStatus status,
                @RequestParam(required = false) Long warehouseId,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size
        ) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(transferService.findAll(status, warehouseId, pageable));
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('transfer.validate') or hasAuthority('transfer.create') or hasAuthority('transfer.receive')")
        public ResponseEntity<TransferResponse> getById(@PathVariable Long id) {
            return ResponseEntity.ok(transferService.findById(id));
        }

        // Admin valide le transfert (PENDING → VALIDATED)
        @PatchMapping("/{id}/validate")
        @PreAuthorize("hasAuthority('transfer.validate')")
        public ResponseEntity<TransferResponse> validate(@PathVariable Long id) {
            return ResponseEntity.ok(transferService.validate(id));
        }

        // Admin annule le transfert
        @PatchMapping("/{id}/cancel")
        @PreAuthorize("hasAuthority('transfer.cancel')")
        public ResponseEntity<TransferResponse> cancel(
                @PathVariable Long id,
                @RequestBody(required = false) CancelTransferRequest req
        ) {
            return ResponseEntity.ok(transferService.cancel(id, req));
        }
    }
}
