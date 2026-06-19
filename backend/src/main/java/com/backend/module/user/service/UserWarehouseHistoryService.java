package com.backend.module.user.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.module.user.entity.User;
import com.backend.module.user.entity.UserWarehouseHistory;
import com.backend.module.user.repository.UserWarehouseHistoryRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Service de gestion de l'historique des affectations utilisateur-entrepôt.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserWarehouseHistoryService {

    private final UserWarehouseHistoryRepository historyRepository;

    /**
     * Enregistre une nouvelle affectation d'un utilisateur à un entrepôt.
     * Marque automatiquement les anciennes affectations comme non-courantes.
     * 
     * @param user L'utilisateur assigné
     * @param warehouse L'entrepôt d'affectation
     */
    public void recordAssignment(User user, Warehouse warehouse) {
        // 1. Marquer toutes les affectations précédentes comme non-courantes
        historyRepository.markAllAsNotCurrent(user.getId());

        // 2. Récupérer l'utilisateur qui effectue l'opération
        User assignedBy = getCurrentUser();

        // 3. Créer la nouvelle entrée d'historique
        UserWarehouseHistory history = UserWarehouseHistory.builder()
                .user(user)
                .warehouse(warehouse)
                .assignedAt(LocalDateTime.now())
                .assignedBy(assignedBy)
                .isCurrent(true)
                .build();

        historyRepository.save(history);
    }

    /**
     * Marque l'affectation actuelle d'un utilisateur comme terminée.
     * 
     * @param userId L'identifiant de l'utilisateur
     */
    public void recordUnassignment(Long userId) {
        historyRepository.markAllAsNotCurrent(userId);
    }

    /**
     * Récupère l'utilisateur connecté actuellement.
     */
    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                User user = new User();
                user.setId(userDetails.getId());
                return user;
            }
        } catch (Exception e) {
            // En cas d'erreur (seed, etc.), retourner null
        }
        return null;
    }
}
