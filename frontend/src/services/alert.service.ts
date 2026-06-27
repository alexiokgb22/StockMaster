import http from './http'
import type { AlertResponse, AlertType, AlertSeverity } from '@/types/alert.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export const alertService = {
  // Liste filtrée des alertes
  list: (params?: {
    warehouseId?: number
    type?: AlertType
    severity?: AlertSeverity
    isRead?: boolean
    isResolved?: boolean
    page?: number
    size?: number
  }): Promise<PaginatedResponse<AlertResponse>> =>
    http.get('/api/alerts', { params }).then((r) => r.data),

  // Compteur non lus (pour le badge)
  countUnread: (params?: { warehouseId?: number }): Promise<{ count: number }> =>
    http.get('/api/alerts/count-unread', { params }).then((r) => r.data),

  // Marquer une alerte comme lue
  markAsRead: (alertId: number): Promise<AlertResponse> =>
    http.patch(`/api/alerts/${alertId}/read`).then((r) => r.data),

  // Résoudre une alerte
  resolve: (alertId: number): Promise<AlertResponse> =>
    http.patch(`/api/alerts/${alertId}/resolve`).then((r) => r.data),

  // Tout marquer comme lu
  markAllAsRead: (params?: { warehouseId?: number }): Promise<{ updated: number }> =>
    http.patch('/api/alerts/read-all', null, { params }).then((r) => r.data),
}
