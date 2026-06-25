export interface SupplierResponse {
  id: number
  name: string
  address: string | null
  city: string | null
  phone: string | null
  email: string | null
  contactName: string | null
  isActive: boolean
  warehouseIds: number[]
  warehouseNames: string[]
  createdAt: string
  updatedAt: string
}

export interface SupplierWarehouseResponse {
  id: number
  name: string
  city: string | null
  deliveryCount: number
}

export interface DeliveryHistoryResponse {
  orderId: number
  orderNumber: string
  status: 'DRAFT' | 'VALIDATED' | 'DELIVERED' | 'CANCELLED'
  orderDate: string | null
  expectedDate: string | null
  totalAmount: number | null
  createdByUsername: string
  lines: DeliveryLineResponse[]
  createdAt: string
}

export interface DeliveryLineResponse {
  productId: number
  productName: string
  productReference: string
  quantity: number
  receivedQty: number | null
  unitPrice: number | null
}

export interface CreateSupplierRequest {
  name: string
  address?: string
  city?: string
  phone?: string
  email?: string
  contactName?: string
  warehouseIds?: number[]
}

export interface UpdateSupplierRequest {
  name?: string
  address?: string
  city?: string
  phone?: string
  email?: string
  contactName?: string
  warehouseIds?: number[]
}
