import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService from '@/services/auth.service'
import type { LoginRequest, UserInfo } from '@/types/auth.types'

/**
 * Store Pinia pour l'authentification.
 * Gère l'état de connexion, les informations utilisateur et les permissions.
 */
export const useAuthStore = defineStore('auth', () => {
  // État
  const user = ref<UserInfo | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => user.value !== null)
  const userRole = computed(() => user.value?.role || null)
  const userPermissions = computed(() => user.value?.permissions || [])
  const warehouseId = computed(() => user.value?.warehouseId || null)

  /**
   * Connexion d'un utilisateur.
   */
  async function login(credentials: LoginRequest): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await authService.login(credentials)
      user.value = {
        id: response.id,
        username: response.username,
        email: response.email,
        role: response.role,
        roleId: 0, // Sera récupéré par getCurrentUser
        permissions: response.permissions,
        warehouseId: response.warehouseId
      }
      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Erreur de connexion'
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Déconnexion de l'utilisateur.
   */
  async function logout(): Promise<void> {
    try {
      await authService.logout()
    } finally {
      clearAuth()
    }
  }

  /**
   * Récupère les informations de l'utilisateur connecté.
   */
  async function fetchUser(): Promise<boolean> {
    try {
      const userData = await authService.getCurrentUser()
      user.value = userData
      return true
    } catch {
      clearAuth()
      return false
    }
  }

  /**
   * Vérifie si l'utilisateur a une permission spécifique.
   */
  function hasPermission(permission: string): boolean {
    return userPermissions.value.includes(permission)
  }

  /**
   * Vérifie si l'utilisateur a au moins une des permissions.
   */
  function hasAnyPermission(permissions: string[]): boolean {
    return permissions.some((perm) => hasPermission(perm))
  }

  /**
   * Vérifie si l'utilisateur a toutes les permissions.
   */
  function hasAllPermissions(permissions: string[]): boolean {
    return permissions.every((perm) => hasPermission(perm))
  }

  /**
   * Efface l'état d'authentification.
   */
  function clearAuth(): void {
    user.value = null
    error.value = null
  }

  return {
    // État
    user,
    isLoading,
    error,
    // Getters
    isAuthenticated,
    userRole,
    userPermissions,
    warehouseId,
    // Actions
    login,
    logout,
    fetchUser,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    clearAuth
  }
})
