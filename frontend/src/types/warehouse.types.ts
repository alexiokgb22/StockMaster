export interface WarehouseResponse {
  id: number
  name: string
  address: string
  city: string
  totalCapacity: number
  usedCapacity: number
  isActive: boolean
  managerId?: number
  managerName?: string
  createdAt: string
  updatedAt: string
}

export interface CreateWarehouseRequest {
  name: string
  address: string
  city: string
  totalCapacity: number
  managerId?: number
}

export interface UpdateWarehouseRequest {
  name?: string
  address?: string
  city?: string
  totalCapacity?: number
  managerId?: number
}

