package com.backend.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    private Double purchasePrice;
    private Double salePrice;
    private Double weight;
    private Double volume;

    // Entrepôts sélectionnés par l'admin (checkboxes).
    // Null ou vide = produit global sans affectation immédiate.
    private Set<Long> warehouseIds;
}
