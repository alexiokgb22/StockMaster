import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export const useAuthGuard = () => {
  const authStore = useAuthStore()

  return async (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const requiresAuth = Boolean(to.meta?.requiresAuth)
    const isLoginRoute = to.name === 'Login'

    if (!authStore.initialized) {
      await authStore.initialize()
    }

    if (requiresAuth && !authStore.isAuthenticated) {
      return next({ name: 'Login', query: { redirect: to.fullPath } })
    }

    if (authStore.currentUser?.mustChangePassword && to.name !== 'ChangePassword') {
      return next({ name: 'ChangePassword' })
    }

    if (isLoginRoute && authStore.isAuthenticated) {
      return next({ name: 'Dashboard' })
    }

    return next()
  }
}

export const usePermissionGuard = () => {
  const authStore = useAuthStore()

  return (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const requiredPermission = to.meta?.permission as string | undefined
    const requiredRole       = to.meta?.role as string | undefined

    // Vérification de permission
    if (requiredPermission && !authStore.hasPermission(requiredPermission)) {
      return next({ name: 'Forbidden' })
    }

    // Vérification de rôle (en plus de la permission)
    if (requiredRole && authStore.currentUser?.role !== requiredRole) {
      return next({ name: 'Forbidden' })
    }

    return next()
  }
}
