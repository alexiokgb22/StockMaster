import http from './http'
import type { CreateUserRequest, UpdateUserRequest, UserResponse } from '@/types/user.types'

const prefix = '/api/users'

export const userService = {
  list: (params?: Record<string, unknown>) =>
    http.get<{ content: UserResponse[] }>(prefix, { params }).then((response) => response.data),

  get: (id: number) =>
    http.get<UserResponse>(`${prefix}/${id}`).then((response) => response.data),

  create: (payload: CreateUserRequest) =>
    http.post<UserResponse>(prefix, payload).then((response) => response.data),

  createStorekeeper: (payload: CreateUserRequest) =>
    http.post<UserResponse>(`${prefix}/storekeeper`, payload).then((response) => response.data),

  update: (id: number, payload: UpdateUserRequest) =>
    http.put<UserResponse>(`${prefix}/${id}`, payload).then((response) => response.data),

  toggle: (id: number) =>
    http.patch<UserResponse>(`${prefix}/${id}/toggle`).then((response) => response.data),

  toggleStorekeeper: (id: number) =>
    http.patch<UserResponse>(`${prefix}/${id}/toggle-storekeeper`).then((response) => response.data),

  resetPassword: (id: number, newPassword: string) =>
    http.patch(`${prefix}/${id}/reset-password`, { newPassword }).then(() => undefined),

  delete: (id: number) =>
    http.delete(`${prefix}/${id}`).then(() => undefined),

  assignRole: (id: number, roleId: number) =>
    http.patch<UserResponse>(`${prefix}/${id}/role`, { roleId }).then((response) => response.data),
}
