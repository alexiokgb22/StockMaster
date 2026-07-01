package com.backend.module.activityreport.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.shared.enums.ActivityReportStatus;
import com.backend.module.user.entity.User;
import com.backend.module.warehouse.entity.Warehouse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "activity_reports",
    uniqueConstraints = {
        // Un seul rapport par magasinier par journée
        @UniqueConstraint(
            name = "uk_activity_report_storekeeper_date",
            columnNames = {"storekeeper_id", "report_date"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ActivityReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ── Période ───────────────────────────────────────────────────
    /** Date de la journée couverte par le rapport. */
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    /** Heure d'arrivée du magasinier. */
    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    /** Heure de départ du magasinier. */
    @Column(name = "departure_time")
    private LocalTime departureTime;

    // ── Champs structurés ─────────────────────────────────────────
    /**
     * Incidents survenus dans la journée (problèmes de livraison,
     * produits endommagés, problèmes matériels, accidents...).
     * Null ou vide si aucun incident.
     */
    @Column(name = "incidents", columnDefinition = "TEXT")
    private String incidents;

    /**
     * Observations générales du magasinier sur la journée
     * (état du stock, remarques terrain, suggestions...).
     */
    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    // ── Résumé opérationnel (auto-calculé au moment de la soumission) ──
    /** Nombre de réceptions effectuées sur la période. */
    @Column(name = "reception_count")
    private Integer receptionCount;

    /** Nombre de sorties effectuées sur la période. */
    @Column(name = "dispatch_count")
    private Integer dispatchCount;

    // ── Statut ────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActivityReportStatus status;

    // ── Relations ─────────────────────────────────────────────────
    /** Le magasinier auteur du rapport. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storekeeper_id", nullable = false)
    private User storekeeper;

    /** L'entrepôt auquel est rattaché le magasinier. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
}
