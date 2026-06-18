import http from './http'
import type { WarehouseResponse, CreateWarehouseRequest, UpdateWarehouseRequest } from '@/types/warehouse.types'

export interface WarehouseListParams {
  search?: string
  active?: boolean
  unassigned?: boolean
  page?: number
  size?: number
}

export interface WarehouseUnassignedParams {
  /** Si fourni, inclut l'entrepôt actuellement géré par cet utilisateur dans les résultats. */
  managerId?: number
  page?: number
  size?: number
}

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  currentPage: number
}

const prefix = '/api/warehouses'

export const warehouseService = {
  list: (params?: WarehouseListParams): Promise<PaginatedResponse<WarehouseResponse>> =>
    http.get(prefix, { params }).then((response) => response.data),

  /**
   * Entrepôts disponibles pour l'assignation d'un gestionnaire.
   * - Sans managerId : tous les entrepôts sans manager.
   * - Avec managerId : sans manager + entrepôt actuel du manager
   *   (pour que le select affiche la valeur courante).
   */
  listUnassigned: (params?: WarehouseUnassignedParams): Promise<PaginatedResponse<WarehouseResponse>> =>
    http
      .get(`${prefix}/unassigned`, { params: { size: 100, ...params } })
      .then((response) => response.data),

  get: (id: number): Promise<WarehouseResponse> =>
    http.get(`${prefix}/${id}`).then((response) => response.data),

  create: (data: CreateWarehouseRequest): Promise<WarehouseResponse> =>
    http.post(prefix, data).then((response) => response.data),

  update: (id: number, data: UpdateWarehouseRequest): Promise<WarehouseResponse> =>
    http.put(`${prefix}/${id}`, data).then((response) => response.data),

  toggle: (id: number): Promise<WarehouseResponse> =>
    http.patch(`${prefix}/${id}/toggle`, {}).then((response) => response.data),
}
