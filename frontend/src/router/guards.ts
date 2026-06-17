import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

/**
 * Guard pour protéger les routes nécessitant une authentification.
 */
export async function authGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  const authStore = useAuthStore()

  // Si l'utilisateur est déjà authentifié
  if (authStore.isAuthenticated) {
    return next()
  }

  // Tentative de récupération de l'utilisateur (cookie peut être présent)
  const authenticated = await authStore.fetchUser()

  if (authenticated) {
    return next()
  }

  // Redirection vers la page de login
  return next({ name: 'login', query: { redirect: to.fullPath } })
}

/**
 * Guard pour les routes accessibles uniquement aux utilisateurs non connectés.
 */
export function guestGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  const authStore = useAuthStore()

  if (authStore.isAuthenticated) {
    return next({ name: 'dashboard' })
  }

  return next()
}

/**
 * Factory pour créer un guard basé sur les permissions.
 */
export function permissionGuard(requiredPermissions: string[]) {
  return (
    to: RouteLocationNormalized,
    from: RouteLocationNormalized,
    next: NavigationGuardNext
  ) => {
    const authStore = useAuthStore()

    if (!authStore.isAuthenticated) {
      return next({ name: 'login' })
    }

    if (authStore.hasAnyPermission(requiredPermissions)) {
      return next()
    }

    // Accès refusé
    return next({ name: 'forbidden' })
  }
}
