package com.backend.module.stock.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {

    @Min(value = 0, message = "La quantité doit être positive ou nulle")
    private Integer quantityAvailable;

    @Min(value = 0, message = "Le stock minimum doit être positif ou nul")
    private Integer minStock;

    @Min(value = 0, message = "Le stock maximum doit être positif ou nul")
    private Integer maxStock;

    private String note;
}
