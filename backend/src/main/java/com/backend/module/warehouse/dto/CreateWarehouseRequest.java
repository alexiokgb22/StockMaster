package com.backend.module.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWarehouseRequest {

    @NotBlank(message = "Le nom de l'entrepôt est obligatoire")
    private String name;

    @NotBlank(message = "L'adresse est obligatoire")
    private String address;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @NotNull(message = "La capacité totale est obligatoire")
    @Positive(message = "La capacité totale doit être positive")
    private Double totalCapacity;

    private Long managerId;
}
