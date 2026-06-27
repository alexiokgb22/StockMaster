package com.backend.module.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateInventoryLineRequest {

    @NotNull(message = "La quantité physique est obligatoire")
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    private Integer actualQty;

    private String note;
}
