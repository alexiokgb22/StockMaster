import http from './http'
import type {
  PurchaseOrderResponse,
  PurchaseOrderStatus,
  CreatePurchaseOrderRequest,
  ValidatePurchaseOrderRequest,
} from '@/types/purchaseorder.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export const purchaseOrderService = {
  // Gestionnaire : commandes de son entrepôt
  listByWarehouse: (
    warehouseId: number,
    params?: { status?: PurchaseOrderStatus; page?: number; size?: number },
  ): Promise<PaginatedResponse<PurchaseOrderResponse>> =>
    http.get(`/api/warehouses/${warehouseId}/purchase-orders`, { params }).then((r) => r.data),

  getById: (warehouseId: number, orderId: number): Promise<PurchaseOrderResponse> =>
    http.get(`/api/warehouses/${warehouseId}/purchase-orders/${orderId}`).then((r) => r.data),

  // Admin : toutes les commandes
  listAll: (params?: {
    status?: PurchaseOrderStatus
    warehouseId?: number
    page?: number
    size?: number
  }): Promise<PaginatedResponse<PurchaseOrderResponse>> =>
    http.get('/api/purchase-orders', { params }).then((r) => r.data),

  create: (warehouseId: number, data: CreatePurchaseOrderRequest): Promise<PurchaseOrderResponse> =>
    http.post(`/api/warehouses/${warehouseId}/purchase-orders`, data).then((r) => r.data),

  validate: (warehouseId: number, orderId: number, data: ValidatePurchaseOrderRequest): Promise<PurchaseOrderResponse> =>
    http.patch(`/api/warehouses/${warehouseId}/purchase-orders/${orderId}/validate`, data).then((r) => r.data),

  deliver: (warehouseId: number, orderId: number): Promise<PurchaseOrderResponse> =>
    http.patch(`/api/warehouses/${warehouseId}/purchase-orders/${orderId}/deliver`).then((r) => r.data),

  close: (warehouseId: number, orderId: number): Promise<PurchaseOrderResponse> =>
    http.patch(`/api/warehouses/${warehouseId}/purchase-orders/${orderId}/close`).then((r) => r.data),

  cancel: (warehouseId: number, orderId: number): Promise<PurchaseOrderResponse> =>
    http.patch(`/api/warehouses/${warehouseId}/purchase-orders/${orderId}/cancel`).then((r) => r.data),

  // Admin : historique des commandes d'un fournisseur (pour SupplierDetailPage)
  listBySupplierId: (
    supplierId: number,
    params?: { page?: number; size?: number },
  ): Promise<PaginatedResponse<PurchaseOrderResponse>> =>
    http.get(`/api/purchase-orders/by-supplier/${supplierId}`, { params }).then((r) => r.data),
}
