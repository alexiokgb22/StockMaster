package com.backend.module.dispatch.entity;

import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.shared.enums.DispatchStatus;
import com.backend.module.user.entity.User;
import com.backend.module.warehouse.entity.Warehouse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "dispatches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Dispatch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "dispatch_number", nullable = false, unique = true, length = 100)
    private String dispatchNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DispatchStatus status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Raison du rejet (optionnel)
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // Date de validation/rejet par le gestionnaire
    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    // Entrepôt d'où sortent les marchandises
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    // Magasinier qui a créé le bon
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    // Gestionnaire qui a validé ou rejeté
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by_id")
    private User validatedBy;

    @OneToMany(mappedBy = "dispatch", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<DispatchLine> lines = new HashSet<>();
}
