package com.backend.module.shared.enums;

public enum TransferStatus {
    PENDING,    // Créé par le gestionnaire source — en attente de validation admin
    VALIDATED,  // Validé par l'admin → stock source décrémenté + quantityInTransit incrémenté
    RECEIVED,   // Confirmé par le gestionnaire cible → stock cible incrémenté + quantityInTransit décrémenté
    CANCELLED   // Annulé par l'admin (rollback si VALIDATED)
}
