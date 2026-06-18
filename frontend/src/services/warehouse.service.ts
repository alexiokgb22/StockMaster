import api from './api'

export interface WarehouseOption {
  id: number
  name: string
  city: string | null
}

class WarehouseService {
  async getAll(): Promise<WarehouseOption[]> {
    const res = await api.get<WarehouseOption[]>('/warehouses')
    return res.data
  }
}

export default new WarehouseService()
