package com.backend.module.stock.controller;

import com.backend.module.shared.enums.MovementType;
import com.backend.module.stock.dto.CreateStockRequest;
import com.backend.module.stock.dto.StockResponse;
import com.backend.module.stock.dto.UpdateStockRequest;
import com.backend.module.stock.service.StockService;
import com.backend.module.stockmovement.dto.StockMovementResponse;
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
@RequestMapping("/api/warehouses/{warehouseId}/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // ── GET /api/warehouses/{id}/stocks ──────────────────────────
    @GetMapping
    @PreAuthorize("hasAuthority('stock.read')")
    public ResponseEntity<Page<StockResponse>> getAll(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean belowMin,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
            stockService.findByWarehouse(warehouseId, zoneId, categoryId, belowMin, search, pageable));
    }

    // ── POST /api/warehouses/{id}/stocks ─────────────────────────
    @PostMapping
    @PreAuthorize("hasAuthority('stock.update')")
    public ResponseEntity<StockResponse> create(
            @PathVariable Long warehouseId,
            @Valid @RequestBody CreateStockRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockService.create(warehouseId, req));
    }

    // ── PUT /api/warehouses/{id}/stocks/{stockId} ────────────────
    @PutMapping("/{stockId}")
    @PreAuthorize("hasAuthority('stock.update')")
    public ResponseEntity<StockResponse> update(
            @PathVariable Long warehouseId,
            @PathVariable Long stockId,
            @Valid @RequestBody UpdateStockRequest req
    ) {
        return ResponseEntity.ok(stockService.update(warehouseId, stockId, req));
    }

    // ── GET /api/warehouses/{id}/stocks/movements ────────────────
    @GetMapping("/movements")
    @PreAuthorize("hasAuthority('stock.view_history')")
    public ResponseEntity<Page<StockMovementResponse>> getMovements(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
            stockService.findMovements(warehouseId, movementType, productId, zoneId, pageable));
    }
}
