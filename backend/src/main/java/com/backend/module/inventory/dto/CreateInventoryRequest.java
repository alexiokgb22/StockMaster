package com.backend.module.inventory.dto;

import com.backend.module.shared.enums.InventoryType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateInventoryRequest {

    @NotNull(message = "Le type d'inventaire est obligatoire")
    private InventoryType inventoryType;

    private String note;

    // Uniquement pour PARTIAL : liste des zoneId à inventorier
    // Si FULL ou liste vide → toutes les lignes de stock de l'entrepôt
    private List<Long> zoneIds;
}
