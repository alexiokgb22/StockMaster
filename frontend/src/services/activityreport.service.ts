import http from './http'
import type {
  ActivityReportResponse,
  ActivityReportStatus,
  CreateActivityReportRequest,
  UpdateActivityReportRequest,
} from '@/types/activityreport.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export const activityReportService = {
  // ── Magasinier — ses propres rapports ──────────────────────
  listMine: (params?: {
    status?: ActivityReportStatus
    page?: number
    size?: number
  }): Promise<PaginatedResponse<ActivityReportResponse>> =>
    http.get('/api/my/activity-reports', { params }).then((r) => r.data),

  // ── Gestionnaire / Admin — rapports d'un entrepôt ───────────
  listByWarehouse: (
    warehouseId: number,
    params?: {
      storekeeperId?: number
      status?: ActivityReportStatus
      from?: string   // yyyy-MM-dd
      to?: string     // yyyy-MM-dd
      page?: number
      size?: number
    },
  ): Promise<PaginatedResponse<ActivityReportResponse>> =>
    http
      .get(`/api/warehouses/${warehouseId}/activity-reports`, { params })
      .then((r) => r.data),

  // ── Détail complet ──────────────────────────────────────────
  getById: (id: number): Promise<ActivityReportResponse> =>
    http.get(`/api/activity-reports/${id}`).then((r) => r.data),

  // ── Création (DRAFT) ────────────────────────────────────────
  create: (data: CreateActivityReportRequest): Promise<ActivityReportResponse> =>
    http.post('/api/my/activity-reports', data).then((r) => r.data),

  // ── Modification ────────────────────────────────────────────
  update: (
    id: number,
    data: UpdateActivityReportRequest,
  ): Promise<ActivityReportResponse> =>
    http.put(`/api/activity-reports/${id}`, data).then((r) => r.data),

  // ── Soumission ──────────────────────────────────────────────
  submit: (id: number): Promise<ActivityReportResponse> =>
    http.patch(`/api/activity-reports/${id}/submit`).then((r) => r.data),
}
