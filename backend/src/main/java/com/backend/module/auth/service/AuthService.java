package com.backend.module.auth.service;

import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.module.auth.dto.LoginRequest;
import com.backend.module.auth.dto.LoginResponse;
import com.backend.module.auth.dto.UserInfoResponse;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;
import com.backend.security.CustomUserDetails;
import com.backend.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

/**
 * Service d'authentification.
 * Gère la connexion et la récupération des informations utilisateur.
 * Respecte le principe de responsabilité unique (SRP).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authentifie un utilisateur et génère un token JWT.
     * 
     * @param loginRequest Les identifiants de connexion
     * @return Les informations de l'utilisateur authentifié et le token
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        // Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Générer le token JWT
        String jwt = tokenProvider.generateToken(authentication);

        // Récupérer les détails de l'utilisateur
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Extraire les permissions
        var permissions = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());

        return LoginResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRoleName())
                .permissions(permissions)
                .warehouseId(userDetails.getWarehouseId())
                .mustChangePassword(userDetails.isMustChangePassword())
                .message("Connexion réussie")
                .build();
    }

    /**
     * Récupère les informations de l'utilisateur actuellement connecté.
     * 
     * @return Les informations de l'utilisateur
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        var permissions = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());

        return UserInfoResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRoleName())
                .roleId(userDetails.getRoleId())
                .permissions(permissions)
                .warehouseId(userDetails.getWarehouseId())
                .warehouseName(userDetails.getWarehouseName())
                .mustChangePassword(userDetails.isMustChangePassword())
                .build();
    }

    /**
     * Permet à l'utilisateur connecté de changer son propre mot de passe.
     * Remet mustChangePassword à false.
     */
    @Transactional
    public void changeOwnPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    /**
     * Vérifie si un utilisateur a une permission spécifique.
     * 
     * @param permissionCode Le code de la permission à vérifier
     * @return true si l'utilisateur a la permission, false sinon
     */
    public boolean hasPermission(String permissionCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permissionCode));
    }
}
