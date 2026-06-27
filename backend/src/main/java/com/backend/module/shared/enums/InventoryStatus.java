package com.backend.module.shared.enums;

public enum InventoryStatus {
    IN_PROGRESS,   // En cours — magasinier saisit les quantités
    COMPLETED,     // Clôturé — ajustements appliqués au stock
    CANCELLED      // Annulé — aucun ajustement
}
