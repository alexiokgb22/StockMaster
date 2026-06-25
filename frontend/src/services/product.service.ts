import http from './http'
import type { ProductResponse, CreateProductRequest, UpdateProductRequest } from '@/types/product.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export const productService = {
  // ── Admin : catalogue global ───────────────────────────────────
  listAll: (params?: {
    search?: string
    categoryId?: number
    active?: boolean
    page?: number
    size?: number
  }): Promise<PaginatedResponse<ProductResponse>> =>
    http.get('/api/products', { params }).then((r) => r.data),

  getById: (id: number): Promise<ProductResponse> =>
    http.get(`/api/products/${id}`).then((r) => r.data),

  create: (data: CreateProductRequest): Promise<ProductResponse> =>
    http.post('/api/products', data).then((r) => r.data),

  update: (id: number, data: UpdateProductRequest): Promise<ProductResponse> =>
    http.put(`/api/products/${id}`, data).then((r) => r.data),

  updateWarehouses: (id: number, warehouseIds: number[]): Promise<ProductResponse> =>
    http.put(`/api/products/${id}/warehouses`, { warehouseIds }).then((r) => r.data),

  toggle: (id: number): Promise<ProductResponse> =>
    http.patch(`/api/products/${id}/toggle`).then((r) => r.data),

  getWarehousesByCategory: (categoryId: number): Promise<number[]> =>
    http.get('/api/products/warehouses-by-category', { params: { categoryId } }).then((r) => r.data),

  // ── Contexte entrepôt (admin + gestionnaire) ──────────────────
  listByWarehouse: (
    warehouseId: number,
    params?: { search?: string; categoryId?: number; page?: number; size?: number },
  ): Promise<PaginatedResponse<ProductResponse>> =>
    http.get(`/api/warehouses/${warehouseId}/products`, { params }).then((r) => r.data),

  createInWarehouse: (
    warehouseId: number,
    data: CreateProductRequest,
  ): Promise<ProductResponse> =>
    http.post(`/api/warehouses/${warehouseId}/products`, data).then((r) => r.data),

  // ── Select produits pour création de stock ────────────────────
  listForSelect: (
    warehouseId: number,
    categoryId?: number,
  ): Promise<ProductResponse[]> =>
    http
      .get(`/api/warehouses/${warehouseId}/products/select`, {
        params: categoryId ? { categoryId } : undefined,
      })
      .then((r) => r.data),
}
