package com.backend.module.permission.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.module.permission.entity.Permission;
import com.backend.module.permission.repository.PermissionRepository;
import com.backend.module.role.entity.Role;
import com.backend.module.role.repository.RoleRepository;

@Service
public class RolePermissionInitializer {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public RolePermissionInitializer(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void initializeDefaults() {
        for (PermissionDefinition definition : PermissionCatalog.defaultDefinitions()) {
            permissionRepository.findByCode(definition.code()).orElseGet(() -> {
                Permission permission = Permission.builder()
                    .code(definition.code())
                    .name(definition.name())
                    .module(definition.module())
                    .description(definition.description())
                    .isActive(true)
                    .build();
                return permissionRepository.save(permission);
            });
        }

        Role adminRole = roleRepository.findByName("Administrateur")
            .orElseGet(() -> roleRepository.save(Role.builder().name("Administrateur").description("Administrateur du système").isActive(true).build()));

        Role managerRole = roleRepository.findByName("Gestionnaire d'entrepôt")
            .orElseGet(() -> roleRepository.save(Role.builder().name("Gestionnaire d'entrepôt").description("Responsable opérationnel d’un entrepôt").isActive(true).build()));

        Role warehouseRole = roleRepository.findByName("Magasinier")
            .orElseGet(() -> roleRepository.save(Role.builder().name("Magasinier").description("Personnel de magasin").isActive(true).build()));

        Role auditorRole = roleRepository.findByName("Auditeur")
            .orElseGet(() -> roleRepository.save(Role.builder().name("Auditeur").description("Auditeur des opérations").isActive(true).build()));

        Set<Permission> allPermissions = permissionRepository.findAll().stream().collect(Collectors.toSet());
        adminRole.setPermissions(allPermissions);
        roleRepository.save(adminRole);

        Set<Permission> managerPermissions = allPermissions.stream()
            .filter(permission -> permission.getCode().startsWith("warehouse.")
                || permission.getCode().startsWith("zone.")
                || permission.getCode().startsWith("product.")
                || permission.getCode().startsWith("category.")
                || permission.getCode().startsWith("supplier.")
                || permission.getCode().startsWith("stock.")
                || permission.getCode().startsWith("receipt.")
                || permission.getCode().startsWith("dispatch.")
                || permission.getCode().startsWith("transfer.")
                || permission.getCode().startsWith("inventory.")
                || permission.getCode().startsWith("alert.")
                || permission.getCode().startsWith("dashboard.")
                || permission.getCode().startsWith("report.")
                || permission.getCode().startsWith("audit."))
            .collect(Collectors.toSet());
        managerRole.setPermissions(managerPermissions);
        roleRepository.save(managerRole);

        Set<Permission> warehousePermissions = allPermissions.stream()
            .filter(permission -> permission.getCode().startsWith("stock.")
                || permission.getCode().startsWith("receipt.")
                || permission.getCode().startsWith("dispatch.")
                || permission.getCode().startsWith("transfer.")
                || permission.getCode().startsWith("inventory.")
                || permission.getCode().startsWith("barcode."))
            .collect(Collectors.toSet());
        warehouseRole.setPermissions(warehousePermissions);
        roleRepository.save(warehouseRole);

        Set<Permission> auditorPermissions = allPermissions.stream()
            .filter(permission -> permission.getCode().startsWith("stock.")
                || permission.getCode().startsWith("dashboard.")
                || permission.getCode().startsWith("report.")
                || permission.getCode().startsWith("audit."))
            .collect(Collectors.toSet());
        auditorRole.setPermissions(auditorPermissions);
        roleRepository.save(auditorRole);
    }
}
