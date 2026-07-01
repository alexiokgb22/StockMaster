package com.backend.module.activityreport.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * Mise à jour d'un rapport en DRAFT.
 * Tous les champs sont optionnels — seuls les champs non null sont mis à jour.
 */
@Data
public class UpdateActivityReportRequest {

    private LocalTime arrivalTime;
    private LocalTime departureTime;
    private String incidents;
    private String observations;
}
