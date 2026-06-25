package com.backend.module.shared.enums;

public enum PurchaseOrderStatus {
    DRAFT,      // Demande créée par le gestionnaire
    VALIDATED,  // Admin a validé et assigné un fournisseur
    DELIVERED,  // Admin a marqué la livraison comme arrivée
    CLOSED,     // Gestionnaire a confirmé la réception du stock
    CANCELLED   // Annulée (avant DELIVERED seulement)
}
