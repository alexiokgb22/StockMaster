package com.backend.module.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.module.user.entity.UserWarehouseHistory;

@Repository
public interface UserWarehouseHistoryRepository extends JpaRepository<UserWarehouseHistory, Long> {

    /**
     * Trouve l'affectation actuelle d'un utilisateur (isCurrent = true).
     */
    @Query("SELECT h FROM UserWarehouseHistory h WHERE h.user.id = :userId AND h.isCurrent = true")
    Optional<UserWarehouseHistory> findCurrentByUserId(@Param("userId") Long userId);

    /**
     * Marque toutes les affectations d'un utilisateur comme non-courantes.
     */
    @Modifying
    @Query("UPDATE UserWarehouseHistory h SET h.isCurrent = false, h.unassignedAt = CURRENT_TIMESTAMP WHERE h.user.id = :userId AND h.isCurrent = true")
    void markAllAsNotCurrent(@Param("userId") Long userId);
}
