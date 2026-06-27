package com.backend.module.auditlog.service;

import com.backend.module.auditlog.entity.AuditLog;
import com.backend.module.auditlog.repository.AuditLogRepository;
import com.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service central de traçabilité.
 *
 * Appelé par l'aspect AuditAspect (automatiquement sur les méthodes @Auditable)
 * ou directement depuis les services métier pour les cas nécessitant un contexte
 * riche (oldValue/newValue, description personnalisée, etc.).
 *
 * La persistance se fait dans une transaction INDÉPENDANTE (REQUIRES_NEW) afin
 * que l'échec du log ne fasse pas rouler en arrière l'opération métier.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Enregistre une action dans le journal.
     * Utilise REQUIRES_NEW pour isoler la persistance du log.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            String module,
            String action,
            String entityName,
            Long entityId,
            String description,
            Long warehouseId,
            String warehouseName,
            String oldValue,
            String newValue
    ) {
        try {
            String username = "system";
            String userRole = null;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
                username = details.getUsername();
                userRole = details.getRoleName();
            }

            AuditLog entry = AuditLog.builder()
                    .module(module)
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .description(description)
                    .username(username)
                    .userRole(userRole)
                    .warehouseId(warehouseId)
                    .warehouseName(warehouseName)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();

            auditLogRepository.save(entry);

        } catch (Exception e) {
            // Le log ne doit jamais faire échouer l'opération métier
            log.error("Erreur lors de l'enregistrement du log d'audit : {}", e.getMessage(), e);
        }
    }

    /**
     * Surcharge simplifiée sans old/new value ni entrepôt.
     * Pour les actions globales (création d'utilisateur, etc.).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String module, String action, String entityName, Long entityId, String description) {
        log(module, action, entityName, entityId, description, null, null, null, null);
    }

    /**
     * Surcharge avec entrepôt, sans old/new value.
     * Pour la majorité des actions métier (créations, validations).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            String module, String action, String entityName,
            Long entityId, String description,
            Long warehouseId, String warehouseName
    ) {
        log(module, action, entityName, entityId, description, warehouseId, warehouseName, null, null);
    }
}
