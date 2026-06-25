package com.backend.module.supplier.dto;

import com.backend.module.shared.enums.PurchaseOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DeliveryHistoryResponse {

    private Long orderId;
    private String orderNumber;
    private PurchaseOrderStatus status;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private Double totalAmount;
    private String createdByUsername;
    private List<DeliveryLineResponse> lines;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class DeliveryLineResponse {
        private Long productId;
        private String productName;
        private String productReference;
        private Integer quantity;
        private Integer receivedQty;
        private Double unitPrice;
    }
}
