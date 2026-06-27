package com.backend.module.auditlog.aspect;

import com.backend.module.auditlog.annotation.Auditable;
import com.backend.module.auditlog.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect AOP qui intercepte automatiquement les méthodes annotées {@link Auditable}
 * et enregistre les actions dans le journal.
 *
 * Deux comportements :
 * - @AfterReturning : log de succès avec l'id de l'entité retournée si disponible
 * - @AfterThrowing  : log d'échec pour garder une trace des tentatives avortées
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogService auditLogService;

    /**
     * Intercepte après un retour normal.
     * Tente d'extraire l'id de l'objet retourné via getId().
     */
    @AfterReturning(
        pointcut = "@annotation(auditable)",
        returning = "result"
    )
    public void logSuccess(JoinPoint jp, Auditable auditable, Object result) {
        try {
            Long entityId = extractId(result);
            Long warehouseId = extractWarehouseIdFromArgs(jp.getArgs());
            String warehouseName = extractWarehouseNameFromResult(result);

            String description = auditable.description().isEmpty()
                ? auditable.action() + " " + auditable.entity()
                    + (entityId != null ? " #" + entityId : "")
                : auditable.description();

            auditLogService.log(
                auditable.module(),
                auditable.action(),
                auditable.entity(),
                entityId,
                description,
                warehouseId,
                warehouseName
            );

        } catch (Exception e) {
            log.warn("AuditAspect : impossible de logger l'action '{}' sur '{}' : {}",
                auditable.action(), auditable.entity(), e.getMessage());
        }
    }

    /**
     * Intercepte après une exception.
     * Log la tentative échouée pour assurer la traçabilité complète.
     */
    @AfterThrowing(
        pointcut = "@annotation(auditable)",
        throwing = "ex"
    )
    public void logFailure(JoinPoint jp, Auditable auditable, Throwable ex) {
        try {
            Long warehouseId = extractWarehouseIdFromArgs(jp.getArgs());

            auditLogService.log(
                auditable.module(),
                auditable.action() + "_FAILED",
                auditable.entity(),
                null,
                auditable.action() + " " + auditable.entity() + " échoué : " + ex.getMessage(),
                warehouseId,
                null
            );
        } catch (Exception e) {
            log.warn("AuditAspect : impossible de logger l'échec : {}", e.getMessage());
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Tente d'extraire l'id via getId() par réflexion.
     * Compatible avec tous les DTOs de réponse qui ont un getId().
     */
    private Long extractId(Object result) {
        if (result == null) return null;
        try {
            Method getId = result.getClass().getMethod("getId");
            Object id = getId.invoke(result);
            return id instanceof Long l ? l : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tente d'extraire warehouseId depuis les arguments de la méthode.
     * Convention : le premier argument de type Long nommé warehouseId
     * ou toute variable Long en première position.
     */
    private Long extractWarehouseIdFromArgs(Object[] args) {
        if (args == null || args.length == 0) return null;
        // Par convention dans ce projet, le premier paramètre est toujours warehouseId
        if (args[0] instanceof Long id) return id;
        return null;
    }
    

    /**
     * Tente d'extraire warehouseName depuis le résultat via getWarehouseName().
     */
    private String extractWarehouseNameFromResult(Object result) {
        if (result == null) return null;
        try {
            Method getWarehouseName = result.getClass().getMethod("getWarehouseName");
            Object name = getWarehouseName.invoke(result);
            return name instanceof String s ? s : null;
        } catch (Exception e) {
            return null;
        }
    }
}
