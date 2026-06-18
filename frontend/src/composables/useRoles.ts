import { ref } from 'vue'
import { roleService } from '@/services/role.service'
import type { RoleResponse } from '@/types/role.types'

export const useRoles = () => {
  const roles = ref<RoleResponse[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const load = async () => {
    loading.value = true
    error.value = null
    try {
      roles.value = await roleService.list()
    } catch (e) {
      error.value = 'Impossible de charger les rôles.'
    } finally {
      loading.value = false
    }
  }

  return {
    roles,
    loading,
    error,
    load,
  }
}
