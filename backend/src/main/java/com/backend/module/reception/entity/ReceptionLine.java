package com.backend.module.reception.entity;

import com.backend.module.purchaseorderline.entity.PurchaseOrderLine;
import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.zone.entity.Zone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "reception_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReceptionLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Quantité attendue (copiée depuis PurchaseOrderLine.quantity au moment de la création)
    @Column(name = "quantity_expected", nullable = false)
    private Integer quantityExpected;

    // Quantité réellement reçue, saisie par le magasinier
    @Column(name = "quantity_received", nullable = false)
    private Integer quantityReceived;

    @Column(name = "note")
    private String note;

    // Lien vers le bon de réception parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reception_id", nullable = false)
    private Reception reception;

    // Ligne de commande d'origine — permet de remonter au produit et à la commande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_line_id", nullable = false)
    private PurchaseOrderLine purchaseOrderLine;

    // Zone dans laquelle le magasinier range physiquement les produits
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    // Écart : négatif = manquant, positif = surplus
    public int getGap() {
        return quantityReceived - quantityExpected;
    }
}
