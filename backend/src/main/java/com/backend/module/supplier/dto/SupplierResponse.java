package com.backend.module.supplier.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupplierResponse {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String phone;
    private String email;
    private String contactName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
