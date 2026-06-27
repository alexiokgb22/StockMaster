package com.backend.module.auditlog.controller;

import com.backend.module.auditlog.dto.AuditLogResponse;
import com.backend.module.auditlog.service.AuditLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Endpoints de consultation du journal d'audit.
 * Tous en lecture seule — aucune modification possible.
 * Accès restreint aux rôles admin et auditeur (permission audit.view).
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    // ── GET /api/audit-logs ──────────────────────────────────────────────────
    // Liste paginée avec filtres
    @GetMapping
    @PreAuthorize("hasAuthority('audit.view')")
    public ResponseEntity<Page<AuditLogResponse>> search(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                auditLogQueryService.search(module, action, username, entityName, warehouseId, from, to, pageable)
        );
    }

    // ── GET /api/audit-logs/{id} ─────────────────────────────────────────────
    // Détail d'un log
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('audit.view')")
    public ResponseEntity<AuditLogResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogQueryService.findById(id));
    }

    // ── GET /api/audit-logs/entity/{entityName}/{entityId} ───────────────────
    // Historique d'une entité précise (ex: tous les logs de la réception #42)
    @GetMapping("/entity/{entityName}/{entityId}")
    @PreAuthorize("hasAuthority('audit.view')")
    public ResponseEntity<Page<AuditLogResponse>> getByEntity(
            @PathVariable String entityName,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogQueryService.findByEntity(entityName, entityId, pageable));
    }
}
