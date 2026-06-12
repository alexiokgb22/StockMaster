package com.backend.module.inventory.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.backend.module.inventoryline.entity.InventoryLine;
import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.shared.enums.InventoryStatus;
import com.backend.module.shared.enums.InventoryType;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_type", nullable = false)
    private InventoryType inventoryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_status", nullable = false)
    private InventoryStatus inventoryStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "inventory", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<InventoryLine> lines = new HashSet<>();
}
