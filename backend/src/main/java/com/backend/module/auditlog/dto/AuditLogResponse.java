package com.backend.module.auditlog.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {

    private Long id;
    private String module;
    private String action;
    private String entityName;
    private Long entityId;
    private String description;
    private String username;
    private String userRole;
    private Long warehouseId;
    private String warehouseName;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private LocalDateTime createdAt;
}
