package com.backend.module.category.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères")
    private String name;

    private String description;
}
