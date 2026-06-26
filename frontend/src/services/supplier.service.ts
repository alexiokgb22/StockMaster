import http from './http'
import type { SupplierResponse, CreateSupplierRequest, UpdateSupplierRequest } from '@/types/supplier.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export const supplierService = {
  listAll: (params?: { search?: string; active?: boolean; page?: number; size?: number }): Promise<PaginatedResponse<SupplierResponse>> =>
    http.get('/api/suppliers', { params }).then((r) => r.data),

  getById: (id: number): Promise<SupplierResponse> =>
    http.get(`/api/suppliers/${id}`).then((r) => r.data),

  create: (data: CreateSupplierRequest): Promise<SupplierResponse> =>
    http.post('/api/suppliers', data).then((r) => r.data),

  update: (id: number, data: UpdateSupplierRequest): Promise<SupplierResponse> =>
    http.put(`/api/suppliers/${id}`, data).then((r) => r.data),

  toggle: (id: number): Promise<SupplierResponse> =>
    http.patch(`/api/suppliers/${id}/toggle`).then((r) => r.data),
}
