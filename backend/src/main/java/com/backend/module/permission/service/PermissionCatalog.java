package com.backend.module.permission.service;

import java.util.List;

public final class PermissionCatalog {

    private PermissionCatalog() {
    }

    public static List<PermissionDefinition> defaultDefinitions() {
        return List.of(

            // Utilisateurs
            // user.create / user.toggle / user.assign_role : admin uniquement (tous rôles, tous comptes)
            // user.create_storekeeper / user.toggle_storekeeper : gestionnaire (ses magasiniers uniquement,
            //   périmètre vérifié en logique métier)
            new PermissionDefinition("user", "read",                 "user.read",                 "Lire les utilisateurs",                  "Consulter les utilisateurs et leurs profils"),
            new PermissionDefinition("user", "create",               "user.create",               "Créer un utilisateur",                   "Créer un utilisateur avec n'importe quel rôle (admin uniquement)"),
            new PermissionDefinition("user", "update",               "user.update",               "Modifier un utilisateur",                "Modifier les informations d'un utilisateur"),
            new PermissionDefinition("user", "toggle",               "user.toggle",               "Activer / désactiver un utilisateur",    "Activer ou désactiver n'importe quel compte (admin uniquement)"),
            new PermissionDefinition("user", "delete",               "user.delete",               "Supprimer un utilisateur",               "Supprimer un utilisateur (admin uniquement)"),
            new PermissionDefinition("user", "reset_password",       "user.reset_password",       "Réinitialiser un mot de passe",          "Réinitialiser le mot de passe d'un utilisateur"),
            new PermissionDefinition("user", "assign_role",          "user.assign_role",          "Attribuer un rôle",                      "Assigner n'importe quel rôle à un utilisateur (admin uniquement)"),
            new PermissionDefinition("user", "create_storekeeper",   "user.create_storekeeper",   "Créer un magasinier",                    "Créer un compte magasinier dans son entrepôt (gestionnaire uniquement, périmètre vérifié en service)"),
            new PermissionDefinition("user", "toggle_storekeeper",   "user.toggle_storekeeper",   "Activer / désactiver un magasinier",     "Activer ou désactiver un magasinier de son entrepôt (gestionnaire uniquement, périmètre vérifié en service)"),

            // Entrepôts
            // warehouse.create / warehouse.disable : admin uniquement
            // warehouse.update / warehouse.read : gestionnaire aussi
            new PermissionDefinition("warehouse", "read",    "warehouse.read",    "Lire les entrepôts",     "Consulter la liste et les détails des entrepôts"),
            new PermissionDefinition("warehouse", "create",  "warehouse.create",  "Créer un entrepôt",      "Ajouter un nouvel entrepôt (admin uniquement)"),
            new PermissionDefinition("warehouse", "update",  "warehouse.update",  "Modifier un entrepôt",   "Mettre à jour les informations d'un entrepôt"),
            new PermissionDefinition("warehouse", "disable", "warehouse.disable", "Désactiver un entrepôt", "Désactiver un entrepôt (admin uniquement)"),

            // Zones
            new PermissionDefinition("zone", "read",   "zone.read",   "Lire les zones",    "Consulter les zones d'un entrepôt"),
            new PermissionDefinition("zone", "create", "zone.create", "Créer une zone",    "Créer une zone de stockage"),
            new PermissionDefinition("zone", "update", "zone.update", "Modifier une zone", "Modifier les informations d'une zone"),

            // Produits
            new PermissionDefinition("product", "read",         "product.read",         "Lire les produits",   "Consulter les produits"),
            new PermissionDefinition("product", "create",       "product.create",       "Créer un produit",    "Ajouter un nouveau produit"),
            new PermissionDefinition("product", "update",       "product.update",       "Modifier un produit", "Mettre à jour un produit"),
            new PermissionDefinition("product", "delete_logic", "product.delete_logic", "Suppression logique", "Désactiver un produit sans le supprimer physiquement"),

            // Catégories
            new PermissionDefinition("category", "read",   "category.read",   "Lire les catégories",    "Consulter les catégories"),
            new PermissionDefinition("category", "create", "category.create", "Créer une catégorie",    "Ajouter une catégorie"),
            new PermissionDefinition("category", "update", "category.update", "Modifier une catégorie", "Mettre à jour une catégorie"),
            new PermissionDefinition("category", "delete", "category.delete", "Supprimer une catégorie","Supprimer une catégorie sans produits rattachés"),

            // Fournisseurs
            new PermissionDefinition("supplier", "read",   "supplier.read",   "Lire les fournisseurs",   "Consulter les fournisseurs"),
            new PermissionDefinition("supplier", "create", "supplier.create", "Créer un fournisseur",    "Ajouter un fournisseur"),
            new PermissionDefinition("supplier", "update", "supplier.update", "Modifier un fournisseur", "Mettre à jour un fournisseur"),

            // Stocks
            new PermissionDefinition("stock", "read",                 "stock.read",                 "Consulter les stocks",   "Voir les stocks disponibles"),
            new PermissionDefinition("stock", "view_history",         "stock.view_history",         "Voir l'historique",      "Consulter l'historique des mouvements"),
            new PermissionDefinition("stock", "update",               "stock.update",               "Mettre à jour le stock", "Ajuster manuellement les quantités en stock (gestionnaire uniquement)"),
            new PermissionDefinition("stock", "configure_thresholds", "stock.configure_thresholds", "Configurer les seuils",  "Définir les seuils minimum et maximum de stock"),

            // Réceptions
            new PermissionDefinition("receipt", "create",          "receipt.create",          "Créer une réception",   "Créer un bon de réception"),
            new PermissionDefinition("receipt", "validate",        "receipt.validate",        "Valider une réception", "Valider une réception de marchandises (gestionnaire uniquement)"),
            new PermissionDefinition("receipt", "quality_control", "receipt.quality_control", "Contrôle qualité",      "Valider ou rejeter un contrôle qualité (gestionnaire uniquement)"),

            // Sorties / Dispatches
            new PermissionDefinition("dispatch", "create",          "dispatch.create",          "Créer une sortie",      "Créer un bon de sortie"),
            new PermissionDefinition("dispatch", "validate",        "dispatch.validate",        "Valider une sortie",    "Valider une sortie de stock (gestionnaire uniquement)"),
            new PermissionDefinition("dispatch", "print_bordereau", "dispatch.print_bordereau", "Imprimer un bordereau", "Générer un bordereau de sortie"),

            // Transferts
            new PermissionDefinition("transfer", "create",   "transfer.create",   "Créer un transfert",    "Créer un transfert entre entrepôts"),
            new PermissionDefinition("transfer", "validate", "transfer.validate", "Valider un transfert",  "Valider un transfert (gestionnaire uniquement)"),
            new PermissionDefinition("transfer", "receive",  "transfer.receive",  "Recevoir un transfert", "Confirmer la réception d'un transfert"),
            new PermissionDefinition("transfer", "cancel",   "transfer.cancel",   "Annuler un transfert",  "Annuler un transfert (gestionnaire uniquement)"),

            // Inventaires
            new PermissionDefinition("inventory", "create",   "inventory.create",   "Créer un inventaire",    "Initier un inventaire (gestionnaire uniquement)"),
            new PermissionDefinition("inventory", "start",    "inventory.start",    "Démarrer un inventaire", "Démarrer le comptage d'un inventaire"),
            new PermissionDefinition("inventory", "complete", "inventory.complete", "Clore un inventaire",    "Finaliser et valider un inventaire (gestionnaire uniquement)"),
            new PermissionDefinition("inventory", "view_gap", "inventory.view_gap", "Voir les écarts",        "Consulter les écarts théorie/réalité"),

            // Alertes
            new PermissionDefinition("alert", "view",   "alert.view",   "Voir les alertes",  "Consulter les alertes système"),
            new PermissionDefinition("alert", "manage", "alert.manage", "Gérer les alertes", "Configurer les règles et seuils de déclenchement des alertes"),

            // Tableau de bord
            new PermissionDefinition("dashboard", "view", "dashboard.view", "Voir le tableau de bord", "Afficher les KPI et graphiques de performance"),

            // Rapports opérationnels (mouvements, stocks, fournisseurs...)
            // Le gestionnaire génère et exporte ; le magasinier n'y a pas accès
            new PermissionDefinition("report", "create", "report.create", "Générer un rapport",    "Créer un rapport opérationnel (gestionnaire uniquement)"),
            new PermissionDefinition("report", "view",   "report.view",   "Voir les rapports",     "Consulter les rapports opérationnels"),
            new PermissionDefinition("report", "export", "report.export", "Exporter les rapports", "Exporter les rapports en PDF/Excel/CSV"),

            // Traçabilité technique (journal des actions système)
            new PermissionDefinition("audit", "view",  "audit.view",  "Voir la traçabilité", "Consulter le journal des actions effectuées dans le système"),
            new PermissionDefinition("audit", "start", "audit.start", "Lancer un audit",     "Initier une session d'audit sur un ou plusieurs entrepôts (auditeur uniquement)"),

            // Rapports d'audit (commissaire aux comptes)
            // L'auditeur crée et soumet ; seul l'admin peut lire les rapports soumis
            new PermissionDefinition("audit_report", "create", "audit_report.create", "Rédiger un rapport d'audit",   "Rédiger un rapport suite à une session d'audit (auditeur uniquement)"),
            new PermissionDefinition("audit_report", "submit", "audit_report.submit", "Soumettre un rapport d'audit", "Finaliser et soumettre un rapport d'audit à l'administrateur (auditeur uniquement)"),
            new PermissionDefinition("audit_report", "view",   "audit_report.view",   "Lire les rapports d'audit",    "Consulter les rapports d'audit soumis par les auditeurs (admin uniquement)"),

            // Rapport d'activité terrain (magasinier)
            // Le magasinier rédige un compte-rendu de ses propres opérations.
            // Le gestionnaire et l'auditeur peuvent le consulter.
            new PermissionDefinition("activity_report", "create", "activity_report.create", "Rédiger un rapport d'activité", "Écrire un compte-rendu de ses propres opérations terrain (entrées, sorties, transferts)"),
            new PermissionDefinition("activity_report", "view",   "activity_report.view",   "Lire les rapports d'activité",  "Consulter les rapports d'activité des magasiniers (gestionnaire et auditeur uniquement)"),

            // Codes-barres
            new PermissionDefinition("barcode", "generate", "barcode.generate", "Générer un code-barres", "Créer un code-barres ou QR code pour un produit"),
            new PermissionDefinition("barcode", "scan",     "barcode.scan",     "Scanner un code-barres", "Scanner un produit ou un emplacement")
        );
    }
}
