package com.backend.module.auditlog.service;

import com.backend.exception.ResourceNotFoundException;
import com.backend.module.auditlog.dto.AuditLogResponse;
import com.backend.module.auditlog.entity.AuditLog;
import com.backend.module.auditlog.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service dédié à la lecture du journal d'audit.
 * Séparé de AuditLogService (écriture) pour respecter le SRP
 * et éviter les dépendances circulaires.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogQueryService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Recherche paginée avec tous les filtres combinés.
     */
    public Page<AuditLogResponse> search(
            String module,
            String action,
            String username,
            String entityName,
            Long warehouseId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        return auditLogRepository
                .search(module, action, username, entityName, warehouseId, from, to, pageable)
                .map(this::toResponse);
    }

    /**
     * Historique complet d'une entité précise.
     * Ex: tous les logs de la réception #42.
     */
    public Page<AuditLogResponse> findByEntity(String entityName, Long entityId, Pageable pageable) {
        return auditLogRepository
                .findByEntityNameAndEntityIdOrderByCreatedAtDesc(entityName, entityId, pageable)
                .map(this::toResponse);
    }

    /**
     * Détail d'un log précis.
     */
    public AuditLogResponse findById(Long id) {
        return auditLogRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Log d'audit introuvable : " + id));
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────

    public AuditLogResponse toResponse(AuditLog a) {
        return AuditLogResponse.builder()
                .id(a.getId())
                .module(a.getModule())
                .action(a.getAction())
                .entityName(a.getEntityName())
                .entityId(a.getEntityId())
                .description(a.getDescription())
                .username(a.getUsername())
                .userRole(a.getUserRole())
                .warehouseId(a.getWarehouseId())
                .warehouseName(a.getWarehouseName())
                .oldValue(a.getOldValue())
                .newValue(a.getNewValue())
                .ipAddress(a.getIpAddress())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
