package com.backend.module.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleRequest {

    @NotNull(message = "L'identifiant du rôle est obligatoire")
    private Long roleId;
}
