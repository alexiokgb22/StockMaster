package com.backend.module.auditsession.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.backend.module.auditreport.entity.AuditReport;
import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.shared.enums.AuditSessionStatus;
import com.backend.module.user.entity.User;
import com.backend.module.warehouse.entity.Warehouse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "audit_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AuditSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuditSessionStatus status;

    // L'auditeur qui a lancé la session
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor_id", nullable = false)
    private User auditor;

    // Un audit peut couvrir plusieurs entrepôts
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "audit_session_warehouses",
        joinColumns = @JoinColumn(name = "audit_session_id"),
        inverseJoinColumns = @JoinColumn(name = "warehouse_id")
    )
    @JsonIgnore
    @Builder.Default
    private Set<Warehouse> warehouses = new HashSet<>();

    // Les rapports d'audit produits dans le cadre de cette session
    @OneToMany(mappedBy = "auditSession", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<AuditReport> auditReports = new HashSet<>();
}
