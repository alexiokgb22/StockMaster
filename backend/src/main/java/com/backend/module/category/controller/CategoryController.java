package com.backend.module.category.controller;

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

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ─────────────────────────────────────────────────────────────
    // LISTE PAGINÉE
    // ─────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────
    // DÉTAIL
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('category.read')")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — admin + gestionnaire
    // ─────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('category.create')")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(req));
    }

    // ─────────────────────────────────────────────────────────────
    // MODIFICATION — admin + gestionnaire
    // ─────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category.update')")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest req
    ) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    // ─────────────────────────────────────────────────────────────
    // SUPPRESSION — admin + gestionnaire
    // Refusée si des produits sont rattachés à la catégorie.
    // ─────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category.delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
