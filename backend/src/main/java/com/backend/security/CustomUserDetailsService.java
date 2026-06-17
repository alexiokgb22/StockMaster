package com.backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service de chargement des détails utilisateur pour Spring Security.
 * Implémente le principe d'inversion de dépendance (DIP) via l'interface UserDetailsService.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Charge un utilisateur par son username OU email avec ses rôles et permissions.
     * Utilise une requête optimisée avec JOIN FETCH pour éviter les problèmes N+1.
     * 
     * @param usernameOrEmail Le nom d'utilisateur ou l'email
     * @return Les détails de l'utilisateur
     * @throws UsernameNotFoundException Si l'utilisateur n'existe pas
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé avec le username/email : " + usernameOrEmail));

        return new CustomUserDetails(user);
    }
}
