package com.backend.module.activityreport.entity;

import java.time.LocalDate;

import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.user.entity.User;
import com.backend.module.warehouse.entity.Warehouse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "activity_reports")
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

    // Période couverte par le rapport
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // Contenu rédigé par le magasinier (entrées, sorties, transferts effectués)
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // Le magasinier auteur du rapport
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storekeeper_id", nullable = false)
    private User storekeeper;

    // L'entrepôt auquel est rattaché le magasinier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
}
