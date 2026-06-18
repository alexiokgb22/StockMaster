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

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository     userRepository;
    private final RoleRepository     roleRepository;
    private final WarehouseRepository warehouseRepository;
    private final PasswordEncoder    passwordEncoder;

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
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getUser(id));
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

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .isActive(true)
                .mustChangePassword(true)
                .role(role)
                .assignedWarehouse(warehouse)
                .build();

        return toResponse(userRepository.save(user));
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

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .isActive(true)
                .mustChangePassword(true)
                .role(storekeeperRole)
                .assignedWarehouse(warehouse)
                .build();

        return toResponse(userRepository.save(user));
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
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
