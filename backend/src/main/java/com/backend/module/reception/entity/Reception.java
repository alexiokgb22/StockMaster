package com.backend.module.reception.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.backend.module.purchaseorder.entity.PurchaseOrder;
import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.shared.enums.ReceptionStatus;
import com.backend.module.user.entity.User;
import com.backend.module.warehouse.entity.Warehouse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "receptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Reception extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "reception_number", nullable = false, unique = true, length = 100)
    private String receptionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReceptionStatus status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Date de validation par le gestionnaire
    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    // Raison du rejet (optionnel)
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // La commande fournisseur à laquelle ce bon est rattaché
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    // Entrepôt de réception
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

    @OneToMany(mappedBy = "reception", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<ReceptionLine> lines = new HashSet<>();
}
