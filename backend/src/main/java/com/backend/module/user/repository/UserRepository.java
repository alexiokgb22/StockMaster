package com.backend.module.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.module.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT u FROM User u JOIN FETCH u.role r JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsernameWithRoleAndPermissions(@Param("username") String username);

    @Query("SELECT u FROM User u JOIN FETCH u.role r JOIN FETCH r.permissions WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role LEFT JOIN FETCH u.assignedWarehouse WHERE u.assignedWarehouse.id = :warehouseId")
    List<User> findByAssignedWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("""
        SELECT u FROM User u
        JOIN FETCH u.role r
        LEFT JOIN FETCH u.assignedWarehouse w
        WHERE (:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:roleId IS NULL OR r.id = :roleId)
          AND (:active IS NULL OR u.isActive = :active)
        """)
    Page<User> findAllWithFilters(
        @Param("search") String search,
        @Param("roleId") Long roleId,
        @Param("active") Boolean active,
        Pageable pageable
    );

    @Query("""
        SELECT u FROM User u
        JOIN FETCH u.role r
        LEFT JOIN FETCH u.assignedWarehouse w
        WHERE u.assignedWarehouse.id = :warehouseId
          AND (:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:active IS NULL OR u.isActive = :active)
        """)
    Page<User> findStorekeepersByWarehouse(
        @Param("warehouseId") Long warehouseId,
        @Param("search") String search,
        @Param("active") Boolean active,
        Pageable pageable
    );
}
