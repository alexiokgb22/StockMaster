package com.backend.module.activityreport.controller;

import com.backend.module.activityreport.dto.ActivityReportResponse;
import com.backend.module.activityreport.dto.CreateActivityReportRequest;
import com.backend.module.activityreport.dto.UpdateActivityReportRequest;
import com.backend.module.activityreport.service.ActivityReportService;
import com.backend.module.shared.enums.ActivityReportStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Endpoints pour les rapports d'activité des magasiniers.
 *
 * Deux groupes de routes :
 *  - /api/my/activity-reports         → magasinier (ses propres rapports)
 *  - /api/warehouses/{id}/activity-reports → gestionnaire / admin (lecture par entrepôt)
 *  - /api/activity-reports/{id}        → détail, modification, soumission
 */
@RestController
@RequiredArgsConstructor
public class ActivityReportController {

    private final ActivityReportService reportService;

    // ─────────────────────────────────────────────────────────────
    // MAGASINIER — ses propres rapports
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/api/my/activity-reports")
    @PreAuthorize("hasAuthority('activity_report.create')")
    public ResponseEntity<Page<ActivityReportResponse>> getMyReports(
            @RequestParam(required = false) ActivityReportStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("reportDate").descending());
        return ResponseEntity.ok(reportService.findMyReports(status, pageable));
    }

    @PostMapping("/api/my/activity-reports")
    @PreAuthorize("hasAuthority('activity_report.create')")
    public ResponseEntity<ActivityReportResponse> create(
            @Valid @RequestBody CreateActivityReportRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.create(req));
    }

    // ─────────────────────────────────────────────────────────────
    // GESTIONNAIRE / ADMIN — rapports par entrepôt
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/api/warehouses/{warehouseId}/activity-reports")
    @PreAuthorize("hasAuthority('activity_report.view')")
    public ResponseEntity<Page<ActivityReportResponse>> getByWarehouse(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) Long storekeeperId,
            @RequestParam(required = false) ActivityReportStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("reportDate").descending());
        return ResponseEntity.ok(
            reportService.findByWarehouse(warehouseId, storekeeperId, status, from, to, pageable));
    }

    // ─────────────────────────────────────────────────────────────
    // DÉTAIL / MODIFICATION / SOUMISSION
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/api/activity-reports/{id}")
    @PreAuthorize("hasAuthority('activity_report.create') or hasAuthority('activity_report.view')")
    public ResponseEntity<ActivityReportResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.findById(id));
    }

    @PutMapping("/api/activity-reports/{id}")
    @PreAuthorize("hasAuthority('activity_report.create')")
    public ResponseEntity<ActivityReportResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateActivityReportRequest req
    ) {
        return ResponseEntity.ok(reportService.update(id, req));
    }

    @PatchMapping("/api/activity-reports/{id}/submit")
    @PreAuthorize("hasAuthority('activity_report.create')")
    public ResponseEntity<ActivityReportResponse> submit(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.submit(id));
    }
}
