import http from './http'
import type {
  ReceptionResponse,
  ReceptionStatus,
  CreateReceptionRequest,
  RejectReceptionRequest,
} from '@/types/reception.types'
import type { PurchaseOrderResponse } from '@/types/purchaseorder.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export const receptionService = {
  // Liste des bons de réception d'un entrepôt
  list: (
    warehouseId: number,
    params?: { status?: ReceptionStatus; page?: number; size?: number },
  ): Promise<PaginatedResponse<ReceptionResponse>> =>
    http.get(`/api/warehouses/${warehouseId}/receptions`, { params }).then((r) => r.data),

  // Commandes DELIVERED disponibles pour réception (Magasinier)
  listDeliverable: (
    warehouseId: number,
    params?: { page?: number; size?: number },
  ): Promise<PaginatedResponse<PurchaseOrderResponse>> =>
    http.get(`/api/warehouses/${warehouseId}/receptions/deliverable`, { params }).then((r) => r.data),

  // Compteur PENDING pour le badge gestionnaire
  countPending: (warehouseId: number): Promise<number> =>
    http.get(`/api/warehouses/${warehouseId}/receptions/pending-count`).then((r) => r.data),

  // Détail d'un bon
  getById: (warehouseId: number, receptionId: number): Promise<ReceptionResponse> =>
    http.get(`/api/warehouses/${warehouseId}/receptions/${receptionId}`).then((r) => r.data),

  // Création (Magasinier)
  create: (warehouseId: number, data: CreateReceptionRequest): Promise<ReceptionResponse> =>
    http.post(`/api/warehouses/${warehouseId}/receptions`, data).then((r) => r.data),

  // Validation (Gestionnaire)
  validate: (warehouseId: number, receptionId: number): Promise<ReceptionResponse> =>
    http.patch(`/api/warehouses/${warehouseId}/receptions/${receptionId}/validate`).then((r) => r.data),

  // Rejet (Gestionnaire)
  reject: (
    warehouseId: number,
    receptionId: number,
    data?: RejectReceptionRequest,
  ): Promise<ReceptionResponse> =>
    http
      .patch(`/api/warehouses/${warehouseId}/receptions/${receptionId}/reject`, data ?? {})
      .then((r) => r.data),
}
