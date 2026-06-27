package com.backend.module.alert.entity;

import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.shared.enums.AlertSeverity;
import com.backend.module.shared.enums.AlertType;
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
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Alert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private AlertSeverity severity;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    // Clé de déduplication — ex : "STOCK_BELOW_MIN-stockId-42"
    // Empêche le scheduler de créer plusieurs alertes actives pour le même événement
    @Column(name = "dedup_key", nullable = false, length = 200)
    private String dedupKey;

    // Références optionnelles selon le type
    @Column(name = "stock_id")
    private Long stockId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "zone_name", length = 100)
    private String zoneName;

    // L'entrepôt concerné (toujours renseigné)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved;
}
