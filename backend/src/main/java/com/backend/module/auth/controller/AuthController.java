package com.backend.module.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.module.auth.dto.LoginRequest;
import com.backend.module.auth.dto.LoginResponse;
import com.backend.module.auth.dto.UserInfoResponse;
import com.backend.module.auth.service.AuthService;
import com.backend.security.JwtTokenProvider;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Contrôleur REST pour l'authentification.
 * Expose les endpoints de connexion, déconnexion et récupération des infos utilisateur.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @Value("${jwt.cookie-name}")
    private String cookieName;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Endpoint de connexion.
     * Authentifie l'utilisateur et retourne un cookie HttpOnly sécurisé avec le JWT.
     * 
     * @param loginRequest Les identifiants de connexion
     * @return Les informations de l'utilisateur connecté
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);

        // Générer le token JWT
        String jwt = tokenProvider.generateToken(response.getUsername());

        // Créer le cookie HttpOnly sécurisé
        ResponseCookie cookie = ResponseCookie.from(cookieName, jwt)
                .httpOnly(true)
                .secure(false) // Mettre à true en production avec HTTPS
                .path("/")
                .maxAge(jwtExpirationMs / 1000) // Durée en secondes
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    /**
     * Endpoint de déconnexion.
     * Supprime le cookie JWT en définissant sa durée à 0.
     * 
     * @return Message de confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Déconnexion réussie");
    }

    /**
     * Endpoint pour récupérer les informations de l'utilisateur connecté.
     * 
     * @return Les informations de l'utilisateur
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        UserInfoResponse userInfo = authService.getCurrentUser();
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Endpoint pour vérifier si l'utilisateur est authentifié.
     * 
     * @return true si authentifié
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAuth() {
        return ResponseEntity.ok(true);
    }
}
