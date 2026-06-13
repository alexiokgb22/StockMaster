package com.backend.module.permission.service;

import java.util.List;

public final class PermissionCatalog {

    private PermissionCatalog() {
    }

    public static List<PermissionDefinition> defaultDefinitions() {
        return List.of(
            new PermissionDefinition("user", "read", "user.read", "Lire les utilisateurs", "Consulter les utilisateurs et leurs profils"),
            new PermissionDefinition("user", "create", "user.create", "Créer un utilisateur", "Créer de nouveaux utilisateurs"),
            new PermissionDefinition("user", "update", "user.update", "Modifier un utilisateur", "Modifier les informations utilisateur"),
            new PermissionDefinition("user", "disable", "user.disable", "Désactiver un utilisateur", "Désactiver un compte utilisateur"),
            new PermissionDefinition("user", "reset_password", "user.reset_password", "Réinitialiser un mot de passe", "Réinitialiser le mot de passe d’un utilisateur"),
            new PermissionDefinition("user", "assign_role", "user.assign_role", "Attribuer un rôle", "Assigner des rôles à des utilisateurs"),
            new PermissionDefinition("warehouse", "read", "warehouse.read", "Lire les entrepôts", "Consulter la liste et les détails des entrepôts"),
            new PermissionDefinition("warehouse", "create", "warehouse.create", "Créer un entrepôt", "Ajouter un nouvel entrepôt"),
            new PermissionDefinition("warehouse", "update", "warehouse.update", "Modifier un entrepôt", "Mettre à jour les informations d’un entrepôt"),
            new PermissionDefinition("warehouse", "disable", "warehouse.disable", "Désactiver un entrepôt", "Désactiver un entrepôt"),
            new PermissionDefinition("zone", "read", "zone.read", "Lire les zones", "Consulter les zones d’un entrepôt"),
            new PermissionDefinition("zone", "create", "zone.create", "Créer une zone", "Créer une zone de stockage"),
            new PermissionDefinition("zone", "update", "zone.update", "Modifier une zone", "Modifier les informations d’une zone"),
            new PermissionDefinition("product", "read", "product.read", "Lire les produits", "Consulter les produits"),
            new PermissionDefinition("product", "create", "product.create", "Créer un produit", "Ajouter un nouveau produit"),
            new PermissionDefinition("product", "update", "product.update", "Modifier un produit", "Mettre à jour un produit"),
            new PermissionDefinition("product", "delete_logic", "product.delete_logic", "Suppression logique", "Désactiver un produit sans le supprimer physiquement"),
            new PermissionDefinition("category", "read", "category.read", "Lire les catégories", "Consulter les catégories"),
            new PermissionDefinition("category", "create", "category.create", "Créer une catégorie", "Ajouter une catégorie"),
            new PermissionDefinition("category", "update", "category.update", "Modifier une catégorie", "Mettre à jour une catégorie"),
            new PermissionDefinition("supplier", "read", "supplier.read", "Lire les fournisseurs", "Consulter les fournisseurs"),
            new PermissionDefinition("supplier", "create", "supplier.create", "Créer un fournisseur", "Ajouter un fournisseur"),
            new PermissionDefinition("supplier", "update", "supplier.update", "Modifier un fournisseur", "Mettre à jour un fournisseur"),
            new PermissionDefinition("stock", "read", "stock.read", "Consulter les stocks", "Voir les stocks disponibles"),
            new PermissionDefinition("stock", "view_history", "stock.view_history", "Voir l’historique", "Consulter l’historique des mouvements"),
            new PermissionDefinition("stock", "update", "stock.update", "Mettre à jour le stock", "Modifier les quantités en stock"),
            new PermissionDefinition("stock", "configure_thresholds", "stock.configure_thresholds", "Configurer les seuils", "Définir les seuils minimum et maximum"),
            new PermissionDefinition("receipt", "create", "receipt.create", "Créer une réception", "Créer un bon de réception"),
            new PermissionDefinition("receipt", "validate", "receipt.validate", "Valider une réception", "Valider une réception de marchandises"),
            new PermissionDefinition("receipt", "quality_control", "receipt.quality_control", "Contrôle qualité", "Valider ou rejeter un contrôle qualité"),
            new PermissionDefinition("dispatch", "create", "dispatch.create", "Créer une sortie", "Créer un bon de sortie"),
            new PermissionDefinition("dispatch", "validate", "dispatch.validate", "Valider une sortie", "Valider une sortie de stock"),
            new PermissionDefinition("dispatch", "print_bordereau", "dispatch.print_bordereau", "Imprimer un bordereau", "Générer un bordereau de sortie"),
            new PermissionDefinition("transfer", "create", "transfer.create", "Créer un transfert", "Créer un transfert entre entrepôts"),
            new PermissionDefinition("transfer", "validate", "transfer.validate", "Valider un transfert", "Valider un transfert"),
            new PermissionDefinition("transfer", "receive", "transfer.receive", "Recevoir un transfert", "Confirmer la réception d’un transfert"),
            new PermissionDefinition("transfer", "cancel", "transfer.cancel", "Annuler un transfert", "Annuler un transfert"),
            new PermissionDefinition("inventory", "create", "inventory.create", "Créer un inventaire", "Créer un inventaire"),
            new PermissionDefinition("inventory", "start", "inventory.start", "Démarrer un inventaire", "Démarrer la comptage d’un inventaire"),
            new PermissionDefinition("inventory", "complete", "inventory.complete", "Clore un inventaire", "Finaliser un inventaire"),
            new PermissionDefinition("inventory", "view_gap", "inventory.view_gap", "Voir les écarts", "Consulter les écarts théorie/réalité"),
            new PermissionDefinition("alert", "view", "alert.view", "Voir les alertes", "Consulter les alertes système"),
            new PermissionDefinition("alert", "manage", "alert.manage", "Gérer les alertes", "Gérer les règles et notifications"),
            new PermissionDefinition("dashboard", "view", "dashboard.view", "Voir le tableau de bord", "Afficher les KPI et graphiques"),
            new PermissionDefinition("report", "view", "report.view", "Voir les rapports", "Consulter les rapports"),
            new PermissionDefinition("report", "export", "report.export", "Exporter les rapports", "Exporter les rapports en PDF/Excel/CSV"),
            new PermissionDefinition("audit", "view", "audit.view", "Voir l’audit", "Consulter la traçabilité des actions"),
            new PermissionDefinition("barcode", "generate", "barcode.generate", "Générer un code-barres", "Créer un code-barres pour un produit"),
            new PermissionDefinition("barcode", "scan", "barcode.scan", "Scanner un code-barres", "Scanner un produit ou un emplacement")
        );
    }
}
