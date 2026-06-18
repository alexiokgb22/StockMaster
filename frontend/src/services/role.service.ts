import api from './api'
import type { RoleOption } from '@/types/user.types'

class RoleService {
  async getAll(): Promise<RoleOption[]> {
    const res = await api.get<RoleOption[]>('/roles')
    return res.data
  }
}

export default new RoleService()
