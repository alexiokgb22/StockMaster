package com.backend.module.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private int productCount;
    private Long warehouseId;       // null = non affectée
    private String warehouseName;   // null = non affectée
    private String createdByUsername;

    // @JsonProperty force la sérialisation en "isAdminDefined" et "isAssigned"
    // au lieu de "adminDefined" / "assigned" (comportement par défaut de Lombok
    // pour les boolean primitifs avec préfixe "is").
    @JsonProperty("isAdminDefined")
    private boolean isAdminDefined;

    @JsonProperty("isAssigned")
    private boolean isAssigned;     // true si rattachée à un entrepôt

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
