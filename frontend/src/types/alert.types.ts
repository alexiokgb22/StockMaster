export type AlertType =
  | 'STOCK_BELOW_MIN'
  | 'STOCK_OUT'
  | 'ZONE_NEAR_CAPACITY'
  | 'WAREHOUSE_NEAR_CAPACITY'

export type AlertSeverity = 'INFO' | 'WARNING' | 'CRITICAL'

export interface AlertResponse {
  id: number
  type: AlertType
  severity: AlertSeverity
  message: string
  stockId: number | null
  productId: number | null
  productName: string | null
  zoneId: number | null
  zoneName: string | null
  warehouseId: number
  warehouseName: string
  isRead: boolean
  isResolved: boolean
  createdAt: string
  updatedAt: string
}
