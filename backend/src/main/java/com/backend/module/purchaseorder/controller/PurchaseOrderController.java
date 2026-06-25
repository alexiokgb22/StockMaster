package com.backend.module.purchaseorder.controller;

import com.backend.module.purchaseorder.dto.CreatePurchaseOrderRequest;
import com.backend.module.purchaseorder.dto.PurchaseOrderResponse;
import com.backend.module.purchaseorder.dto.ValidatePurchaseOrderRequest;
import com.backend.module.purchaseorder.service.PurchaseOrderService;
import com.backend.module.shared.enums.PurchaseOrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

public class PurchaseOrderController {

    // ─────────────────────────────────────────────────────────────
    // CONTEXTE ENTREPÔT
    //   GET    /api/warehouses/{id}/purchase-orders
    //   GET    /api/warehouses/{id}/purchase-orders/{orderId}
    //   POST   /api/warehouses/{id}/purchase-orders
    //   PATCH  /api/warehouses/{id}/purchase-orders/{orderId}/validate
    //   PATCH  /api/warehouses/{id}/purchase-orders/{orderId}/deliver
    //   PATCH  /api/warehouses/{id}/purchase-orders/{orderId}/close
    //   PATCH  /api/warehouses/{id}/purchase-orders/{orderId}/cancel
    // ─────────────────────────────────────────────────────────────

    @RestController
    @RequestMapping("/api/warehouses/{warehouseId}/purchase-orders")
    @RequiredArgsConstructor
    public static class WarehousePurchaseOrderController {

        private final PurchaseOrderService purchaseOrderService;

        @GetMapping
        @PreAuthorize("hasAuthority('stock.read')")
        public ResponseEntity<Page<PurchaseOrderResponse>> getAll(
                @PathVariable Long warehouseId,
                @RequestParam(required = false) PurchaseOrderStatus status,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size
        ) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(purchaseOrderService.findByWarehouse(warehouseId, status, pageable));
        }

        @GetMapping("/{orderId}")
        @PreAuthorize("hasAuthority('stock.read')")
        public ResponseEntity<PurchaseOrderResponse> getById(
                @PathVariable Long warehouseId,
                @PathVariable Long orderId
        ) {
            return ResponseEntity.ok(purchaseOrderService.findById(warehouseId, orderId));
        }

        // Gestionnaire crée une demande
        @PostMapping
        @PreAuthorize("hasAuthority('receipt.create')")
        public ResponseEntity<PurchaseOrderResponse> create(
                @PathVariable Long warehouseId,
                @Valid @RequestBody CreatePurchaseOrderRequest req
        ) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(purchaseOrderService.create(warehouseId, req));
        }

        // Admin valide et assigne le fournisseur
        @PatchMapping("/{orderId}/validate")
        @PreAuthorize("hasAuthority('receipt.validate')")
        public ResponseEntity<PurchaseOrderResponse> validate(
                @PathVariable Long warehouseId,
                @PathVariable Long orderId,
                @Valid @RequestBody ValidatePurchaseOrderRequest req
        ) {
            return ResponseEntity.ok(purchaseOrderService.validate(warehouseId, orderId, req));
        }

        // Admin marque la commande comme livrée
        @PatchMapping("/{orderId}/deliver")
        @PreAuthorize("hasAuthority('receipt.validate')")
        public ResponseEntity<PurchaseOrderResponse> deliver(
                @PathVariable Long warehouseId,
                @PathVariable Long orderId
        ) {
            return ResponseEntity.ok(purchaseOrderService.markDelivered(warehouseId, orderId));
        }

        // Gestionnaire clôture après réception du stock
        @PatchMapping("/{orderId}/close")
        @PreAuthorize("hasAuthority('receipt.validate')")
        public ResponseEntity<PurchaseOrderResponse> close(
                @PathVariable Long warehouseId,
                @PathVariable Long orderId
        ) {
            return ResponseEntity.ok(purchaseOrderService.close(warehouseId, orderId));
        }

        // Annulation (admin ou gestionnaire)
        @PatchMapping("/{orderId}/cancel")
        @PreAuthorize("hasAuthority('receipt.validate')")
        public ResponseEntity<PurchaseOrderResponse> cancel(
                @PathVariable Long warehouseId,
                @PathVariable Long orderId
        ) {
            return ResponseEntity.ok(purchaseOrderService.cancel(warehouseId, orderId));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VUE ADMIN GLOBALE
    //   GET /api/purchase-orders — toutes les commandes de tous les entrepôts
    // ─────────────────────────────────────────────────────────────

    @RestController
    @RequestMapping("/api/purchase-orders")
    @RequiredArgsConstructor
    public static class AdminPurchaseOrderController {

        private final PurchaseOrderService purchaseOrderService;

        @GetMapping
        @PreAuthorize("hasAuthority('receipt.validate')")
        public ResponseEntity<Page<PurchaseOrderResponse>> getAll(
                @RequestParam(required = false) PurchaseOrderStatus status,
                @RequestParam(required = false) Long warehouseId,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size
        ) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(purchaseOrderService.findAll(status, warehouseId, pageable));
        }

        // Historique des commandes d'un fournisseur — pour SupplierDetailPage
        @GetMapping("/by-supplier/{supplierId}")
        @PreAuthorize("hasAuthority('supplier.read')")
        public ResponseEntity<Page<PurchaseOrderResponse>> getBySupplierId(
                @PathVariable Long supplierId,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "10") int size
        ) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(purchaseOrderService.findBySupplierId(supplierId, pageable));
        }
    }
}
