export type ActivityReportStatus = 'DRAFT' | 'SUBMITTED'

export interface OperationSummary {
  id: number
  number: string
  type: 'RECEPTION' | 'DISPATCH'
  lineCount: number
  doneAt: string
}

export interface ActivityReportResponse {
  id: number
  reportDate: string
  arrivalTime: string | null
  departureTime: string | null
  incidents: string | null
  observations: string | null
  receptionCount: number
  dispatchCount: number
  status: ActivityReportStatus
  storekeeperId: number
  storekeeperUsername: string
  warehouseId: number
  warehouseName: string
  receptions?: OperationSummary[]
  dispatches?: OperationSummary[]
  createdAt: string
  updatedAt: string
}

export interface CreateActivityReportRequest {
  reportDate: string           // yyyy-MM-dd
  arrivalTime?: string         // HH:mm
  departureTime?: string       // HH:mm
  incidents?: string
  observations?: string
}

export interface UpdateActivityReportRequest {
  arrivalTime?: string
  departureTime?: string
  incidents?: string
  observations?: string
}
