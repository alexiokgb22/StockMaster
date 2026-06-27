package com.backend.module.transfer.entity;

import com.backend.module.product.entity.Product;
import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "transfer_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransferLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "note")
    private String note;

    // Transfert parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;

    // Produit transféré
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Zone source (d'où le produit sort)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_zone_id", nullable = false)
    private Zone sourceZone;

    // Zone cible (où le produit arrive) — peut être null à la création,
    // renseignée par le gestionnaire cible au moment de la réception
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_zone_id")
    private Zone targetZone;
}
