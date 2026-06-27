<template>
  <div class="space-y-6">
    <PageHeader title="Tableau de bord" subtitle="Vue d'ensemble de votre activité" />

    <!-- ── Vue Admin ─────────────────────────────────────────── -->
    <template v-if="isAdmin">
      <div v-if="loadingGlobal" class="py-12 text-center text-sm text-text-secondary">
        Chargement…
      </div>
      <template v-else-if="globalData">
        <!-- KPIs référentiel -->
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <MetricCard
            label="Entrepôts actifs"
            :value="`${globalData.activeWarehouses} / ${globalData.totalWarehouses}`"
            description="Sites opérationnels"
          />
          <MetricCard
            label="Produits actifs"
            :value="globalData.activeProducts"
            description="Références au catalogue"
          />
          <MetricCard
            label="Fournisseurs"
            :value="globalData.activeSuppliers"
            description="Partenaires actifs"
          />
          <MetricCard
            label="Utilisateurs"
            :value="globalData.activeUsers"
            description="Comptes actifs"
          />
        </div>

        <!-- KPIs flux en attente -->
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <KpiCard
            label="Commandes à livrer"
            :value="globalData.pendingPurchaseOrders"
            color="blue"
            icon="📦"
          />
          <KpiCard
            label="Réceptions en attente"
            :value="globalData.pendingReceptions"
            color="amber"
            icon="📥"
          />
          <KpiCard
            label="Sorties en attente"
            :value="globalData.pendingDispatches"
            color="purple"
            icon="📤"
          />
          <KpiCard
            label="Transferts en transit"
            :value="globalData.pendingTransfers"
            color="teal"
            icon="🔄"
          />
        </div>

        <!-- Alertes -->
        <div class="grid gap-4 sm:grid-cols-2">
          <KpiCard
            label="Alertes non lues"
            :value="globalData.totalUnreadAlerts"
            color="red"
            icon="🔔"
            :action="globalData.totalUnreadAlerts > 0 ? 'Voir les alertes' : undefined"
            @action="$router.push({ name: 'Alerts' })"
          />
          <KpiCard
            label="Ruptures de stock"
            :value="globalData.globalStockOutCount"
            color="red"
            icon="⚠"
          />
        </div>

        <!-- Graphiques -->
        <div class="grid gap-6 xl:grid-cols-2">
          <MovementsChart :data="movementsChart" :loading="loadingCharts" />
          <CategoryChart :data="categoryChart" :loading="loadingCharts" />
        </div>

        <!-- Top produits -->
        <TopProductsTable :data="topProducts" :loading="loadingCharts" />
      </template>
    </template>

    <!-- ── Vue Gestionnaire / Magasinier ─────────────────────── -->
    <template v-else-if="warehouseId">
      <div v-if="loadingWarehouse" class="py-12 text-center text-sm text-text-secondary">
        Chargement…
      </div>
      <template v-else-if="warehouseData">
        <!-- KPIs stock -->
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <MetricCard
            label="Lignes de stock"
            :value="warehouseData.totalStockLines"
            description="Articles en stock"
          />
          <KpiCard
            label="Ruptures totales"
            :value="warehouseData.stockOutCount"
            color="red"
            icon="🚨"
          />
          <KpiCard
            label="Stocks sous seuil"
            :value="warehouseData.belowMinCount"
            color="amber"
            icon="⚠"
          />
          <BaseCard>
            <div class="text-sm text-text-secondary">Capacité utilisée</div>
            <div class="mt-1 text-2xl font-semibold text-text-main">
              {{ warehouseData.capacityPercent != null
                  ? warehouseData.capacityPercent.toFixed(1) + '%'
                  : '—' }}
            </div>
            <div class="mt-2 h-2 w-full rounded-full bg-gray-100">
              <div
                :class="[
                  'h-2 rounded-full transition-all',
                  (warehouseData.capacityPercent ?? 0) >= 85 ? 'bg-red-500' :
                  (warehouseData.capacityPercent ?? 0) >= 70 ? 'bg-amber-500' : 'bg-green-500',
                ]"
                :style="{ width: `${Math.min(warehouseData.capacityPercent ?? 0, 100)}%` }"
              />
            </div>
          </BaseCard>
        </div>

        <!-- Flux en attente -->
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <KpiCard label="Commandes à livrer"    :value="warehouseData.pendingPurchaseOrders" color="blue"   icon="📦" />
          <KpiCard label="Réceptions en attente" :value="warehouseData.pendingReceptions"     color="amber"  icon="📥" />
          <KpiCard label="Sorties en attente"    :value="warehouseData.pendingDispatches"     color="purple" icon="📤" />
          <KpiCard label="Transferts entrants"   :value="warehouseData.incomingTransfers"     color="teal"   icon="🔄" />
        </div>

        <!-- Alertes -->
        <KpiCard
          label="Alertes actives"
          :value="warehouseData.activeAlerts"
          color="red"
          icon="🔔"
          :action="warehouseData.activeAlerts > 0 ? 'Voir les alertes' : undefined"
          @action="$router.push({ name: 'Alerts' })"
        />

        <!-- Graphiques -->
        <div class="grid gap-6 xl:grid-cols-2">
          <MovementsChart :data="movementsChart" :loading="loadingCharts" />
          <CategoryChart  :data="categoryChart"  :loading="loadingCharts" />
        </div>

        <TopProductsTable :data="topProducts" :loading="loadingCharts" />
      </template>
    </template>

    <!-- ── Auditeur ou rôle sans entrepôt ────────────────────── -->
    <template v-else>
      <BaseCard>
        <p class="text-sm text-text-secondary">
          Bienvenue sur StockMaster. Votre profil ne dispose pas encore d'un entrepôt assigné.
        </p>
      </BaseCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { dashboardService } from '@/services/dashboard.service'
