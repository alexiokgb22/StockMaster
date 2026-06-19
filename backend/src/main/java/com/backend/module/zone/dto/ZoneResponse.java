package com.backend.module.zone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneResponse {

    private Long id;
    private String name;
    private Integer sequenceNumber;
    private Double capacity;
    private String zoneType;
    private Long warehouseId;
    private String warehouseName;
    private Long categoryId;
    private String categoryName;
    private String createdByUsername;

    // @JsonProperty force "isAdminDefined" dans le JSON
    // (Lombok génère sinon "adminDefined" pour les boolean primitifs avec préfixe "is")
    @JsonProperty("isAdminDefined")
    private boolean isAdminDefined;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
