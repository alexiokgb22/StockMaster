package com.backend.module.permission.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.module.permission.entity.Permission;
import com.backend.module.permission.repository.PermissionRepository;
import com.backend.module.role.entity.Role;
import com.backend.module.role.repository.RoleRepository;

@Service
public class PermissionSeedService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionSeedService(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedPermissionsAndRoles() {
        // 1. Synchroniser le catalogue de permissions avec la base
        Map<String, Permission> existingPermissions = permissionRepository.findAll().stream()
            .collect(Collectors.toMap(Permission::getCode, p -> p));

        Set<Permission> persistedPermissions = new HashSet<>();

        for (PermissionDefinition definition : PermissionCatalog.defaultDefinitions()) {
            Permission permission = existingPermissions.get(definition.code());
            if (permission == null) {
                permission = Permission.builder()
                    .code(definition.code())
                    .name(definition.name())
                    .module(definition.module())
                    .description(definition.description())
                    .isActive(true)
                    .build();
                permission = permissionRepository.save(permission);
            }
            persistedPermissions.add(permission);
        }

        // 2. Créer les rôles et leur affecter les permissions
        seedRoles(persistedPermissions);
    }

    private void seedRoles(Set<Permission> all) {

        // Administrateur
        // Accès total : toutes les permissions sans exception.
        // Seul à pouvoir créer des entrepôts, des gestionnaires, des auditeurs,
        // désactiver n'importe quel compte, et lire les rapports d'audit.
        Role adminRole = findOrCreateRole("Administrateur", "Accès complet au système");
        assignPermissions(adminRole, all);

        // Gestionnaire d'entrepôt
        // Pilote, configure et supervise son entrepôt. Valide les opérations.
        // Peut créer des magasiniers et activer/désactiver leurs comptes (périmètre
        // limité à son entrepôt, vérifié en logique métier).
        // Génère les rapports opérationnels. Lit les rapports d'activité des magasiniers.
        Role managerRole = findOrCreateRole("Gestionnaire d'entrepôt", "Responsable opérationnel d'un entrepôt");
        assignPermissions(managerRole, filter(all, List.of(
            // Gestion des magasiniers de son entrepôt uniquement
            "user.create_storekeeper",
            "user.toggle_storekeeper",
            // Entrepôt (son périmètre, lecture et modification)
            "warehouse.read", "warehouse.update",
            // Zones
            "zone.read", "zone.create", "zone.update",
            // Référentiel produits / catégories / fournisseurs
            "product.read", "product.create", "product.update", "product.delete_logic",
            "category.read", "category.create", "category.update",
            "supplier.read", "supplier.create", "supplier.update",
            // Stocks (lecture + ajustement manuel + seuils)
            "stock.read", "stock.view_history", "stock.update", "stock.configure_thresholds",
            // Réceptions (création + validation + contrôle qualité)
            "receipt.create", "receipt.validate", "receipt.quality_control",
            // Sorties (création + validation + bordereau)
            "dispatch.create", "dispatch.validate", "dispatch.print_bordereau",
            // Transferts (création + validation + réception + annulation)
            "transfer.create", "transfer.validate", "transfer.receive", "transfer.cancel",
            // Inventaires (création + démarrage + clôture + écarts)
            "inventory.create", "inventory.start", "inventory.complete", "inventory.view_gap",
            // Alertes
            "alert.view", "alert.manage",
            // Tableau de bord et rapports opérationnels
            "dashboard.view",
            "report.create", "report.view", "report.export",
            // Traçabilité technique (consultation)
            "audit.view",
            // Rapports d'activité des magasiniers (lecture)
            "activity_report.view",
            // Codes-barres
            "barcode.generate"
        )));

        // Magasinier
        // Opérateur terrain. Exécute les tâches physiques.
        // Rédige des comptes-rendus de ses propres opérations uniquement.
        // Ne valide rien, ne configure rien, n'accède pas aux rapports globaux.
        Role storeKeeperRole = findOrCreateRole("Magasinier", "Opérateur terrain chargé des tâches physiques de stockage");
        assignPermissions(storeKeeperRole, filter(all, List.of(
            // Consultation du stock et historique
            "stock.read", "stock.view_history",
            // Réceptions (création uniquement, validation réservée au gestionnaire)
            "receipt.create",
            // Sorties (création + bordereau)
            "dispatch.create", "dispatch.print_bordereau",
            // Transferts (création et réception)
            "transfer.create", "transfer.receive",
            // Inventaires (participation au comptage)
            "inventory.start",
            // Rapport d'activité terrain (ses propres opérations)
            "activity_report.create",
            // Codes-barres (scan terrain)
            "barcode.scan"
        )));

        // Auditeur
        // Commissaire aux comptes. Accès en lecture seule à l'état des entrepôts,
        // des stocks et des mouvements. Lance des sessions d'audit, rédige et soumet
        // des rapports d'audit à l'admin. Peut lire les rapports d'activité
        // des magasiniers dans le cadre de ses missions.
        Role auditorRole = findOrCreateRole("Auditeur", "Commissaire chargé de l'audit des entrepôts");
        assignPermissions(auditorRole, filter(all, List.of(
            // Lecture de l'état des entrepôts, zones, produits, fournisseurs
            "warehouse.read",
            "zone.read",
            "product.read",
            "category.read",
            "supplier.read",
            // Stocks et historique des mouvements
            "stock.read", "stock.view_history",
            // Écarts d'inventaire
            "inventory.view_gap",
            // Tableau de bord (KPI globaux)
            "dashboard.view",
            // Traçabilité technique (journal des actions)
            "audit.view",
            // Session d'audit + rédaction et soumission du rapport
            "audit.start",
            "audit_report.create", "audit_report.submit",
            // Rapports d'activité des magasiniers (dans le cadre de l'audit)
            "activity_report.view"
        )));
    }

    // Helpers

    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = Role.builder()
                .name(name)
                .description(description)
                .isActive(true)
                .build();
            return roleRepository.save(role);
        });
    }

    private void assignPermissions(Role role, Set<Permission> permissions) {
        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
    }

    private Set<Permission> filter(Set<Permission> all, List<String> codes) {
        return all.stream()
            .filter(p -> codes.contains(p.getCode()))
            .collect(Collectors.toSet());
    }
}
