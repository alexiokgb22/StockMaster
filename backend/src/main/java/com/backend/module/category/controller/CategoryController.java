package com.backend.module.category.controller;

import com.backend.module.category.dto.AssignWarehouseRequest;
import com.backend.module.category.dto.CategoryResponse;
import com.backend.module.category.dto.CreateCategoryRequest;
import com.backend.module.category.dto.UpdateCategoryRequest;
import com.backend.module.category.service.CategoryService;
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

public class CategoryController {

    // ─────────────────────────────────────────────────────────────
    // ADMIN : gestion globale des catégories
    //   GET    /api/categories              → toutes les catégories
    //   GET    /api/categories/unassigned   → non encore affectées
    //   POST   /api/categories              → créer (sans entrepôt)
    //   PUT    /api/categories/{id}         → modifier
    //   DELETE /api/categories/{id}         → supprimer
    //   PATCH  /api/categories/{id}/assign  → affecter à un entrepôt
    // ─────────────────────────────────────────────────────────────

    @RestController
    @RequestMapping("/api/categories")
    @RequiredArgsConstructor
    public static class AdminCategoryController {

        private final CategoryService categoryService;

        @GetMapping
        @PreAuthorize("hasAuthority('category.read')")
        public ResponseEntity<Page<CategoryResponse>> getAll(
                @RequestParam(required = false) String search,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size
        ) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            return ResponseEntity.ok(categoryService.findAll(search, pageable));
        }

        @GetMapping("/unassigned")
        @PreAuthorize("hasAuthority('category.read')")
        public ResponseEntity<List<CategoryResponse>> getUnassigned(
                @RequestParam(required = false) String search
        ) {
            return ResponseEntity.ok(categoryService.findUnassigned(search));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('category.create')")
        public ResponseEntity<CategoryResponse> create(
                @Valid @RequestBody CreateCategoryRequest req
        ) {
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(req));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('category.update')")
        public ResponseEntity<CategoryResponse> update(
                @PathVariable Long id,
                @Valid @RequestBody UpdateCategoryRequest req
        ) {
            return ResponseEntity.ok(categoryService.update(id, req));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('category.delete')")
        public ResponseEntity<Void> delete(@PathVariable Long id) {
            categoryService.delete(id);
            return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{id}/assign")
        @PreAuthorize("hasAuthority('category.create')")
        public ResponseEntity<CategoryResponse> assign(
                @PathVariable Long id,
                @Valid @RequestBody AssignWarehouseRequest req
        ) {
            return ResponseEntity.ok(categoryService.assignToWarehouse(id, req));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CONTEXTE ENTREPÔT : Admin (lecture) + Gestionnaire (création)
    //   GET  /api/warehouses/{warehouseId}/categories
    //   POST /api/warehouses/{warehouseId}/categories  → gestionnaire
    // ─────────────────────────────────────────────────────────────

    @RestController
    @RequestMapping("/api/warehouses/{warehouseId}/categories")
    @RequiredArgsConstructor
    public static class WarehouseCategoryController {

        private final CategoryService categoryService;

        @GetMapping
        @PreAuthorize("hasAuthority('category.read')")
        public ResponseEntity<Page<CategoryResponse>> getAll(
                @PathVariable Long warehouseId,
                @RequestParam(required = false) String search,
                @RequestParam(defaultValue = "0")  int page,
                @RequestParam(defaultValue = "20") int size
        ) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            return ResponseEntity.ok(categoryService.findByWarehouse(warehouseId, search, pageable));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('category.create')")
        public ResponseEntity<CategoryResponse> createInWarehouse(
                @PathVariable Long warehouseId,
                @Valid @RequestBody CreateCategoryRequest req
        ) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(categoryService.createInWarehouse(warehouseId, req));
        }

        // ─────────────────────────────────────────────────────────
        // DÉSAFFECTATION — retire la catégorie de l'entrepôt sans la supprimer
        //   DELETE /api/warehouses/{warehouseId}/categories/{id}
        // ─────────────────────────────────────────────────────────

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('category.delete')")
        public ResponseEntity<Void> unassign(
                @PathVariable Long warehouseId,
                @PathVariable Long id
        ) {
            categoryService.unassignFromWarehouse(warehouseId, id);
            return ResponseEntity.noContent().build();
        }
    }
}
