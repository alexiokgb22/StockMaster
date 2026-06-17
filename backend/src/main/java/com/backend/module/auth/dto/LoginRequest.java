package com.backend.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la requête de connexion.
 * Accepte username OU email pour la connexion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Le nom d'utilisateur ou l'email est obligatoire")
    private String username; // Peut contenir username ou email

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
