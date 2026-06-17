import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

/**
 * Composable pour gérer les permissions.
 * Fournit des helpers pour vérifier les permissions de manière réactive.
 */
export function usePermissions() {
  const authStore = useAuthStore()

  const hasPermission = (permission: string) => computed(() => authStore.hasPermission(permission))

  const hasAnyPermission = (permissions: string[]) =>
    computed(() => authStore.hasAnyPermission(permissions))

  const hasAllPermissions = (permissions: string[]) =>
    computed(() => authStore.hasAllPermissions(permissions))

  const userRole = computed(() => authStore.userRole)

  const isAdmin = computed(() => authStore.userRole === 'Administrateur')

  const isManager = computed(() => authStore.userRole === 'Gestionnaire d\'entrepôt')

  const isStorekeeper = computed(() => authStore.userRole === 'Magasinier')

  const isAuditor = computed(() => authStore.userRole === 'Auditeur')

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    userRole,
    isAdmin,
    isManager,
    isStorekeeper,
    isAuditor
  }
}
