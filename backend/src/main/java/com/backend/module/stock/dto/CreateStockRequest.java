package com.backend.module.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockRequest {

    @NotNull(message = "Le produit est obligatoire")
    private Long productId;

    @NotNull(message = "La zone est obligatoire")
    private Long zoneId;

    @NotNull(message = "La quantité initiale est obligatoire")
    @Min(value = 0, message = "La quantité doit être positive ou nulle")
    private Integer initialQuantity;

    private Integer minStock;
    private Integer maxStock;

    private String note;
}
