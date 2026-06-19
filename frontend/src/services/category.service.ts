import http from './http'
import type { CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest } from '@/types/category.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

const prefix = '/api/categories'

export const categoryService = {
  list: (params?: { search?: string; page?: number; size?: number }): Promise<PaginatedResponse<CategoryResponse>> =>
    http.get(prefix, { params }).then((r) => r.data),

  get: (id: number): Promise<CategoryResponse> =>
    http.get(`${prefix}/${id}`).then((r) => r.data),

  create: (data: CreateCategoryRequest): Promise<CategoryResponse> =>
    http.post(prefix, data).then((r) => r.data),

  update: (id: number, data: UpdateCategoryRequest): Promise<CategoryResponse> =>
    http.put(`${prefix}/${id}`, data).then((r) => r.data),

  delete: (id: number): Promise<void> =>
    http.delete(`${prefix}/${id}`).then(() => undefined),
}
