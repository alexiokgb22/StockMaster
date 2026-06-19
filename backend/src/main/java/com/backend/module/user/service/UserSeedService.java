package com.backend.module.user.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.module.role.entity.Role;
import com.backend.module.role.repository.RoleRepository;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service de seed pour créer l'utilisateur administrateur par défaut.
 * S'exécute après le PermissionSeedService grâce à @Order(2).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSeedService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Order(2) // S'exécute après PermissionSeedService (Order 1)
    @Transactional
    public void seedAdminUser() {
        System.out.println("===============================================");
        System.out.println("DÉMARRAGE DU SEED DE L'UTILISATEUR ADMIN");
        System.out.println("===============================================");
        
        // Vérifier si un admin existe déjà
        if (userRepository.existsByUsername("admin")) {
            System.out.println("Utilisateur admin existe déjà");
            System.out.println("===============================================");
            return;
        }

        // Récupérer le rôle Administrateur
        Role adminRole = roleRepository.findByName("Administrateur")
                .orElseThrow(() -> new RuntimeException(
                        "Le rôle Administrateur doit être créé avant de seed l'utilisateur admin"));

        System.out.println("Rôle Administrateur trouvé : " + adminRole.getName());
        
        // Créer l'utilisateur admin
        User admin = User.builder()
        .username("admin")
        .email("admin@stockmaster.com")
        .password(passwordEncoder.encode("admin123"))
        .isActive(true)
        .mustChangePassword(false)
        .role(adminRole)
        .assignedWarehouse(null)
        .build();

        userRepository.save(admin);

        System.out.println("==================================================");
        System.out.println("Utilisateur admin créé avec succès !");
        System.out.println("Username: admin");
        System.out.println("Password: admin123");
        System.out.println("IMPORTANT: Changez ce mot de passe en production !");
        System.out.println("==================================================");
    }
}
