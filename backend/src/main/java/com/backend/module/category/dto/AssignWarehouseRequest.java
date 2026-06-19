package com.backend.module.category.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignWarehouseRequest {

    @NotNull(message = "L'identifiant de l'entrepôt est obligatoire")
    private Long warehouseId;
}
