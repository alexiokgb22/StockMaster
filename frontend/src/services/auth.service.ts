import http from './http'
import type { LoginRequest, LoginResponse, UserInfoResponse } from '@/types/auth.types'

const prefix = '/api/auth'

export const authService = {
  login: (payload: LoginRequest): Promise<LoginResponse> =>
    http.post(`${prefix}/login`, payload).then((response) => response.data),

  logout: (): Promise<void> =>
    http.post(`${prefix}/logout`).then(() => undefined),

  fetchCurrentUser: (): Promise<UserInfoResponse> =>
    http.get(`${prefix}/me`).then((response) => response.data),

  checkAuth: (): Promise<boolean> =>
    http.get(`${prefix}/check`).then((response) => response.data),

  changePassword: (newPassword: string): Promise<void> =>
    http.post(`${prefix}/change-password`, { newPassword }).then(() => undefined),
}
