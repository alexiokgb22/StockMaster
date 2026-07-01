import http from './http'
import type {
  DispatchResponse,
  DispatchStatus,
  CreateDispatchRequest,
} from '@/types/dispatch.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? ''
const base = (warehouseId: number) => `/api/warehouses/${warehouseId}/dispatches`

export const dispatchService = {
  // Historique des bons de sortie d'un entrepôt
  list: (
    warehouseId: number,
    params?: { status?: DispatchStatus; page?: number; size?: number },
  ): Promise<PaginatedResponse<DispatchResponse>> =>
    http.get(base(warehouseId), { params }).then((r) => r.data),

  getById: (warehouseId: number, dispatchId: number): Promise<DispatchResponse> =>
    http.get(`${base(warehouseId)}/${dispatchId}`).then((r) => r.data),

  // Création + validation immédiate par le magasinier
  create: (warehouseId: number, data: CreateDispatchRequest): Promise<DispatchResponse> =>
    http.post(base(warehouseId), data).then((r) => r.data),

  getBordereauUrl: (warehouseId: number, dispatchId: number): string =>
    `${apiBaseUrl}${base(warehouseId)}/${dispatchId}/bordereau`,
}
