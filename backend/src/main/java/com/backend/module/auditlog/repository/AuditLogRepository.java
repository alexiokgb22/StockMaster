package com.backend.module.auditlog.repository;

import com.backend.module.auditlog.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Recherche paginée avec filtres combinés (tous optionnels).
     * Utilisée par AuditLogController pour la liste principale.
     */
    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:module      IS NULL OR a.module      = :module)
          AND (:action      IS NULL OR a.action      = :action)
          AND (:username    IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%')))
          AND (:entityName  IS NULL OR a.entityName  = :entityName)
          AND (:warehouseId IS NULL OR a.warehouseId = :warehouseId)
          AND (:from        IS NULL OR a.createdAt  >= :from)
          AND (:to          IS NULL OR a.createdAt  <= :to)
        ORDER BY a.createdAt DESC
        """)
    Page<AuditLog> search(
        @Param("module")      String module,
        @Param("action")      String action,
        @Param("username")    String username,
        @Param("entityName")  String entityName,
        @Param("warehouseId") Long warehouseId,
        @Param("from")        LocalDateTime from,
        @Param("to")          LocalDateTime to,
        Pageable pageable
    );

    /** Tous les logs d'une entité précise (ex: historique d'une réception). */
    Page<AuditLog> findByEntityNameAndEntityIdOrderByCreatedAtDesc(
        String entityName, Long entityId, Pageable pageable);

    /** Tous les logs d'un utilisateur. */
    Page<AuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
}
