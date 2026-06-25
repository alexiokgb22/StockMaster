package com.backend.module.supplier.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateSupplierRequest {

    private String name;
    private String address;
    private String city;
    private String phone;

    @Email(message = "Email invalide")
    private String email;

    private String contactName;
    private Set<Long> warehouseIds;
}
