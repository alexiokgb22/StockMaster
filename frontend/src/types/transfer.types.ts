export type TransferStatus = 'PENDING' | 'VALIDATED' | 'RECEIVED' | 'CANCELLED'

export interface TransferLineResponse {
  id: number
  productId: number
  productName: string
  productReference: string
  categoryName: string
  quantity: number
  note: string | null
  sourceZoneId: number
  sourceZoneName: string
  targetZoneId: number | null
  targetZoneName: string | null
}

export interface TransferResponse {
  id: number
  transferNumber: string
  status: TransferStatus
  note: string | null
  cancellationReason: string | null
  validatedAt: string | null
  receivedAt: string | null
  sourceWarehouseId: number
  sourceWarehouseName: string
  targetWarehouseId: number
  targetWarehouseName: string
  createdByUsername: string
  validatedByUsername: string | null
  receivedByUsername: string | null
  lines: TransferLineResponse[]
  createdAt: string
  updatedAt: string
}

export interface TransferLineRequest {
  productId: number
  sourceZoneId: number
  quantity: number
  note?: string
}

export interface CreateTransferRequest {
  targetWarehouseId: number
  note?: string
  lines: TransferLineRequest[]
}

export interface ReceiveLineRequest {
  transferLineId: number
  targetZoneId: number
}

export interface ReceiveTransferRequest {
  lines: ReceiveLineRequest[]
}

export interface CancelTransferRequest {
  reason?: string
}
