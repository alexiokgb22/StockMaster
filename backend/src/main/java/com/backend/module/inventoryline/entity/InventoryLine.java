package com.backend.module.inventoryline.entity;

import com.backend.module.inventory.entity.Inventory;
import com.backend.module.product.entity.Product;
import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.stock.entity.Stock;
import com.backend.module.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "inventory_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InventoryLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Quantité théorique snapshotée au moment de la création de l'inventaire
    @Column(name = "theoretical_qty", nullable = false)
    private Integer theoreticalQty;

    // Quantité physique saisie par le magasinier (null tant que non saisie)
    @Column(name = "actual_qty")
    private Integer actualQty;

    @Column(name = "note")
    private String note;

    // Inventaire parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    // Ligne de stock concernée — contient le produit ET la zone
    // Lien direct vers Stock pour snapshot (product_id, zone_id, warehouse_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // Dénormalisation du produit pour accès rapide sans JOIN sur stock
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Dénormalisation de la zone pour accès rapide
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    /**
     * Écart = physique - théorique.
     * Négatif = perte/manquant. Positif = surplus.
     * Null si actualQty pas encore saisie.
     */
    public Integer getGap() {
        if (actualQty == null) return null;
        return actualQty - theoreticalQty;
    }

    public boolean isCounted() {
        return actualQty != null;
    }
}
