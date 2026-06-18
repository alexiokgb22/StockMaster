import http from './http'
import type { RoleResponse } from '@/types/role.types'

const prefix = '/api/roles'

export const roleService = {
  list: (): Promise<RoleResponse[]> =>
    http.get<RoleResponse[]>(prefix).then((response) => response.data),
}
