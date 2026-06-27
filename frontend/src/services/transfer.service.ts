import http from './http'
import type {
  TransferResponse,
  TransferStatus,
  CreateTransferRequest,
  ReceiveTransferRequest,
  CancelTransferRequest,
} from '@/types/transfer.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

const warehouseBase = (warehouseId: number) => `/api/warehouses/${warehouseId}/transfers`

export const transferService = {
  // ── Vue gestionnaire source ───────────────────────────────────
  listOutgoing: (
    warehouseId: number,
    params?: { status?: TransferStatus; page?: number; size?: number },
  ): Promise<PaginatedResponse<TransferResponse>> =>
    http.get(`${warehouseBase(warehouseId)}/outgoing`, { params }).then((r) => r.data),

  // ── Vue gestionnaire cible ────────────────────────────────────
  listIncoming: (
    warehouseId: number,
    params?: { status?: TransferStatus; page?: number; size?: number },
  ): Promise<PaginatedResponse<TransferResponse>> =>
    http.get(`${warehouseBase(warehouseId)}/incoming`, { params }).then((r) => r.data),

  countIncoming: (warehouseId: number): Promise<number> =>
    http.get(`${warehouseBase(warehouseId)}/incoming/count`).then((r) => r.data),

  create: (warehouseId: number, data: CreateTransferRequest): Promise<TransferResponse> =>
    http.post(warehouseBase(warehouseId), data).then((r) => r.data),

  receive: (
    warehouseId: number,
    transferId: number,
    data: ReceiveTransferRequest,
  ): Promise<TransferResponse> =>
    http
      .patch(`${warehouseBase(warehouseId)}/${transferId}/receive`, data)
      .then((r) => r.data),

  // ── Vue admin ─────────────────────────────────────────────────
  listAll: (params?: {
    status?: TransferStatus
    warehouseId?: number
    page?: number
    size?: number
  }): Promise<PaginatedResponse<TransferResponse>> =>
    http.get('/api/transfers', { params }).then((r) => r.data),

  getById: (transferId: number): Promise<TransferResponse> =>
    http.get(`/api/transfers/${transferId}`).then((r) => r.data),

  validate: (transferId: number): Promise<TransferResponse> =>
    http.patch(`/api/transfers/${transferId}/validate`).then((r) => r.data),

  cancel: (transferId: number, data?: CancelTransferRequest): Promise<TransferResponse> =>
    http.patch(`/api/transfers/${transferId}/cancel`, data ?? {}).then((r) => r.data),
}
