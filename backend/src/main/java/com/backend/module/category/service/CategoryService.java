package com.backend.module.category.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.category.dto.CategoryResponse;
import com.backend.module.category.dto.CreateCategoryRequest;
import com.backend.module.category.dto.UpdateCategoryRequest;
import com.backend.module.category.entity.Category;
import com.backend.module.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // ─────────────────────────────────────────────────────────────
    // LECTURE
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAll(String search, Pageable pageable) {
        Page<Category> categories = (search != null && !search.isBlank())
                ? categoryRepository.findByNameContainingIgnoreCase(search, pageable)
                : categoryRepository.findAll(pageable);
        return categories.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return toResponse(getCategory(id));
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION
    // ─────────────────────────────────────────────────────────────

    public CategoryResponse create(CreateCategoryRequest req) {
        if (categoryRepository.existsByNameIgnoreCase(req.getName())) {
            throw new BusinessException("Une catégorie avec ce nom existe déjà : " + req.getName());
        }

        Category category = Category.builder()
                .name(req.getName().trim())
                .description(req.getDescription())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    // ─────────────────────────────────────────────────────────────
    // MODIFICATION
    // ─────────────────────────────────────────────────────────────

    public CategoryResponse update(Long id, UpdateCategoryRequest req) {
        Category category = getCategory(id);

        if (req.getName() != null && !req.getName().isBlank()) {
            String trimmed = req.getName().trim();
            if (!trimmed.equalsIgnoreCase(category.getName())
                    && categoryRepository.existsByNameIgnoreCaseAndIdNot(trimmed, id)) {
                throw new BusinessException("Une catégorie avec ce nom existe déjà : " + trimmed);
            }
            category.setName(trimmed);
        }

        if (req.getDescription() != null) {
            category.setDescription(req.getDescription());
        }

        return toResponse(categoryRepository.save(category));
    }

    // ─────────────────────────────────────────────────────────────
    // SUPPRESSION
    // Refusée si des produits référencent cette catégorie,
    // pour ne pas casser l'intégrité des données.
    // ─────────────────────────────────────────────────────────────

    public void delete(Long id) {
        Category category = getCategory(id);

        long productCount = categoryRepository.countProducts(id);
        if (productCount > 0) {
            throw new BusinessException(
                "Impossible de supprimer la catégorie \"" + category.getName()
                + "\" : " + productCount + " produit(s) y sont rattachés."
            );
        }

        categoryRepository.delete(category);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .productCount((int) categoryRepository.countProducts(c.getId()))
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
