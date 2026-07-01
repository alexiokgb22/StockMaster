// Seul statut possible désormais : VALIDATED
// Le magasinier crée et valide en une seule opération
export type ReceptionStatus = 'VALIDATED'

export interface ReceptionLineResponse {
  id: number
  purchaseOrderLineId: number
  productId: number
  productName: string
  productReference: string
  categoryName: string
  quantityExpected: number
  quantityReceived: number
  gap: number
  note: string | null
  zoneId: number
  zoneName: string
}

export interface ReceptionResponse {
  id: number
  receptionNumber: string
  status: ReceptionStatus
  note: string | null
  validatedAt: string | null
  purchaseOrderId: number
  purchaseOrderNumber: string
  supplierId: number | null
  supplierName: string | null
  warehouseId: number
  warehouseName: string
  createdByUsername: string
  validatedByUsername: string | null
  lines: ReceptionLineResponse[]
  gapCount: number
  createdAt: string
  updatedAt: string
}

export interface ReceptionLineRequest {
  purchaseOrderLineId: number
  quantityReceived: number
  zoneId: number
  note?: string
}

export interface CreateReceptionRequest {
  purchaseOrderId: number
  note?: string
  lines: ReceptionLineRequest[]
}
