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
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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

    // Numéro unique auto-généré : INV-{yyyyMMdd}-{4 chiffres}
    @Column(name = "inventory_number", nullable = false, unique = true, length = 100)
    private String inventoryNumber;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Date de démarrage effectif (= createdAt pour l'instant, distinct si workflow en 2 étapes)
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    // Date de clôture
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

    // Gestionnaire qui a créé + clôturé
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "inventory", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<InventoryLine> lines = new HashSet<>();
}
