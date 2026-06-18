package com.backend.module.user.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Boolean isActive;
    private Boolean mustChangePassword;
    private String roleName;
    private Long roleId;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
