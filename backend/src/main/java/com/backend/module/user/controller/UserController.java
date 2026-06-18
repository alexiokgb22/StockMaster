package com.backend.module.user.controller;

import com.backend.module.user.dto.*;
import com.backend.module.user.service.UserService;
import com.backend.module.user.dto.AssignWarehouseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ──────────────────────────────────────────────────────────────
    // LECTURE
    // ──────────────────────────────────────────────────────────────

    /**
     * Admin : liste paginée de tous les utilisateurs.
     * Gestionnaire : utiliser /storekeeper-list à la place.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<Page<UserResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userService.findAll(search, roleId, active, pageable));
    }

    /**
     * Gestionnaire : liste paginée de ses magasiniers.
     */
    @GetMapping("/storekeeper-list")
    @PreAuthorize("hasAuthority('user.create_storekeeper')")
    public ResponseEntity<Page<UserResponse>> getStorekeepers(
            @RequestParam Long warehouseId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userService.findStorekeepersByWarehouse(warehouseId, search, active, pageable));
    }

    /**
     * Gestionnaires disponibles pour l'assignation à un entrepôt.
     * Retourne les gestionnaires sans entrepôt + le gestionnaire actuel de l'entrepôt (si warehouseId fourni).
     * Utilisé par le formulaire d'édition/création d'un entrepôt.
     */
    @GetMapping("/available-managers")
    @PreAuthorize("hasAuthority('warehouse.update')")
    public ResponseEntity<List<UserResponse>> getAvailableManagers(
            @RequestParam(required = false) Long warehouseId
    ) {
        return ResponseEntity.ok(userService.findAvailableManagers(warehouseId));
    }

    /**
     * Arbre hiérarchique : tous les entrepôts avec gestionnaire + magasiniers.
     * Admin uniquement.
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<List<WarehouseTreeNode>> getTree() {
        return ResponseEntity.ok(userService.buildTree());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.read')")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ──────────────────────────────────────────────────────────────
    // CRÉATION
    // ──────────────────────────────────────────────────────────────

    /**
     * Admin crée un utilisateur avec le rôle de son choix.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user.create')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(req));
    }

    /**
     * Gestionnaire crée un magasinier pour son entrepôt.
     */
    @PostMapping("/storekeeper")
    @PreAuthorize("hasAuthority('user.create_storekeeper')")
    public ResponseEntity<UserResponse> createStorekeeper(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createStorekeeper(req));
    }

    // ──────────────────────────────────────────────────────────────
    // MODIFICATION
    // ──────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest req
    ) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    // ──────────────────────────────────────────────────────────────
    // ACTIVATION / DÉSACTIVATION
    // ──────────────────────────────────────────────────────────────

    /**
     * Admin : toggle n'importe quel utilisateur.
     */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('user.toggle')")
    public ResponseEntity<UserResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUser(id));
    }

    /**
     * Gestionnaire : toggle un magasinier de son entrepôt.
     */
    @PatchMapping("/{id}/toggle-storekeeper")
    @PreAuthorize("hasAuthority('user.toggle_storekeeper')")
    public ResponseEntity<UserResponse> toggleStorekeeper(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleStorekeeper(id));
    }

    // ──────────────────────────────────────────────────────────────
    // MOT DE PASSE
    // ──────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user.reset_password')")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest req
    ) {
        userService.resetPassword(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user.delete')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────────────────────
    // ATTRIBUTION DE RÔLE — ADMIN ONLY
    // ──────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('user.assign_role')")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleRequest req
    ) {
        return ResponseEntity.ok(userService.assignRole(id, req));
    }

    // ──────────────────────────────────────────────────────────────
    // ASSIGNATION / DÉSAFFECTATION D'ENTREPÔT — ADMIN ONLY
    // ──────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/warehouse")
    @PreAuthorize("hasAuthority('user.assign_role')")
    public ResponseEntity<UserResponse> assignWarehouse(
            @PathVariable Long id,
            @RequestBody AssignWarehouseRequest req
    ) {
        return ResponseEntity.ok(userService.assignWarehouse(id, req.getWarehouseId()));
    }
}
