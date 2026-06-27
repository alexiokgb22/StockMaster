package com.backend.module.auditlog.entity;

import com.backend.module.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Journal immuable de chaque action critique effectuée dans le système.
 * Chaque ligne représente : QUI a fait QUOI sur QUELLE entité, QUAND.
 * Les lignes ne sont jamais modifiées ni supprimées.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_module",    columnList = "module"),
    @Index(name = "idx_audit_action",    columnList = "action"),
    @Index(name = "idx_audit_username",  columnList = "username"),
    @Index(name = "idx_audit_entity",    columnList = "entity_name, entity_id"),
    @Index(name = "idx_audit_createdat", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Module métier concerné (ex: "reception", "stock", "transfer"). */
    @Column(name = "module", nullable = false, length = 60)
    private String module;

    /** Action réalisée (ex: "CREATE", "VALIDATE", "REJECT", "DELETE"). */
    @Column(name = "action", nullable = false, length = 60)
    private String action;

    /** Nom de l'entité cible (ex: "Reception", "Stock", "Transfer"). */
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    /** Identifiant de l'entité cible (peut être null si création échouée). */
    @Column(name = "entity_id")
    private Long entityId;

    /** Description lisible de l'action (ex: "Réception REC-20240101-4521 validée"). */
    @Column(name = "description", length = 500)
    private String description;

    /** Nom d'utilisateur de l'acteur (conservé même si l'utilisateur est supprimé). */
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    /** Rôle de l'utilisateur au moment de l'action. */
    @Column(name = "user_role", length = 60)
    private String userRole;

    /** Identifiant de l'entrepôt concerné (null si action globale). */
    @Column(name = "warehouse_id")
    private Long warehouseId;

    /** Nom de l'entrepôt concerné (conservé même si l'entrepôt change). */
    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    /** Données avant modification (format JSON, null si création). */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /** Données après modification (format JSON, null si suppression). */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /** Adresse IP du client (null si non disponible). */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
}
