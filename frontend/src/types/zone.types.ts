export interface ZoneResponse {
  id: number
  name: string
  sequenceNumber: number
  capacity: number | null
  zoneType: string | null
  warehouseId: number
  warehouseName: string
  categoryId: number | null
  categoryName: string | null
  createdByUsername: string
  isAdminDefined: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateZoneRequest {
  zoneType: string
  capacity?: number
  categoryId?: number
}

export interface UpdateZoneRequest {
  zoneType?: string
  capacity?: number
}

export interface AssignCategoryRequest {
  categoryId: number
}
