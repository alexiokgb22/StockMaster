package com.backend.module.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un point de la série temporelle entrées/sorties.
 * Utilisé pour tracer l'évolution des flux sur la période demandée.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementChartPoint {

    /** Date au format "yyyy-MM-dd" (granularité jour) ou "yyyy-WW" (semaine) ou "yyyy-MM" (mois). */
    private String label;

    /** Quantité totale entrée (ENTRY). */
    private long entries;

    /** Quantité totale sortie (EXIT + TRANSFER). */
    private long exits;

    /** Quantité transférée entre entrepôts (TRANSFER uniquement). */
    private long transfers;
}
