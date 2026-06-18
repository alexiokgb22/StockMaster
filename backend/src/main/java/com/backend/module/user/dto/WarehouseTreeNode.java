package com.backend.module.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Nœud de l'arbre hiérarchique retourné par GET /api/users/tree.
 * Représente un entrepôt avec son gestionnaire et ses magasiniers.
 */
@Data
@Builder
public class WarehouseTreeNode {

    private Long   warehouseId;
    private String warehouseName;
    private String warehouseCity;
    private Boolean warehouseActive;

    /** Gestionnaire de l'entrepôt — null si non encore assigné. */
    private UserSummary manager;

    /** Magasiniers affectés à cet entrepôt. */
    private List<UserSummary> storekeepers;

    @Data
    @Builder
    public static class UserSummary {
        private Long    id;
        private String  username;
        private String  email;
        private Boolean isActive;
    }
}
