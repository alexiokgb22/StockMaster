import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

export const usePermissions = () => {
  const authStore = useAuthStore()

  const permissions = computed(() => authStore.currentUser?.permissions ?? [])
  const hasPermission = (permission: string) => authStore.hasPermission(permission)
  const can = (permission: string) => hasPermission(permission)

  return {
    permissions,
    hasPermission,
    can,
  }
}
