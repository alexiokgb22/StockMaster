package com.backend.module.shared.enums;

public enum AlertType {
    STOCK_BELOW_MIN,        // quantityAvailable < minStock
    STOCK_OUT,              // quantityAvailable == 0
    ZONE_NEAR_CAPACITY,     // usedCapacity > 80% de zone.capacity
    WAREHOUSE_NEAR_CAPACITY // usedCapacity > 85% de warehouse.capacity
}
