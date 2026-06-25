package com.backend.module.purchaseorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ValidatePurchaseOrderRequest {

    @NotNull(message = "Le fournisseur est obligatoire")
    private Long supplierId;

    private LocalDate expectedDate;
    private String note;
}
