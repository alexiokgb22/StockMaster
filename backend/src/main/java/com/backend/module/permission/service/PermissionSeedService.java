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
import com.backend.module.role.entity.Role;
import com.backend.module.role.repository.RoleRepository;
import com.backend.module.permission.repository.PermissionRepository;

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
        List<PermissionDefinition> definitions = PermissionCatalog.defaultDefinitions();

        Map<String, Permission> existingPermissions = permissionRepository.findAll().stream()
            .collect(Collectors.toMap(Permission::getCode, permission -> permission));

        Set<Permission> persistedPermissions = new HashSet<>();

        for (PermissionDefinition definition : definitions) {
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

        seedRolePermissions(persistedPermissions);
    }

    @Transactional
    public void seedRolePermissions(Set<Permission> persistedPermissions) {
        Role adminRole = roleRepository.findByName("Administrateur").orElseGet(() -> createRole("Administrateur", "Accès complet au système"));
        Role warehouseManagerRole = roleRepository.findByName("Gestionnaire d'entrepôt").orElseGet(() -> createRole("Gestionnaire d'entrepôt", "Gestion opérationnelle des entrepôts"));
        Role storeKeeperRole = roleRepository.findByName("Magasinier").orElseGet(() -> createRole("Magasinier", "Gestion quotidienne des stocks"));
        Role auditorRole = roleRepository.findByName("Auditeur").orElseGet(() -> createRole("Auditeur", "Consultation et contrôle"));

        assignPermissions(adminRole, persistedPermissions);
        assignPermissions(warehouseManagerRole, filterPermissions(persistedPermissions, List.of(
            "warehouse.read", "warehouse.create", "warehouse.update",
            "zone.read", "zone.create", "zone.update",
            "product.read", "product.create", "product.update", "product.delete_logic",
            "category.read", "category.create", "category.update",
            "supplier.read", "supplier.create", "supplier.update",
            "stock.read", "stock.view_history", "stock.update", "stock.configure_thresholds",
            "receipt.create", "receipt.validate", "receipt.quality_control",
            "dispatch.create", "dispatch.validate", "dispatch.print_bordereau",
            "transfer.create", "transfer.validate", "transfer.receive", "transfer.cancel",
            "inventory.create", "inventory.start", "inventory.complete", "inventory.view_gap",
            "alert.view", "alert.manage",
            "dashboard.view",
            "report.view", "report.export",
            "audit.view"
        )));

        assignPermissions(storeKeeperRole, filterPermissions(persistedPermissions, List.of(
            "stock.read", "stock.view_history", "stock.update",
            "receipt.create", "receipt.validate",
            "dispatch.create", "dispatch.validate",
            "transfer.create", "transfer.receive",
            "inventory.create", "inventory.start", "inventory.complete",
            "barcode.scan"
        )));

        assignPermissions(auditorRole, filterPermissions(persistedPermissions, List.of(
            "stock.read", "stock.view_history",
            "dashboard.view",
            "report.view", "report.export",
            "audit.view"
        )));
    }

    private Role createRole(String name, String description) {
        Role role = Role.builder()
            .name(name)
            .description(description)
            .isActive(true)
            .build();
        return roleRepository.save(role);
    }

    private void assignPermissions(Role role, Set<Permission> permissions) {
        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
    }

    private Set<Permission> filterPermissions(Set<Permission> persistedPermissions, List<String> codes) {
        return persistedPermissions.stream()
            .filter(permission -> codes.contains(permission.getCode()))
            .collect(Collectors.toSet());
    }
}
