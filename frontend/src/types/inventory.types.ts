export type InventoryStatus = 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type InventoryType   = 'FULL' | 'PARTIAL'

export interface InventoryLineResponse {
  id: number
  stockId: number
  productId: number
  productName: string
  productReference: string
  categoryName: string
  zoneId: number
  zoneName: string
  theoreticalQty: number
  actualQty: number | null   // null tant que non saisie
  gap: number | null         // null tant que non saisie
  counted: boolean
  note: string | null
}

export interface InventoryResponse {
  id: number
  inventoryNumber: string
  inventoryType: InventoryType
  inventoryStatus: InventoryStatus
  note: string | null
  startedAt: string
  completedAt: string | null
  warehouseId: number
  warehouseName: string
  createdByUsername: string
  lines: InventoryLineResponse[]
  totalLines: number
  countedLines: number
  gapLines: number
  totalGap: number
  createdAt: string
  updatedAt: string
}

export interface CreateInventoryRequest {
  inventoryType: InventoryType
  note?: string
  zoneIds?: number[]   // uniquement pour PARTIAL
}

export interface UpdateInventoryLineRequest {
  actualQty: number
  note?: string
}
