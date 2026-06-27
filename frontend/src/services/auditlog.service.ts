import http from './http'
import type { AuditLogResponse, AuditLogFilters } from '@/types/auditlog.types'

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export const auditLogService = {
  /**
   * Recherche paginée avec filtres.
   */
  search: (filters: AuditLogFilters = {}): Promise<PaginatedResponse<AuditLogResponse>> =>
    http.get('/api/audit-logs', { params: filters }).then((r) => r.data),

  /**
   * Détail d'un log.
   */
  findById: (id: number): Promise<AuditLogResponse> =>
    http.get(`/api/audit-logs/${id}`).then((r) => r.data),

  /**
   * Historique de toutes les actions sur une entité précise.
   * Ex: auditLogService.findByEntity('Reception', 42)
   */
  findByEntity: (
    entityName: string,
    entityId: number,
    page = 0,
    size = 20,
  ): Promise<PaginatedResponse<AuditLogResponse>> =>
    http
      .get(`/api/audit-logs/entity/${entityName}/${entityId}`, { params: { page, size } })
      .then((r) => r.data),
}
