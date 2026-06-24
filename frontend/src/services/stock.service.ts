import http from './http'
import type {
  StockResponse,
  CreateStockRequest,
  UpdateStockRequest,
  StockMovementResponse,
  MovementType,
} from '@/types/stock.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

const base = (warehouseId: number) => `/api/warehouses/${warehouseId}/stocks`

export const stockService = {
  list: (
    warehouseId: number,
    params?: {
      zoneId?: number
      categoryId?: number
      belowMin?: boolean
      search?: string
      page?: number
      size?: number
    },
  ): Promise<PaginatedResponse<StockResponse>> =>
    http.get(base(warehouseId), { params }).then((r) => r.data),

  create: (warehouseId: number, data: CreateStockRequest): Promise<StockResponse> =>
    http.post(base(warehouseId), data).then((r) => r.data),

  update: (
    warehouseId: number,
    stockId: number,
    data: UpdateStockRequest,
  ): Promise<StockResponse> =>
    http.put(`${base(warehouseId)}/${stockId}`, data).then((r) => r.data),

  listMovements: (
    warehouseId: number,
    params?: {
      movementType?: MovementType
      productId?: number
      zoneId?: number
      page?: number
      size?: number
    },
  ): Promise<PaginatedResponse<StockMovementResponse>> =>
    http.get(`${base(warehouseId)}/movements`, { params }).then((r) => r.data),
}
