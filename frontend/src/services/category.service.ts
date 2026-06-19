import http from './http'
import type {
  CategoryResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  AssignWarehouseRequest,
} from '@/types/category.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export const categoryService = {
  // ── Admin : vue globale ────────────────────────────────────────
  listAll: (params?: { search?: string; page?: number; size?: number }): Promise<PaginatedResponse<CategoryResponse>> =>
    http.get('/api/categories', { params }).then((r) => r.data),

  listUnassigned: (search?: string): Promise<CategoryResponse[]> =>
    http.get('/api/categories/unassigned', { params: { search } }).then((r) => r.data),

  create: (data: CreateCategoryRequest): Promise<CategoryResponse> =>
    http.post('/api/categories', data).then((r) => r.data),

  update: (id: number, data: UpdateCategoryRequest): Promise<CategoryResponse> =>
    http.put(`/api/categories/${id}`, data).then((r) => r.data),

  delete: (id: number): Promise<void> =>
    http.delete(`/api/categories/${id}`).then(() => undefined),

  assignToWarehouse: (id: number, data: AssignWarehouseRequest): Promise<CategoryResponse> =>
    http.patch(`/api/categories/${id}/assign`, data).then((r) => r.data),

  // ── Contexte entrepôt ──────────────────────────────────────────
  listByWarehouse: (
    warehouseId: number,
    params?: { search?: string; page?: number; size?: number },
  ): Promise<PaginatedResponse<CategoryResponse>> =>
    http.get(`/api/warehouses/${+warehouseId}/categories`, { params }).then((r) => r.data),

  createInWarehouse: (warehouseId: number, data: CreateCategoryRequest): Promise<CategoryResponse> =>
    http.post(`/api/warehouses/${+warehouseId}/categories`, data).then((r) => r.data),

  /**
   * Désaffecte une catégorie d'un entrepôt sans la supprimer.
   * La catégorie redevient disponible pour une réaffectation ultérieure.
   */
  unassignFromWarehouse: (warehouseId: number, categoryId: number): Promise<void> =>
    http.delete(`/api/warehouses/${+warehouseId}/categories/${categoryId}`).then(() => undefined),
}
