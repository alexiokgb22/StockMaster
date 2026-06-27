package com.backend.module.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Répartition du stock par catégorie (données camembert).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStockPoint {

    private Long categoryId;
    private String categoryName;

    /** Nombre de lignes de stock pour cette catégorie. */
    private long stockLineCount;

    /** Quantité totale disponible pour cette catégorie. */
    private long totalQuantity;
}
