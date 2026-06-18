import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import type { LoginRequest } from '@/types/auth.types'

export const useAuth = () => {
  const authStore = useAuthStore()
  const { currentUser, isAuthenticated, loading, errorMessage } = storeToRefs(authStore)

  return {
    currentUser,
    isAuthenticated,
    loading,
    errorMessage,
    login: authStore.login,
    logout: authStore.logout,
    fetchCurrentUser: authStore.fetchCurrentUser,
    checkAuth: authStore.initialize,
    changePassword: authStore.changePassword,
    hasPermission: authStore.hasPermission,
    async signIn(payload: LoginRequest) {
      await authStore.login(payload)
      await authStore.fetchCurrentUser()
    },
  }
}
