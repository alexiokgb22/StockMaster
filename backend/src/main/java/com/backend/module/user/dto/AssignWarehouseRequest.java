package com.backend.module.user.dto;

import lombok.Data;

/**
 * Corps de la requête PATCH /api/users/:id/warehouse.
 * warehouseId null = désaffectation pure.
 */
@Data
public class AssignWarehouseRequest {

    /** Identifiant de l'entrepôt à assigner. Null pour désaffecter. */
    private Long warehouseId;
}
