package com.backend.module.user.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.module.role.entity.Role;
import com.backend.module.role.repository.RoleRepository;
import com.backend.module.user.dto.*;
import com.backend.module.user.entity.User;
import com.backend.module.user.repository.UserRepository;
import com.backend.module.warehouse.entity.Warehouse;
import com.backend.module.warehouse.repository.WarehouseRepository;
import com.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository     userRepository;
    private final RoleRepository     roleRepository;
    private final WarehouseRepository warehouseRepository;
    private final PasswordEncoder    passwordEncoder;
    private final UserWarehouseHistoryService historyService;

    // ─────────────────────────────────────────────────────────────
    // LECTURE
    // ─────────────────────────────────────────────────────────────

    /**
     * Admin : liste paginée de tous les utilisateurs avec filtres.
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(String search, Long roleId, Boolean active, Pageable pageable) {
        return userRepository.findAllWithFilters(search, roleId, active, pageable)
                .map(this::toResponse);
    }

    /**
     * Gestionnaire : liste paginée des magasiniers de son entrepôt.
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> findStorekeepersByWarehouse(Long warehouseId, String search, Boolean active, Pageable pageable) {
        return userRepository.findStorekeepersByWarehouse(warehouseId, search, active, pageable)
                .map(u -> {
                    UserResponse response = toResponse(u);
                    // Calculer si le magasinier est actuellement dans cet entrepôt
                    response.setIsCurrentlyInWarehouse(
                        u.getAssignedWarehouse() != null && 
                        u.getAssignedWarehouse().getId().equals(warehouseId)
                    );
                    return response;
                });
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getUser(id));
    }

    /**
     * Gestionnaires disponibles pour l'assignation à un entrepôt.
     * Retourne les gestionnaires sans entrepôt + le gestionnaire actuel de l'entrepôt cible.
     * warehouseId null → création d'un entrepôt, retourne tous les gestionnaires libres.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> findAvailableManagers(Long warehouseId) {
        return userRepository.findAvailableManagers(warehouseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Arbre hiérarchique admin : tous les entrepôts avec leur gestionnaire et magasiniers.
     * Retourne un nœud par entrepôt, incluant les entrepôts sans gestionnaire.
     */
    @Transactional(readOnly = true)
    public List<WarehouseTreeNode> buildTree() {
        // 1. Charger tous les entrepôts
        List<Warehouse> warehouses = warehouseRepository.findAll(
            org.springframework.data.domain.Sort.by("name")
        );

        if (warehouses.isEmpty()) return List.of();

        List<Long> warehouseIds = warehouses.stream()
                .map(Warehouse::getId)
                .toList();

        // 2. Charger tous les magasiniers de ces entrepôts en une seule requête
        List<User> allStorekeepers = userRepository.findStorekeepersByWarehouseIds(warehouseIds);

        // Grouper les magasiniers par warehouseId
        Map<Long, List<User>> storekeepersByWarehouse = allStorekeepers.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        u -> u.getAssignedWarehouse().getId()
                ));

        // 3. Construire les nœuds
        return warehouses.stream()
                .map(w -> {
                    List<WarehouseTreeNode.UserSummary> storekeepers = storekeepersByWarehouse
                            .getOrDefault(w.getId(), List.of())
                            .stream()
                            .map(this::toSummary)
                            .toList();

                    WarehouseTreeNode.UserSummary manager = w.getManager() != null
                            ? toSummary(w.getManager())
                            : null;

                    return WarehouseTreeNode.builder()
                            .warehouseId(w.getId())
                            .warehouseName(w.getName())
                            .warehouseCity(w.getCity())
                            .warehouseActive(w.getIsActive())
                            .manager(manager)
                            .storekeepers(storekeepers)
                            .build();
                })
                .toList();
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — ADMIN
    // L'admin choisit librement le rôle (tout sauf Administrateur).
    // ─────────────────────────────────────────────────────────────

    public UserResponse createUser(CreateUserRequest req) {
        validateUsernameAndEmail(req.getUsername(), req.getEmail(), null);

        Role role = roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé : " + req.getRoleId()));

        if ("Administrateur".equals(role.getName())) {
            throw new BusinessException("La création d'un compte Administrateur n'est pas autorisée via cette API");
        }

        Warehouse warehouse = resolveWarehouse(role, req.getWarehouseId());

        // Vérification AVANT la création : si Gestionnaire d'entrepôt, l'entrepôt
        // cible ne doit pas avoir de manager.
        if (warehouse != null && "Gestionnaire d'entrepôt".equals(role.getName())) {
            if (warehouse.getManager() != null) {
                throw new BusinessException(
                    "L'entrepôt \"" + warehouse.getName() + "\" a déjà un gestionnaire assigné"
                );
            }
        }

        // Récupérer l'utilisateur connecté (créateur)
        CustomUserDetails creator = currentUser();
        User createdBy = creator != null ? getUser(creator.getId()) : null;

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .isActive(true)
                .mustChangePassword(true)
                .role(role)
                .assignedWarehouse(warehouse)
                .createdBy(createdBy)
                .build();

        User saved = userRepository.save(user);

        // Synchronisation bidirectionnelle : mettre à jour warehouse.manager
        if (warehouse != null && "Gestionnaire d'entrepôt".equals(role.getName())) {
            warehouse.setManager(saved);
            warehouseRepository.save(warehouse);
        }

        // Enregistrer l'historique si l'utilisateur est affecté à un entrepôt
        if (warehouse != null && "Magasinier".equals(role.getName())) {
            historyService.recordAssignment(saved, warehouse);
        }

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    // CRÉATION — GESTIONNAIRE
    // Rôle figé à "Magasinier", entrepôt = entrepôt du gestionnaire.
    // ─────────────────────────────────────────────────────────────

    public UserResponse createStorekeeper(CreateUserRequest req) {
        validateUsernameAndEmail(req.getUsername(), req.getEmail(), null);

        CustomUserDetails manager = currentUser();
        Long warehouseId = manager.getWarehouseId();

        if (warehouseId == null) {
            throw new BusinessException("Votre compte n'est pas affecté à un entrepôt");
        }

        Role storekeeperRole = roleRepository.findByName("Magasinier")
                .orElseThrow(() -> new ResourceNotFoundException("Rôle Magasinier introuvable"));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + warehouseId));

        // Récupérer le gestionnaire comme créateur
        User createdBy = getUser(manager.getId());

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .isActive(true)
                .mustChangePassword(true)
                .role(storekeeperRole)
                .assignedWarehouse(warehouse)
                .createdBy(createdBy)
                .build();

        User saved = userRepository.save(user);

        // Enregistrer dans l'historique
        historyService.recordAssignment(saved, warehouse);

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    // MODIFICATION
    // ─────────────────────────────────────────────────────────────

    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = getUser(id);

        if (req.getUsername() != null) {
            validateUsernameAndEmail(req.getUsername(), null, id);
            user.setUsername(req.getUsername());
        }
        if (req.getEmail() != null) {
            validateUsernameAndEmail(null, req.getEmail(), id);
            user.setEmail(req.getEmail());
        }
        if (req.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(req.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + req.getWarehouseId()));
            user.setAssignedWarehouse(warehouse);
        }

        return toResponse(userRepository.save(user));
    }

    // ─────────────────────────────────────────────────────────────
    // ACTIVATION / DÉSACTIVATION
    // ─────────────────────────────────────────────────────────────

    /**
     * Admin : toggle n'importe quel compte (sauf le sien).
     */
    public UserResponse toggleUser(Long id) {
        CustomUserDetails caller = currentUser();
        if (caller.getId().equals(id)) {
            throw new BusinessException("Vous ne pouvez pas désactiver votre propre compte");
        }
        User user = getUser(id);
        user.setIsActive(!user.getIsActive());
        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        CustomUserDetails caller = currentUser();
        if (caller.getId().equals(id)) {
            throw new BusinessException("Vous ne pouvez pas supprimer votre propre compte");
        }

        User user = getUser(id);

        // Désaffecter l'utilisateur de l'entrepôt qu'il gère (si gestionnaire)
        // avant la suppression pour éviter la contrainte FK warehouses.manager_id → users.id
        warehouseRepository.findByManagerId(id).forEach(warehouse -> {
            warehouse.setManager(null);
            warehouseRepository.save(warehouse);
        });

        userRepository.delete(user);
    }

    /**
     * Gestionnaire : toggle uniquement les magasiniers de son entrepôt.
     */
    public UserResponse toggleStorekeeper(Long id) {
        CustomUserDetails manager = currentUser();
        User user = getUser(id);

        if (!"Magasinier".equals(user.getRole().getName())) {
            throw new BusinessException("Vous ne pouvez désactiver que les magasiniers");
        }
        if (user.getAssignedWarehouse() == null
                || !user.getAssignedWarehouse().getId().equals(manager.getWarehouseId())) {
            throw new BusinessException("Ce magasinier n'appartient pas à votre entrepôt");
        }

        user.setIsActive(!user.getIsActive());
        return toResponse(userRepository.save(user));
    }

    // ─────────────────────────────────────────────────────────────
    // RÉINITIALISATION DU MOT DE PASSE
    // ─────────────────────────────────────────────────────────────

    public void resetPassword(Long id, ResetPasswordRequest req) {
        User user = getUser(id);
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    // ─────────────────────────────────────────────────────────────
    // ASSIGNATION / DÉSAFFECTATION D'ENTREPÔT
    // Opération transactionnelle :
    //   1. Désaffecte l'ancien entrepôt du manager (si existant)
    //   2. Affecte le nouvel entrepôt (si warehouseId non null)
    //      → vérifie que la cible n'a pas déjà un autre gestionnaire
    // ─────────────────────────────────────────────────────────────

    public UserResponse assignWarehouse(Long userId, Long warehouseId) {
        User user = getUser(userId);
        String userRole = user.getRole().getName();

        // 1. Désaffecter l'ancien entrepôt géré par cet utilisateur (si gestionnaire)
        if ("Gestionnaire d'entrepôt".equals(userRole)) {
            warehouseRepository.findByManagerId(userId).forEach(old -> {
                old.setManager(null);
                warehouseRepository.save(old);
            });
        }

        if (warehouseId == null) {
            // Désaffectation pure
            if ("Magasinier".equals(userRole)) {
                historyService.recordUnassignment(userId);
            }
            user.setAssignedWarehouse(null);
        } else {
            Warehouse target = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + warehouseId));

            // Vérification UNIQUEMENT pour les Gestionnaires d'entrepôt :
            // l'entrepôt cible ne doit pas avoir de gestionnaire autre que l'utilisateur courant.
            if ("Gestionnaire d'entrepôt".equals(userRole)) {
                if (warehouseRepository.countOtherManager(warehouseId, userId) > 0) {
                    throw new BusinessException(
                        "L'entrepôt \"" + target.getName() + "\" a déjà un gestionnaire assigné"
                    );
                }
                // 2a. Affecter le gestionnaire à l'entrepôt (relation bidirectionnelle)
                target.setManager(user);
                warehouseRepository.save(target);
            }

            // 2b. Affecter l'entrepôt à l'utilisateur
            user.setAssignedWarehouse(target);

            // 3. Enregistrer dans l'historique si c'est un magasinier
            if ("Magasinier".equals(userRole)) {
                historyService.recordAssignment(user, target);
            }
        }

        return toResponse(userRepository.save(user));
    }

    // ─────────────────────────────────────────────────────────────
    // ATTRIBUTION DE RÔLE — ADMIN ONLY
    // ─────────────────────────────────────────────────────────────

    public UserResponse assignRole(Long id, AssignRoleRequest req) {
        User user = getUser(id);

        Role role = roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé : " + req.getRoleId()));

        if ("Administrateur".equals(role.getName())) {
            throw new BusinessException("L'attribution du rôle Administrateur n'est pas autorisée");
        }

        // Si le nouveau rôle n'est pas Magasinier, effacer l'entrepôt associé
        if (!"Magasinier".equals(role.getName())) {
            user.setAssignedWarehouse(null);
        }

        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────────

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + id));
    }

    private CustomUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserDetails) auth.getPrincipal();
    }

    private void validateUsernameAndEmail(String username, String email, Long excludeId) {
        if (username != null) {
            boolean exists = excludeId == null
                    ? userRepository.existsByUsername(username)
                    : userRepository.existsByUsernameAndIdNot(username, excludeId);
            if (exists) throw new BusinessException("Ce nom d'utilisateur est déjà utilisé : " + username);
        }
        if (email != null) {
            boolean exists = excludeId == null
                    ? userRepository.existsByEmail(email)
                    : userRepository.existsByEmailAndIdNot(email, excludeId);
            if (exists) throw new BusinessException("Cet email est déjà utilisé : " + email);
        }
    }

    /**
     * Résout l'entrepôt en fonction du rôle.
     * Facultatif même pour Magasinier (peut être assigné plus tard).
     */
    private Warehouse resolveWarehouse(Role role, Long warehouseId) {
        if (warehouseId == null) return null;
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + warehouseId));
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .isActive(u.getIsActive())
                .mustChangePassword(Boolean.TRUE.equals(u.getMustChangePassword()))
                .roleName(u.getRole().getName())
                .roleId(u.getRole().getId())
                .warehouseId(u.getAssignedWarehouse() != null ? u.getAssignedWarehouse().getId() : null)
                .warehouseName(u.getAssignedWarehouse() != null ? u.getAssignedWarehouse().getName() : null)
                .createdById(u.getCreatedBy() != null ? u.getCreatedBy().getId() : null)
                .createdByUsername(u.getCreatedBy() != null ? u.getCreatedBy().getUsername() : null)
                .createdByRole(u.getCreatedBy() != null ? u.getCreatedBy().getRole().getName() : null)
                .isCurrentlyInWarehouse(null) // Sera calculé au niveau du contrôleur si nécessaire
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    private WarehouseTreeNode.UserSummary toSummary(User u) {
        return WarehouseTreeNode.UserSummary.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .isActive(u.getIsActive())
                .build();
    }
}
