package com.backend.module.reception.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateReceptionRequest {

    @NotNull(message = "La commande est obligatoire")
    private Long purchaseOrderId;

    private String note;

    @NotEmpty(message = "Le bon doit contenir au moins une ligne")
    @Valid
    private List<ReceptionLineRequest> lines;

    @Data
    public static class ReceptionLineRequest {

        @NotNull(message = "La ligne de commande est obligatoire")
        private Long purchaseOrderLineId;

        @NotNull(message = "La quantité reçue est obligatoire")
        @Min(value = 0, message = "La quantité reçue ne peut pas être négative")
        private Integer quantityReceived;

        @NotNull(message = "La zone de rangement est obligatoire")
        private Long zoneId;

        private String note;
    }
}
