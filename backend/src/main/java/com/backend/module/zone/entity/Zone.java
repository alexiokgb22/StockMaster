package com.backend.module.zone.entity;

import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.shared.enums.ZoneType;
import com.backend.module.stock.entity.Stock;
import com.backend.module.stockmovement.entity.StockMovement;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Zone extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "capacity")
    private Double capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type", nullable = false)
    private ZoneType zoneType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Stock> stocks = new HashSet<>();

    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<StockMovement> stockMovements = new HashSet<>();
}
