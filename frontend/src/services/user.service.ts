import api from './api'
import type { UserResponse, CreateUserRequest, UpdateUserRequest, ResetPasswordRequest, AssignRoleRequest } from '@/types/user.types'
import type { PaginatedResponse } from '@/types/common.types'

export interface UserFilters {
  search?: string
  roleId?: number
  active?: boolean
  page?: number
  size?: number
}

class UserService {
  // ── Lecture ────────────────────────────────────────────────────

  async getAll(filters: UserFilters = {}): Promise<PaginatedResponse<UserResponse>> {
    const params = { ...filters, page: filters.page ?? 0, size: filters.size ?? 20 }
    const res = await api.get<PaginatedResponse<UserResponse>>('/users', { params })
    return res.data
  }

  async getStorekeepers(warehouseId: number, filters: Omit<UserFilters, 'roleId'> = {}): Promise<PaginatedResponse<UserResponse>> {
    const params = { warehouseId, ...filters, page: filters.page ?? 0, size: filters.size ?? 20 }
    const res = await api.get<PaginatedResponse<UserResponse>>('/users/storekeeper-list', { params })
    return res.data
  }

  async getById(id: number): Promise<UserResponse> {
    const res = await api.get<UserResponse>(`/users/${id}`)
    return res.data
  }

  // ── Création ───────────────────────────────────────────────────

  async create(data: CreateUserRequest): Promise<UserResponse> {
    const res = await api.post<UserResponse>('/users', data)
    return res.data
  }

  async createStorekeeper(data: CreateUserRequest): Promise<UserResponse> {
    const res = await api.post<UserResponse>('/users/storekeeper', data)
    return res.data
  }

  // ── Modification ───────────────────────────────────────────────

  async update(id: number, data: UpdateUserRequest): Promise<UserResponse> {
    const res = await api.put<UserResponse>(`/users/${id}`, data)
    return res.data
  }

  // ── Toggle ─────────────────────────────────────────────────────

  async toggle(id: number): Promise<UserResponse> {
    const res = await api.patch<UserResponse>(`/users/${id}/toggle`)
    return res.data
  }

  async toggleStorekeeper(id: number): Promise<UserResponse> {
    const res = await api.patch<UserResponse>(`/users/${id}/toggle-storekeeper`)
    return res.data
  }

  // ── Mot de passe ───────────────────────────────────────────────

  async resetPassword(id: number, data: ResetPasswordRequest): Promise<void> {
    await api.patch(`/users/${id}/reset-password`, data)
  }

  // ── Rôle ───────────────────────────────────────────────────────

  async assignRole(id: number, data: AssignRoleRequest): Promise<UserResponse> {
    const res = await api.patch<UserResponse>(`/users/${id}/role`, data)
    return res.data
  }
}

export default new UserService()
