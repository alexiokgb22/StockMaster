import http from './http'
import type {
  GlobalDashboardResponse,
  WarehouseDashboardResponse,
  MovementChartPoint,
  CategoryStockPoint,
  TopProductPoint,
} from '@/types/dashboard.types'

export const dashboardService = {
  // KPIs globaux — Admin
  getGlobalSummary: (): Promise<GlobalDashboardResponse> =>
    http.get('/api/dashboard/summary').then((r) => r.data),

  // KPIs d'un entrepôt — Gestionnaire + Admin
  getWarehouseSummary: (warehouseId: number): Promise<WarehouseDashboardResponse> =>
    http.get(`/api/dashboard/warehouse/${warehouseId}`).then((r) => r.data),

  // Graphique entrées/sorties
  getMovementsChart: (params?: {
    warehouseId?: number
    days?: number
  }): Promise<MovementChartPoint[]> =>
    http.get('/api/dashboard/movements-chart', { params }).then((r) => r.data),

  // Répartition par catégorie
  getStockByCategory: (params?: { warehouseId?: number }): Promise<CategoryStockPoint[]> =>
    http.get('/api/dashboard/stock-by-category', { params }).then((r) => r.data),

  // Top produits mouvementés
  getTopProducts: (params?: {
    warehouseId?: number
    days?: number
    limit?: number
  }): Promise<TopProductPoint[]> =>
    http.get('/api/dashboard/top-products', { params }).then((r) => r.data),
}
