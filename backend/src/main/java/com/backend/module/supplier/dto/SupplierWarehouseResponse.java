package com.backend.module.supplier.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierWarehouseResponse {
    private Long id;
    private String name;
    private String city;
    private int deliveryCount; // nb de commandes livrées (DELIVERED)
}
