package com.backend.module.warehouse.entity;

import java.util.HashSet;
import java.util.Set;

import com.backend.module.activityreport.entity.ActivityReport;
import com.backend.module.auditreport.entity.AuditReport;
import com.backend.module.category.entity.Category;
import com.backend.module.inventory.entity.Inventory;
import com.backend.module.purchaseorder.entity.PurchaseOrder;
import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.stock.entity.Stock;
import com.backend.module.user.entity.User;
import com.backend.module.zone.entity.Zone;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Warehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "total_capacity")
    private Double totalCapacity;

    @Column(name = "used_capacity")
    private Double usedCapacity;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // Le gestionnaire responsable de cet entrepôt
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    // --- Relations inverses ---

    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Zone> zones = new HashSet<>();

    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Stock> stocks = new HashSet<>();

    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<PurchaseOrder> purchaseOrders = new HashSet<>();

    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Inventory> inventories = new HashSet<>();

    // Magasiniers affectés à cet entrepôt
    @OneToMany(mappedBy = "assignedWarehouse", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<User> storekeepers = new HashSet<>();

    // Rapports d'activité des magasiniers de cet entrepôt
    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<ActivityReport> activityReports = new HashSet<>();

    // Rapports d'audit portant sur cet entrepôt
    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<AuditReport> auditReports = new HashSet<>();

    // Catégories affectées à cet entrepôt
    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Category> categories = new HashSet<>();
}
