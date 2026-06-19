export interface CategoryResponse {
  id: number
  name: string
  description: string | null
  productCount: number
  warehouseId: number | null      // null = non affectée
  warehouseName: string | null    // null = non affectée
  createdByUsername: string
  isAdminDefined: boolean
  isAssigned: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateCategoryRequest {
  name: string
  description?: string
}

export interface UpdateCategoryRequest {
  name?: string
  description?: string
}

export interface AssignWarehouseRequest {
  warehouseId: number
}
