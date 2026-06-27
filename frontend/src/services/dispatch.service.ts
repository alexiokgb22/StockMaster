import http from './http'
import type {
  DispatchResponse,
  DispatchStatus,
  CreateDispatchRequest,
  RejectDispatchRequest,
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
  list: (
    warehouseId: number,
    params?: { status?: DispatchStatus; page?: number; size?: number },
  ): Promise<PaginatedResponse<DispatchResponse>> =>
    http.get(base(warehouseId), { params }).then((r) => r.data),

  getById: (warehouseId: number, dispatchId: number): Promise<DispatchResponse> =>
    http.get(`${base(warehouseId)}/${dispatchId}`).then((r) => r.data),

  countPending: (warehouseId: number): Promise<number> =>
    http.get(`${base(warehouseId)}/pending-count`).then((r) => r.data),

  create: (warehouseId: number, data: CreateDispatchRequest): Promise<DispatchResponse> =>
    http.post(base(warehouseId), data).then((r) => r.data),

  validate: (warehouseId: number, dispatchId: number): Promise<DispatchResponse> =>
    http.patch(`${base(warehouseId)}/${dispatchId}/validate`).then((r) => r.data),

  reject: (
    warehouseId: number,
    dispatchId: number,
    data?: RejectDispatchRequest,
  ): Promise<DispatchResponse> =>
    http.patch(`${base(warehouseId)}/${dispatchId}/reject`, data ?? {}).then((r) => r.data),

  getBordereauUrl: (warehouseId: number, dispatchId: number): string =>
    `${apiBaseUrl}${base(warehouseId)}/${dispatchId}/bordereau`,
}
