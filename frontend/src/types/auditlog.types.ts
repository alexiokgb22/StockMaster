export interface AuditLogResponse {
  id: number
  module: string
  action: string
  entityName: string
  entityId: number | null
  description: string | null
  username: string
  userRole: string | null
  warehouseId: number | null
  warehouseName: string | null
  oldValue: string | null
  newValue: string | null
  ipAddress: string | null
  createdAt: string
}

export interface AuditLogFilters {
  module?: string
  action?: string
  username?: string
  entityName?: string
  warehouseId?: number
  from?: string
  to?: string
  page?: number
  size?: number
}

/** Modules métier connus — utilisés pour les filtres dropdown */
export const AUDIT_MODULES = [
  'reception',
  'dispatch',
  'transfer',
  'stock',
  'purchaseorder',
  'inventory',
  'user',
  'warehouse',
] as const

/** Actions connues */
export const AUDIT_ACTIONS = [
  'CREATE',
  'UPDATE',
  'VALIDATE',
  'REJECT',
  'CANCEL',
  'RECEIVE',
  'DELETE',
  'COMPLETE',
] as const
