package com.backend.module.shared.enums;

public enum ReceptionStatus {
    PENDING,    // Bon créé par le magasinier, en attente de validation
    VALIDATED,  // Gestionnaire a validé — stock mis à jour
    REJECTED    // Gestionnaire a rejeté — commande repasse à DELIVERED
}
