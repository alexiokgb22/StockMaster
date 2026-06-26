package com.backend.module.shared.enums;

public enum DispatchStatus {
    PENDING,    // Créé par le magasinier, en attente de validation
    VALIDATED,  // Validé par le gestionnaire → stock décrémenté
    REJECTED    // Rejeté par le gestionnaire → stock inchangé
}
