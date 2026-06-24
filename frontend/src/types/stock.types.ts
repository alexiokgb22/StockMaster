export interface StockResponse {
  id: number
  quantityAvailable: number
  quantityReserved: number
  quantityInTransit: number
  minStock: number | null
  maxStock: number | null
  isBelowMin: boolean
  productId: number
  productName: string
  productReference: string
  productBarcode: string
  productVolume: number | null
  categoryId: number
  categoryName: string
  warehouseId: number
  warehouseName: string
  zoneId: number
  zoneName: string
  createdAt: string
  updatedAt: string
}

export interface CreateStockRequest {
  productId: number
  zoneId: number
  initialQuantity: number
  minStock?: number
  maxStock?: number
  note?: string
}

export interface UpdateStockRequest {
  quantityAvailable?: number
  minStock?: number
  maxStock?: number
  note?: string
}

export type MovementType = 'ENTRY' | 'EXIT' | 'TRANSFER' | 'ADJUSTMENT'

export interface StockMovementResponse {
  id: number
  quantity: number
  movementType: MovementType
  referenceDoc: string | null
  note: string | null
  productId: number
  productName: string
  warehouseId: number
  warehouseName: string
  zoneId: number
  zoneName: string
  createdByUsername: string
  createdAt: string
}
