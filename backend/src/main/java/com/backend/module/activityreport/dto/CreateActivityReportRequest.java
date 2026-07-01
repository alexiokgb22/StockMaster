package com.backend.module.activityreport.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateActivityReportRequest {

    @NotNull(message = "La date du rapport est obligatoire")
    private LocalDate reportDate;

    private LocalTime arrivalTime;
    private LocalTime departureTime;

    /** Incidents de la journée — null si aucun. */
    private String incidents;

    /** Observations générales. */
    private String observations;
}
