package com.backend.module.product.entity;

import java.util.HashSet;
import java.util.Set;

import com.backend.module.category.entity.Category;
import com.backend.module.inventoryline.entity.InventoryLine;
import com.backend.module.purchaseorderline.entity.PurchaseOrderLine;
import com.backend.module.shared.entity.BaseEntity;
import com.backend.module.stock.entity.Stock;
import com.backend.module.stockmovement.entity.StockMovement;
import com.backend.module.transfer.entity.TransferLine;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "products",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_products_reference", columnNames = "reference"),
        @UniqueConstraint(name = "uk_products_barcode", columnNames = "barcode")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    @Column(name = "barcode", nullable = false, unique = true, length = 100)
    private String barcode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "purchase_price")
    private Double purchasePrice;

    @Column(name = "sale_price")
    private Double salePrice;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    // Entrepôts dans lesquels ce produit est disponible.
    // Table de liaison : product_warehouses (product_id, warehouse_id)
    // L'admin choisit explicitement les entrepôts à la création ou après coup.
    // Le gestionnaire crée dans son entrepôt → son entrepôt est ajouté automatiquement.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_warehouses",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "warehouse_id")
    )
    @JsonIgnore
    @Builder.Default
    private Set<Warehouse> warehouses = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Stock> stocks = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<StockMovement> stockMovements = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<PurchaseOrderLine> purchaseOrderLines = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<TransferLine> transferLines = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<InventoryLine> inventoryLines = new HashSet<>();
}
