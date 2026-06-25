package com.backend.module.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSupplierRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    private String address;
    private String city;
    private String phone;

    @Email(message = "Email invalide")
    private String email;

    private String contactName;
}
