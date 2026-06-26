package com.backend.module.dispatch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateDispatchRequest {

    private String note;

    @NotEmpty(message = "Le bon doit contenir au moins une ligne")
    @Valid
    private List<DispatchLineRequest> lines;

    @Data
    public static class DispatchLineRequest {

        @NotNull(message = "Le produit est obligatoire")
        private Long productId;

        @NotNull(message = "La zone est obligatoire")
        private Long zoneId;

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être au moins 1")
        private Integer quantityRequested;

        private String note;
    }
}
