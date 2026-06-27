package com.backend.module.alert.controller;

import com.backend.module.alert.dto.AlertResponse;
import com.backend.module.alert.service.AlertService;
import com.backend.module.shared.enums.AlertSeverity;
import com.backend.module.shared.enums.AlertType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // ── GET /api/alerts ────────────────────────────────────────────
    // Paramètres optionnels : warehouseId, type, severity, isRead, isResolved
    @GetMapping
    @PreAuthorize("hasAuthority('alert.view')")
    public ResponseEntity<Page<AlertResponse>> getAll(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) AlertType type,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) Boolean isResolved,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
            alertService.findAll(warehouseId, type, severity, isRead, isResolved, pageable));
    }

    // ── GET /api/alerts/count-unread ───────────────────────────────
    @GetMapping("/count-unread")
    @PreAuthorize("hasAuthority('alert.view')")
    public ResponseEntity<Map<String, Long>> countUnread(
            @RequestParam(required = false) Long warehouseId
    ) {
        return ResponseEntity.ok(Map.of("count", alertService.countUnread(warehouseId)));
    }

    // ── PATCH /api/alerts/{id}/read ────────────────────────────────
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAuthority('alert.view')")
    public ResponseEntity<AlertResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.markAsRead(id));
    }

    // ── PATCH /api/alerts/{id}/resolve ─────────────────────────────
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('alert.manage')")
    public ResponseEntity<AlertResponse> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolve(id));
    }

    // ── PATCH /api/alerts/read-all ─────────────────────────────────
    @PatchMapping("/read-all")
    @PreAuthorize("hasAuthority('alert.view')")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @RequestParam(required = false) Long warehouseId
    ) {
        int updated = alertService.markAllAsRead(warehouseId);
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}
