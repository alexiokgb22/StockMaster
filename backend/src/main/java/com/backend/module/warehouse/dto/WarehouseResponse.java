package com.backend.module.warehouse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarehouseResponse {
    private Long id;
    private String name;
    private String city;
}
