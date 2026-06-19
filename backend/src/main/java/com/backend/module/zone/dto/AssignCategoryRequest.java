package com.backend.module.zone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignCategoryRequest {

    @NotNull(message = "L'identifiant de la catégorie est obligatoire")
    private Long categoryId;
}
