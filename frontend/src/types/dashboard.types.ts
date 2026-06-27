export interface GlobalDashboardResponse {
  activeWarehouses: number
  totalWarehouses: number
  activeProducts: number
  activeSuppliers: number
  activeUsers: number
  globalBelowMinCount: number
  globalStockOutCount: number
  totalUnreadAlerts: number
  totalCriticalAlerts: number
  pendingPurchaseOrders: number
  pendingReceptions: number
  pendingDispatches: number
  pendingTransfers: number
}

export interface WarehouseDashboardResponse {
  warehouseId: number
  warehouseName: string
  totalStockLines: number
  stockOutCount: number
  belowMinCount: number
  totalCapacity: number | null
  usedCapacity: number | null
  capacityPercent: number | null
  pendingPurchaseOrders: number
  pendingReceptions: number
  pendingDispatches: number
  incomingTransfers: number
  activeAlerts: number
  criticalAlerts: number
}

export interface MovementChartPoint {
  label: string
  entries: number
  exits: number
  transfers: number
}

export interface CategoryStockPoint {
  categoryId: number
  categoryName: string
  stockLineCount: number
  totalQuantity: number
}

export interface TopProductPoint {
  productId: number
  productName: string
  productReference: string
  categoryName: string
  movementCount: number
  totalQuantity: number
}
