package com.backend.module.auth.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse de connexion.
 * Contient les informations de l'utilisateur authentifié et ses permissions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private Set<String> permissions;
    private Long warehouseId;
    private Boolean mustChangePassword;
    private String message;
}
