export type DispatchStatus = 'PENDING' | 'VALIDATED' | 'REJECTED'

export interface DispatchLineResponse {
  id: number
  productId: number
  productName: string
  productReference: string
  categoryName: string
  quantityRequested: number
  note: string | null
  zoneId: number
  zoneName: string
  stockAvailable: number
}

export interface DispatchResponse {
  id: number
  dispatchNumber: string
  status: DispatchStatus
  note: string | null
  rejectionReason: string | null
  validatedAt: string | null
  clientFirstName: string
  clientLastName: string
  clientPhone: string
  deliveryAddress: string
  warehouseId: number
  warehouseName: string
  createdByUsername: string
  validatedByUsername: string | null
  lines: DispatchLineResponse[]
  createdAt: string
  updatedAt: string
}

export interface DispatchLineRequest {
  productId: number
  zoneId: number
  quantityRequested: number
  note?: string
}

export interface CreateDispatchRequest {
  note?: string
  clientFirstName: string
  clientLastName: string
  clientPhone: string
  deliveryAddress: string
  lines: DispatchLineRequest[]
}

export interface RejectDispatchRequest {
  reason?: string
}
