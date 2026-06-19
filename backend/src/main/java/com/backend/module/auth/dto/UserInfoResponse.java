package com.backend.module.auth.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les informations de l'utilisateur connecté.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private Long roleId;
    private Set<String> permissions;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCity;
    private String warehouseAddress;
    private Boolean mustChangePassword;
}
