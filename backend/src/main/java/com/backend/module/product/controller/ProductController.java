package com.backend.module.product.controller;

import com.backend.module.product.dto.CreateProductRequest;
import com.backend.module.product.dto.ProductResponse;
import com.backend.module.product.dto.UpdateProductRequest;
import com.backend.module.product.dto.UpdateProductWarehousesRequest;
import com.backend.module.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class ProductController {

    // ─────────────────────────────────────────────────────────────
    // ADMIN : catalogue global
    //   GET    /api/products              → tous les produits
    //   POST   /api/products              → créer (admin)
    //   PUT    /api/products/{id}         → modifier
    //   PATCH  /api/products/{id}/toggle  → activer/désactiver
    // ─────────────────────────────────────────────────────────────

    @RestController
    @RequestMapping("/api/products")
    @RequiredArgsConstructor
    public static class AdminProductController {

        private final ProductService productService;

        @GetMapping
        @PreAuthorize("hasAuthority('product.read')")
        public ResponseEntity<Page<ProductResponse>> getAll(
                @RequestParam(required = false) String search,
                @RequestParam(required = false) Long categoryId,
                @RequestParam(required = false) Boolean active,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size
        ) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(productService.findAll(search, categoryId, active, pageable));
        }

        @GetMapping("/warehouses-by-category")
        @PreAuthorize("hasAuthority('product.read')")
        public ResponseEntity<List<Long>> getWarehousesByCategory(@RequestParam Long categoryId) {
            return ResponseEntity.ok(productService.findWarehouseIdsByCategory(categoryId));
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('product.read')")
        public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
            return ResponseEntity.ok(productService.findById(id));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('product.create')")
        public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
            return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(req));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('product.update')")
        public ResponseEntity<ProductResponse> update(
                @PathVariable Long id,
                @Valid @RequestBody UpdateProductRequest req
        ) {
            return ResponseEntity.ok(productService.update(id, req));
        }

        @PatchMapping("/{id}/toggle")
        @PreAuthorize("hasAuthority('product.delete_logic')")
        public ResponseEntity<ProductResponse> toggle(@PathVariable Long id) {
            return ResponseEntity.ok(productService.toggle(id));
        }

        @PutMapping("/{id}/warehouses")
        @PreAuthorize("hasAuthority('product.update')")
        public ResponseEntity<ProductResponse> updateWarehouses(
                @PathVariable Long id,
                @Valid @RequestBody UpdateProductWarehousesRequest req
        ) {
            return ResponseEntity.ok(productService.updateWarehouses(id, req.getWarehouseIds()));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CONTEXTE ENTREPÔT : Admin (lecture) + Gestionnaire (création)
    //   GET  /api/warehouses/{warehouseId}/products
    //   POST /api/warehouses/{warehouseId}/products
    //   GET  /api/warehouses/{warehouseId}/products/select
    //        → liste plate pour les selects de création de stock
    //          (filtrée par categoryId si fourni)
    // ─────────────────────────────────────────────────────────────

    @RestController
    @RequestMapping("/api/warehouses/{warehouseId}/products")
    @RequiredArgsConstructor
    public static class WarehouseProductController {

        private final ProductService productService;

        @GetMapping
        @PreAuthorize("hasAuthority('product.read')")
        public ResponseEntity<Page<ProductResponse>> getAll(
                @PathVariable Long warehouseId,
                @RequestParam(required = false) String search,
                @RequestParam(required = false) Long categoryId,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size
        ) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            return ResponseEntity.ok(
                productService.findByWarehouse(warehouseId, search, categoryId, pageable));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('product.create')")
        public ResponseEntity<ProductResponse> createInWarehouse(
                @PathVariable Long warehouseId,
                @Valid @RequestBody CreateProductRequest req
        ) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productService.createInWarehouse(warehouseId, req));
        }

        /**
         * Liste plate des produits pour le select de création de stock.
         * Si categoryId est fourni → filtre sur cette catégorie.
         * Sinon → tous les produits actifs de l'entrepôt.
         */
        @GetMapping("/select")
        @PreAuthorize("hasAuthority('product.read')")
        public ResponseEntity<List<ProductResponse>> getForSelect(
                @PathVariable Long warehouseId,
                @RequestParam(required = false) Long categoryId
        ) {
            return ResponseEntity.ok(productService.findForStockSelect(warehouseId, categoryId));
        }
    }
}
