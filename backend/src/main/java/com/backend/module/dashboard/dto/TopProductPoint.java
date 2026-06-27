package com.backend.module.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Top produits les plus mouvementés sur la période.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductPoint {

    private Long productId;
    private String productName;
    private String productReference;
    private String categoryName;

    /** Nombre total de mouvements sur la période. */
    private long movementCount;

    /** Quantité totale mouvementée. */
    private long totalQuantity;
}
