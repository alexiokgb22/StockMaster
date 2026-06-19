import http from './http'
import type { ZoneResponse, CreateZoneRequest, UpdateZoneRequest, AssignCategoryRequest } from '@/types/zone.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export const zoneService = {
  list: (
    warehouseId: number,
    params?: { page?: number; size?: number },
  ): Promise<PaginatedResponse<ZoneResponse>> =>
    http.get(`/api/warehouses/${+warehouseId}/zones`, { params }).then((r) => r.data),

  create: (warehouseId: number, data: CreateZoneRequest): Promise<ZoneResponse> =>
    http.post(`/api/warehouses/${+warehouseId}/zones`, data).then((r) => r.data),

  update: (warehouseId: number, zoneId: number, data: UpdateZoneRequest): Promise<ZoneResponse> =>
    http.put(`/api/warehouses/${+warehouseId}/zones/${+zoneId}`, data).then((r) => r.data),

  assignCategory: (warehouseId: number, zoneId: number, data: AssignCategoryRequest): Promise<ZoneResponse> =>
    http
      .patch(`/api/warehouses/${+warehouseId}/zones/${+zoneId}/assign-category`, data)
      .then((r) => r.data),

  /**
   * IDs des catégories déjà couvertes par une zone Admin dans cet entrepôt.
   * Permet au gestionnaire de voir uniquement les catégories qu'il peut encore couvrir.
   */
  coveredCategoryIds: (warehouseId: number): Promise<number[]> =>
    http.get(`/api/warehouses/${+warehouseId}/zones/covered-categories`).then((r) => r.data),
}
