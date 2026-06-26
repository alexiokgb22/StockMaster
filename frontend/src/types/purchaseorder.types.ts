export type PurchaseOrderStatus = 'DRAFT' | 'VALIDATED' | 'DELIVERED' | 'CLOSED' | 'CANCELLED'

export interface OrderLineResponse {
  id: number
  productId: number
  productName: string
  productReference: string
  categoryName: string
  quantity: number
  receivedQty: number
  unitPrice: number | null
}

export interface PurchaseOrderResponse {
  id: number
  orderNumber: string
  status: PurchaseOrderStatus
  orderDate: string | null
  expectedDate: string | null
  note: string | null
  totalAmount: number | null
  warehouseId: number
  warehouseName: string
  supplierId: number | null
  supplierName: string | null
  supplierContactName: string | null
  supplierPhone: string | null
  supplierEmail: string | null
  createdByUsername: string
  lines: OrderLineResponse[]
  createdAt: string
  updatedAt: string
}

export interface OrderLineRequest {
  productId: number
  quantity: number
  unitPrice?: number
}

export interface CreatePurchaseOrderRequest {
  expectedDate?: string
  note?: string
  lines: OrderLineRequest[]
}

export interface ValidatePurchaseOrderRequest {
  supplierId: number
  expectedDate?: string
  note?: string
}