import type {
  GlobalDashboardResponse,
  WarehouseDashboardResponse,
  MovementChartPoint,
  CategoryStockPoint,
  TopProductPoint,
} from '@/types/dashboard.types'
import PageHeader   from '@/components/ui/PageHeader.vue'
import MetricCard   from '@/components/ui/MetricCard.vue'
import BaseCard     from '@/components/ui/BaseCard.vue'
import KpiCard      from '@/components/dashboard/KpiCard.vue'
import MovementsChart  from '@/components/dashboard/MovementsChart.vue'
import CategoryChart   from '@/components/dashboard/CategoryChart.vue'
import TopProductsTable from '@/components/dashboard/TopProductsTable.vue'

const authStore = useAuthStore()

const isAdmin     = computed(() => authStore.currentUser?.role === 'Administrateur')
const warehouseId = computed(() => authStore.currentUser?.warehouseId ?? null)

// ── État ─────────────────────────────────────────────────────
const globalData    = ref<GlobalDashboardResponse | null>(null)
const warehouseData = ref<WarehouseDashboardResponse | null>(null)
const movementsChart = ref<MovementChartPoint[]>([])
const categoryChart  = ref<CategoryStockPoint[]>([])
const topProducts    = ref<TopProductPoint[]>([])

const loadingGlobal   = ref(false)
const loadingWarehouse = ref(false)
const loadingCharts   = ref(false)

// ── Chargements ───────────────────────────────────────────────
async function loadAdmin() {
  loadingGlobal.value = true
  try {
    globalData.value = await dashboardService.getGlobalSummary()
  } finally {
    loadingGlobal.value = false
  }
  loadCharts(undefined)
}

async function loadWarehouse(wid: number) {
  loadingWarehouse.value = true
  try {
    warehouseData.value = await dashboardService.getWarehouseSummary(wid)
  } finally {
    loadingWarehouse.value = false
  }
  loadCharts(wid)
}

async function loadCharts(wid: number | undefined) {
  loadingCharts.value = true
  try {
    const [movements, categories, top] = await Promise.all([
      dashboardService.getMovementsChart({ warehouseId: wid, days: 30 }),
      dashboardService.getStockByCategory({ warehouseId: wid }),
      dashboardService.getTopProducts({ warehouseId: wid, days: 30, limit: 5 }),
    ])
    movementsChart.value = movements
    categoryChart.value  = categories
    topProducts.value    = top
  } finally {
    loadingCharts.value = false
  }
}

onMounted(() => {
  if (isAdmin.value) {
    loadAdmin()
  } else if (warehouseId.value) {
    loadWarehouse(warehouseId.value)
  }
})
</script>
