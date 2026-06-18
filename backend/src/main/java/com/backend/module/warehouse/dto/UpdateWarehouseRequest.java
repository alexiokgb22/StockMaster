package com.backend.module.warehouse.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWarehouseRequest {

    private String name;
    private String address;
    private String city;

    @Positive(message = "La capacité totale doit être positive")
    private Double totalCapacity;

    private Long managerId;
}
