package com.backend.module.auditreport.entity;

import java.time.LocalDateTime;

import com.backend.module.auditsession.entity.AuditSession;
import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.shared.enums.AuditReportStatus;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "audit_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AuditReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // Contenu du rapport rédigé par l'auditeur
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuditReportStatus status;

    // Date à laquelle l'auditeur a soumis le rapport à l'admin
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // L'auditeur auteur du rapport
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor_id", nullable = false)
    private User auditor;

    // L'entrepôt sur lequel porte le rapport
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    // La session d'audit dans le cadre de laquelle ce rapport a été rédigé
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_session_id", nullable = false)
    private AuditSession auditSession;
}
