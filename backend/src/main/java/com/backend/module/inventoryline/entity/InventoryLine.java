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

    /** Quantité théorique au moment de la création de l'inventaire (copie de stock.quantityAvailable). */
    @Column(name = "theoretical_qty")
    private Integer theoreticalQty;

    /** Quantité physique saisie par le magasinier pendant le comptage. */
    @Column(name = "actual_qty")
    private Integer actualQty;

    @Column(name = "note", length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    /**
     * Lien direct vers la ligne de stock concernée.
     * Permet de mettre à jour précisément le bon stock (produit × entrepôt × zone)
     * lors de la clôture de l'inventaire.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /**
     * Dénormalisation du produit pour affichage rapide
     * sans recharger la ligne de stock.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Zone de stockage — dénormalisée ici pour affichage rapide
     * sans recharger la ligne de stock.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    /**
     * Calcule l'écart entre quantité physique et quantité théorique.
     * Retourne null si le comptage n'a pas encore été saisi.
     */
    public Integer getGap() {
        if (actualQty == null) return null;
        return actualQty - theoreticalQty;
    }

    /**
     * Indique si cette ligne a été saisie par le magasinier.
     * Une ligne est considérée "comptée" dès que actualQty est renseignée.
     */
    public boolean isCounted() {
        return actualQty != null;
    }
}
