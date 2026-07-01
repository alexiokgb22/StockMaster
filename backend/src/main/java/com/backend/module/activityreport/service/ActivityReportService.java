package com.backend.module.activityreport.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.activityreport.dto.ActivityReportResponse;
import com.backend.module.activityreport.dto.CreateActivityReportRequest;
import com.backend.module.activityreport.dto.UpdateActivityReportRequest;
import com.backend.module.activityreport.entity.ActivityReport;
import com.backend.module.activityreport.repository.ActivityReportRepository;
import com.backend.module.dispatch.entity.Dispatch;
import com.backend.module.dispatch.repository.DispatchRepository;
import com.backend.module.reception.entity.Reception;
import com.backend.module.reception.repository.ReceptionRepository;
import com.backend.module.shared.enums.ActivityReportStatus;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import com.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityReportService {

    private final ActivityReportRepository reportRepository;
    private final ReceptionRepository receptionRepository;
    private final DispatchRepository dispatchRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;

    // ─────────────────────────────────────────────────────────────
    // LECTURE — rapports du magasinier connecté
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ActivityReportResponse> findMyReports(
            ActivityReportStatus status, Pageable pageable) {
        User me = currentUserEntity();
        return reportRepository
                .findByStorekeeper(me.getId(), status, pageable)
                .map(r -> toResponse(r, false));
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — rapports d'un entrepôt (gestionnaire / admin)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ActivityReportResponse> findByWarehouse(
            Long warehouseId,
            Long storekeeperId,
            ActivityReportStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {
        return reportRepository
                .findByWarehouse(warehouseId, storekeeperId, status, from, to, pageable)
                .map(r -> toResponse(r, false));
    }

    // ─────────────────────────────────────────────────────────────
    // LECTURE — détail d'un rapport (avec liste des opérations)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ActivityReportResponse findById(Long reportId) {
        ActivityReport report = getReport(reportId);
        checkAccess(report);
        return toResponse(report, true);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — Magasinier initie un rapport (DRAFT)
    //
    // Règles :
    //   1. Un seul rapport par magasinier par journée.
    //   2. La date ne peut pas être dans le futur.
    //   3. Le magasinier doit avoir un entrepôt assigné.
    // ─────────────────────────────────────────────────────────────

    public ActivityReportResponse create(CreateActivityReportRequest req) {
        User storekeeper = currentUserEntity();

        if (storekeeper.getAssignedWarehouse() == null) {
            throw new BusinessException(
                "Vous n'êtes affecté à aucun entrepôt. Contactez votre gestionnaire.");
        }

        Warehouse warehouse = storekeeper.getAssignedWarehouse();

        // Règle 2 : pas de rapport dans le futur
        if (req.getReportDate().isAfter(LocalDate.now())) {
            throw new BusinessException(
                "Impossible de créer un rapport pour une date future");
        }

        // Règle 1 : unicité par magasinier + date
        if (reportRepository.existsByStorekeeperIdAndReportDate(
                storekeeper.getId(), req.getReportDate())) {
            throw new BusinessException(
                "Un rapport existe déjà pour le " + req.getReportDate()
                + ". Vous pouvez le modifier s'il est encore en DRAFT.");
        }

        ActivityReport report = ActivityReport.builder()
                .reportDate(req.getReportDate())
                .arrivalTime(req.getArrivalTime())
                .departureTime(req.getDepartureTime())
                .incidents(req.getIncidents())
                .observations(req.getObservations())
                .status(ActivityReportStatus.DRAFT)
                .storekeeper(storekeeper)
                .warehouse(warehouse)
                .receptionCount(0)
                .dispatchCount(0)
                .build();

        return toResponse(reportRepository.save(report), false);
    }

    // ─────────────────────────────────────────────────────────────
    // MODIFICATION — Magasinier modifie un rapport DRAFT
    // ─────────────────────────────────────────────────────────────

    public ActivityReportResponse update(Long reportId, UpdateActivityReportRequest req) {
        ActivityReport report = getReport(reportId);
        User me = currentUserEntity();

        if (!report.getStorekeeper().getId().equals(me.getId())) {
            throw new BusinessException("Vous ne pouvez modifier que vos propres rapports");
        }
        if (report.getStatus() != ActivityReportStatus.DRAFT) {
            throw new BusinessException(
                "Ce rapport a déjà été soumis et ne peut plus être modifié");
        }

        if (req.getArrivalTime()   != null) report.setArrivalTime(req.getArrivalTime());
        if (req.getDepartureTime() != null) report.setDepartureTime(req.getDepartureTime());
        if (req.getIncidents()     != null) report.setIncidents(req.getIncidents());
        if (req.getObservations()  != null) report.setObservations(req.getObservations());

        return toResponse(reportRepository.save(report), false);
    }

    // ─────────────────────────────────────────────────────────────
    // SOUMISSION — Magasinier soumet le rapport (DRAFT → SUBMITTED)
    //
    // Au moment de la soumission :
    //   - Les réceptions et sorties de la journée sont comptées
    //     automatiquement à partir des données du système.
    //   - Le rapport devient non modifiable.
    // ─────────────────────────────────────────────────────────────

    public ActivityReportResponse submit(Long reportId) {
        ActivityReport report = getReport(reportId);
        User me = currentUserEntity();

        if (!report.getStorekeeper().getId().equals(me.getId())) {
            throw new BusinessException("Vous ne pouvez soumettre que vos propres rapports");
        }
        if (report.getStatus() != ActivityReportStatus.DRAFT) {
            throw new BusinessException("Ce rapport a déjà été soumis");
        }

        // Calculer automatiquement les compteurs à partir des données système
        LocalDate date = report.getReportDate();
        Long storekeeperId = me.getId();
        Long warehouseId = report.getWarehouse().getId();

        // Début et fin de journée
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd   = date.atTime(LocalTime.MAX);

        int receptionCount = receptionRepository
                .countByCreatedByIdAndWarehouseIdAndCreatedAtBetween(
                        storekeeperId, warehouseId, dayStart, dayEnd);

        int dispatchCount = dispatchRepository
                .countByCreatedByIdAndWarehouseIdAndCreatedAtBetween(
                        storekeeperId, warehouseId, dayStart, dayEnd);

        report.setReceptionCount(receptionCount);
        report.setDispatchCount(dispatchCount);
        report.setStatus(ActivityReportStatus.SUBMITTED);

        return toResponse(reportRepository.save(report), true);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────────

    private ActivityReport getReport(Long id) {
        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rapport d'activité introuvable : " + id));
    }

    /**
     * Vérifie que l'utilisateur connecté a le droit de lire ce rapport.
     * - Magasinier : seulement ses propres rapports.
     * - Gestionnaire / Admin : tous les rapports.
     */
    private void checkAccess(ActivityReport report) {
        User me = currentUserEntity();
        String role = me.getRole().getName();
        boolean isMagasinier = "Magasinier".equals(role);
        if (isMagasinier && !report.getStorekeeper().getId().equals(me.getId())) {
            throw new BusinessException("Accès refusé à ce rapport");
        }
    }

    private User currentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findByIdWithRole(details.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    // ─────────────────────────────────────────────────────────────
    // MAPPING
    // ─────────────────────────────────────────────────────────────

    private ActivityReportResponse toResponse(ActivityReport r, boolean withOperations) {
        ActivityReportResponse.ActivityReportResponseBuilder builder = ActivityReportResponse.builder()
                .id(r.getId())
                .reportDate(r.getReportDate())
                .arrivalTime(r.getArrivalTime())
                .departureTime(r.getDepartureTime())
                .incidents(r.getIncidents())
                .observations(r.getObservations())
                .receptionCount(r.getReceptionCount())
                .dispatchCount(r.getDispatchCount())
                .status(r.getStatus())
                .storekeeperId(r.getStorekeeper().getId())
                .storekeeperUsername(r.getStorekeeper().getUsername())
                .warehouseId(r.getWarehouse().getId())
                .warehouseName(r.getWarehouse().getName())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt());

        if (withOperations) {
            LocalDate date = r.getReportDate();
            Long storekeeperId = r.getStorekeeper().getId();
            Long warehouseId   = r.getWarehouse().getId();
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd   = date.atTime(LocalTime.MAX);

            // Réceptions du jour par ce magasinier
            List<Reception> receptions = receptionRepository
                    .findByCreatedByIdAndWarehouseIdAndCreatedAtBetween(
                            storekeeperId, warehouseId, dayStart, dayEnd);

            List<ActivityReportResponse.OperationSummary> receptionSummaries = receptions.stream()
                    .map(rec -> ActivityReportResponse.OperationSummary.builder()
                            .id(rec.getId())
                            .number(rec.getReceptionNumber())
                            .type("RECEPTION")
                            .lineCount(rec.getLines().size())
                            .doneAt(rec.getCreatedAt())
                            .build())
                    .toList();

            // Sorties du jour par ce magasinier
            List<Dispatch> dispatches = dispatchRepository
                    .findByCreatedByIdAndWarehouseIdAndCreatedAtBetween(
                            storekeeperId, warehouseId, dayStart, dayEnd);

            List<ActivityReportResponse.OperationSummary> dispatchSummaries = dispatches.stream()
                    .map(d -> ActivityReportResponse.OperationSummary.builder()
                            .id(d.getId())
                            .number(d.getDispatchNumber())
                            .type("DISPATCH")
                            .lineCount(d.getLines().size())
                            .doneAt(d.getCreatedAt())
                            .build())
                    .toList();

            builder.receptions(receptionSummaries);
            builder.dispatches(dispatchSummaries);
        }

        return builder.build();
    }
}
