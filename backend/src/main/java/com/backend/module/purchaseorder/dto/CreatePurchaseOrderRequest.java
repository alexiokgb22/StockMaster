package com.backend.module.purchaseorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePurchaseOrderRequest {

    private LocalDate expectedDate;
    private String note;

    @NotEmpty(message = "La commande doit contenir au moins une ligne")
    @Valid
    private List<OrderLineRequest> lines;

    @Data
    public static class OrderLineRequest {

        @NotNull(message = "Le produit est obligatoire")
        private Long productId;

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être supérieure à 0")
        private Integer quantity;

        private Double unitPrice;
    }
}
