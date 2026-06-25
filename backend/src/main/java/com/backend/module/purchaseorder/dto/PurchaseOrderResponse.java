package com.backend.module.purchaseorder.dto;

import com.backend.module.shared.enums.PurchaseOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PurchaseOrderResponse {

    private Long id;
    private String orderNumber;
    private PurchaseOrderStatus status;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private String note;
    private Double totalAmount;

    // Entrepôt
    private Long warehouseId;
    private String warehouseName;

    // Fournisseur (null tant que non validé par l'admin)
    private Long supplierId;
    private String supplierName;
    private String supplierContactName;
    private String supplierPhone;
    private String supplierEmail;

    // Créateur
    private String createdByUsername;

    private List<OrderLineResponse> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class OrderLineResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productReference;
        private String categoryName;
        private Integer quantity;
        private Integer receivedQty;
        private Double unitPrice;
    }
}
