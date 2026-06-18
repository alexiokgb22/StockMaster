import api from './api'
import type { LoginRequest, LoginResponse, UserInfo } from '@/types/auth.types'

/**
 * Service d'authentification.
 * Gère toutes les requêtes API liées à l'authentification.
 */
class AuthService {
  /**
   * Connecte un utilisateur.
   */
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await api.post<LoginResponse>('/auth/login', credentials)
    return response.data
  }

  /**
   * Déconnecte l'utilisateur.
   */
  async logout(): Promise<void> {
    await api.post('/auth/logout')
  }

  /**
   * Récupère les informations de l'utilisateur connecté.
   */
  async getCurrentUser(): Promise<UserInfo> {
    const response = await api.get<UserInfo>('/auth/me')
    return response.data
  }

  /**
   * Vérifie si l'utilisateur est authentifié.
   */
  async checkAuth(): Promise<boolean> {
    try {
      const response = await api.get<boolean>('/auth/check')
      return response.data
    } catch {
      return false
    }
  }

  async changePassword(newPassword: string): Promise<void> {
    await api.post('/auth/change-password', { newPassword })
  }
}

export default new AuthService()
