package com.backend.module.transfer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateTransferRequest {

    @NotNull(message = "L'entrepôt cible est obligatoire")
    private Long targetWarehouseId;

    private String note;

    @NotEmpty(message = "Le transfert doit contenir au moins une ligne")
    @Valid
    private List<TransferLineRequest> lines;

    @Data
    public static class TransferLineRequest {

        @NotNull(message = "Le produit est obligatoire")
        private Long productId;

        @NotNull(message = "La zone source est obligatoire")
        private Long sourceZoneId;

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être au moins 1")
        private Integer quantity;

        private String note;
    }
}
