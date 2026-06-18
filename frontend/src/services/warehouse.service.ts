import http from './http'
import type { WarehouseResponse } from '@/types/warehouse.types'

const prefix = '/api/warehouses'

export const warehouseService = {
  list: (): Promise<WarehouseResponse[]> =>
    http.get<WarehouseResponse[]>(prefix).then((response) => response.data),
}
