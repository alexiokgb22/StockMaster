package com.backend.module.transfer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReceiveTransferRequest {

    @NotEmpty(message = "Les zones cibles sont obligatoires")
    @Valid
    private List<ReceiveLineRequest> lines;

    @Data
    public static class ReceiveLineRequest {

        @NotNull(message = "L'id de la ligne de transfert est obligatoire")
        private Long transferLineId;

        @NotNull(message = "La zone cible est obligatoire")
        private Long targetZoneId;
    }
}
