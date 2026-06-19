package com.backend.module.zone.dto;

import com.backend.module.shared.enums.ZoneType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de mise à jour partielle d'une zone.
 * Seuls les champs non null sont appliqués (patch sémantique).
 * Le gestionnaire ne peut modifier que les zones qu'il a créées.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateZoneRequest {

    // null = pas de changement
    private ZoneType zoneType;

    // null = pas de changement ; 0 ou négatif = erreur de validation côté service
    private Double capacity;
}
