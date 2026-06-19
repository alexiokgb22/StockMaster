package com.backend.module.user.entity;

import java.util.HashSet;
import java.util.Set;

import com.backend.module.activityreport.entity.ActivityReport;
import com.backend.module.auditreport.entity.AuditReport;
import com.backend.module.auditsession.entity.AuditSession;
import com.backend.module.inventory.entity.Inventory;
import com.backend.module.purchaseorder.entity.PurchaseOrder;
import com.backend.module.role.entity.Role;
import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.stockmovement.entity.StockMovement;
import com.backend.module.transfer.entity.Transfer;
import com.backend.module.warehouse.entity.Warehouse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // Forcé à true à la création — l'utilisateur doit changer son mot de passe à la première connexion
    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Entrepôt d'affectation du magasinier.
    // Null pour les autres rôles (admin, gestionnaire, auditeur).
    // Permet au service de vérifier le périmètre du gestionnaire
    // lors de la création/gestion des magasiniers.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse assignedWarehouse;

    // --- Relations inverses ---

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<StockMovement> stockMovements = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<PurchaseOrder> purchaseOrders = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Transfer> transfers = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Inventory> inventories = new HashSet<>();

    // Entrepôts dont cet utilisateur est le gestionnaire responsable
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Warehouse> managedWarehouses = new HashSet<>();

    // Rapports d'activité rédigés par ce magasinier
    @OneToMany(mappedBy = "storekeeper", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<ActivityReport> activityReports = new HashSet<>();

    // Sessions d'audit lancées par cet auditeur
    @OneToMany(mappedBy = "auditor", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<AuditSession> auditSessions = new HashSet<>();

    // Rapports d'audit rédigés par cet auditeur
    @OneToMany(mappedBy = "auditor", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<AuditReport> auditReports = new HashSet<>();
}
