import { defineStore } from 'pinia'
import { authService } from '@/services/auth.service'
import type { LoginRequest, UserInfoResponse } from '@/types/auth.types'

interface AuthState {
  currentUser: UserInfoResponse | null
  isAuthenticated: boolean
  loading: boolean
  initialized: boolean
  errorMessage: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    currentUser: null,
    isAuthenticated: false,
    loading: false,
    initialized: false,
    errorMessage: null,
  }),
  getters: {
    permissions: (state) => state.currentUser?.permissions ?? [],
    hasPermission: (state) => {
      return (permission: string) => state.currentUser?.permissions.includes(permission) ?? false
    },
  },
  actions: {
    async initialize() {
      if (this.initialized) {
        return
      }

      this.loading = true
      try {
        const authorized = await authService.checkAuth()
        if (authorized) {
          await this.fetchCurrentUser()
        } else {
          this.clearSession()
        }
      } catch (error) {
        this.clearSession()
      } finally {
        this.initialized = true
        this.loading = false
      }
    },

    async login(payload: LoginRequest) {
      this.loading = true
      this.errorMessage = null

      try {
        await authService.login(payload)
        this.isAuthenticated = true
        this.initialized = true
      } catch (error: unknown) {
        this.errorMessage = 'Impossible de se connecter. Vérifiez vos identifiants.'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchCurrentUser() {
      this.loading = true
      try {
        const response = await authService.fetchCurrentUser()
        this.currentUser = response
        this.isAuthenticated = true
      } finally {
        this.loading = false
      }
    },

    async logout() {
      this.loading = true
      try {
        await authService.logout()
      } finally {
        this.clearSession()
        this.loading = false
      }
    },

    async changePassword(newPassword: string) {
      this.loading = true
      try {
        await authService.changePassword(newPassword)
        if (this.currentUser) {
          this.currentUser.mustChangePassword = false
        }
      } finally {
        this.loading = false
      }
    },

    clearSession() {
      this.currentUser = null
      this.isAuthenticated = false
      this.loading = false
    },
  },
})
