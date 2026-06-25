package com.backend.module.reception.dto;

import lombok.Data;

@Data
public class RejectReceptionRequest {
    private String reason; // Raison du rejet (optionnel)
}
