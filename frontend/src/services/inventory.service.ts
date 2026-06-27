import http from './http'
import type {
  InventoryResponse,
  InventoryStatus,
  CreateInventoryRequest,
  UpdateInventoryLineRequest,
} from '@/types/inventory.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

const base = (warehouseId: number) => `/api/warehouses/${warehouseId}/inventories`

export const inventoryService = {
  list: (
    warehouseId: number,
    params?: { status?: InventoryStatus; page?: number; size?: number },
  ): Promise<PaginatedResponse<InventoryResponse>> =>
    http.get(base(warehouseId), { params }).then((r) => r.data),

  getById: (warehouseId: number, inventoryId: number): Promise<InventoryResponse> =>
    http.get(`${base(warehouseId)}/${inventoryId}`).then((r) => r.data),

  create: (warehouseId: number, data: CreateInventoryRequest): Promise<InventoryResponse> =>
    http.post(base(warehouseId), data).then((r) => r.data),

  // Saisie d'une ligne (quantité physique)
  updateLine: (
    warehouseId: number,
    inventoryId: number,
    lineId: number,
    data: UpdateInventoryLineRequest,
  ): Promise<InventoryResponse> =>
    http
      .patch(`${base(warehouseId)}/${inventoryId}/lines/${lineId}`, data)
      .then((r) => r.data),

  // Clôture — irréversible
  complete: (warehouseId: number, inventoryId: number): Promise<InventoryResponse> =>
    http.patch(`${base(warehouseId)}/${inventoryId}/complete`).then((r) => r.data),

  // Annulation
  cancel: (warehouseId: number, inventoryId: number): Promise<InventoryResponse> =>
    http.patch(`${base(warehouseId)}/${inventoryId}/cancel`).then((r) => r.data),
}
