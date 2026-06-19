package com.backend.module.zone.dto;

import com.backend.module.shared.enums.ZoneType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateZoneRequest {

    @NotNull(message = "Le type de zone est obligatoire")
    private ZoneType zoneType;

    private Double capacity;

    // Catégorie optionnelle à l'affectation directe (si déjà connue)
    private Long categoryId;
}
