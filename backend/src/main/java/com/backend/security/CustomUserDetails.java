package com.backend.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.backend.module.user.entity.User;

import lombok.Getter;

/**
 * Implémentation personnalisée de UserDetails pour Spring Security.
 * Encapsule les informations de l'utilisateur nécessaires à l'authentification.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final String roleName;
    private final Long roleId;
    private final boolean isActive;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Long warehouseId;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.roleName = user.getRole().getName();
        this.roleId = user.getRole().getId();
        this.isActive = user.getIsActive();
        this.warehouseId = user.getAssignedWarehouse() != null ? user.getAssignedWarehouse().getId() : null;
        
        // Convertir les permissions en authorities Spring Security
        this.authorities = user.getRole().getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
