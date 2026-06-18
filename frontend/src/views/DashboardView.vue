<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  Warehouse, Package, TrendingUp, AlertTriangle, ArrowDownToLine,
  ArrowUpFromLine, ClipboardList, ShoppingCart, ArrowLeftRight,
  RefreshCw, ChevronRight, Clock, CheckCircle, XCircle, Activity
} from '@lucide/vue'
import MetricCard from '@/components/ui/MetricCard.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import SkeletonLoader from '@/components/ui/SkeletonLoader.vue'

const auth    = useAuthStore()
const loading = ref(false)

// ── KPIs ──────────────────────────────────────────────────────────────
const kpis = computed(() => [
  { title: 'Entrepôts Actifs',   value: '12',     icon: Warehouse,        trend: 'neutral' as const, trendValue: '0',    color: 'primary' as const },
  { title: 'Produits en Stock',  value: '3 247',  icon: Package,          trend: 'up'      as const, trendValue: '+8.2%',color: 'info'    as const },
  { title: 'Valeur Totale',      value: '€2.4M',  icon: TrendingUp,       trend: 'up'      as const, trendValue: '+12.5%',color: 'success' as const },
  { title: 'Alertes Critiques',  value: '5',      icon: AlertTriangle,    trend: 'down'    as const, trendValue: '-3',   color: 'error'   as const },
  { title: 'Réceptions / mois',  value: '148',    icon: ArrowDownToLine,  trend: 'up'      as const, trendValue: '+5%',  color: 'success' as const },
  { title: 'Sorties / mois',     value: '231',    icon: ArrowUpFromLine,  trend: 'up'      as const, trendValue: '+11%', color: 'warning' as const },
  { title: 'Inventaires actifs', value: '3',      icon: ClipboardList,    trend: 'neutral' as const, trendValue: '',     color: 'primary' as const },
  { title: 'Cmdes en attente',   value: '9',      icon: ShoppingCart,     trend: 'down'    as const, trendValue: '-2',   color: 'warning' as const },
])

// ── Graphique évolution stocks ─────────────────────────────────────────
const stockChartOptions = ref({
  chart: { type: 'area', height: 220, toolbar: { show: false }, sparkline: { enabled: false }, background: 'transparent' },
  dataLabels: { enabled: false },
  stroke:     { curve: 'smooth', width: 2 },
  fill:       { type: 'gradient', gradient: { shadeIntensity: 1, opacityFrom: 0.25, opacityTo: 0.02 } },
  colors:     ['#0B3563', '#F8D830'],
  xaxis:      { categories: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc'], labels: { style: { colors: '#64748b', fontSize: '11px' } }, axisBorder: { show: false }, axisTicks: { show: false } },
  yaxis:      { labels: { style: { colors: '#64748b', fontSize: '11px' } } },
  grid:       { borderColor: '#e8edf4', strokeDashArray: 3 },
  legend:     { position: 'top', horizontalAlign: 'right', fontSize: '12px', labels: { colors: '#0B3563' } },
  tooltip:    { theme: 'light' },
})
const stockSeries = ref([
  { name: 'Entrées',  data: [420, 380, 510, 470, 540, 490, 620, 580, 610, 720, 680, 740] },
  { name: 'Sorties',  data: [350, 290, 410, 380, 430, 420, 530, 490, 520, 610, 570, 640] },
])

// ── Graphique répartition entrepôts ───────────────────────────────────
const warehouseChartOptions = ref({
  chart:   { type: 'donut', height: 200, background: 'transparent' },
  labels:  ['Paris Nord', 'Lyon Est', 'Marseille', 'Bordeaux', 'Autres'],
  colors:  ['#0B3563', '#F8D830', '#10b981', '#3b82f6', '#94a3b8'],
  legend:  { position: 'bottom', fontSize: '11px', labels: { colors: '#0B3563' } },
  dataLabels: { enabled: false },
  plotOptions: { pie: { donut: { size: '65%', labels: { show: true, total: { show: true, label: 'Total', fontSize: '12px', color: '#64748b', formatter: () => '3 247' } } } } },
  stroke:  { width: 0 },
  tooltip: { theme: 'light' },
})
const warehouseSeries = ref([1240, 680, 520, 380, 427])

// ── Graphique flux barres ──────────────────────────────────────────────
const fluxChartOptions = ref({
  chart:       { type: 'bar', height: 180, toolbar: { show: false }, background: 'transparent' },
  plotOptions: { bar: { horizontal: false, columnWidth: '50%', borderRadius: 4 } },
  dataLabels:  { enabled: false },
  colors:      ['#10b981', '#ef4444'],
  xaxis:       { categories: ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'], labels: { style: { colors: '#64748b', fontSize: '11px' } }, axisBorder: { show: false } },
  yaxis:       { labels: { style: { colors: '#64748b', fontSize: '11px' } } },
  grid:        { borderColor: '#e8edf4', strokeDashArray: 3 },
  legend:      { position: 'top', fontSize: '12px', labels: { colors: '#0B3563' } },
  tooltip:     { theme: 'light' },
})
const fluxSeries = ref([
  { name: 'Entrées', data: [44, 55, 57, 56, 61, 28] },
  { name: 'Sorties', data: [35, 41, 36, 26, 45, 12] },
])

// ── Alertes critiques ──────────────────────────────────────────────────
const alerts = ref([
  { id: 1, type: 'error',   product: 'Câble USB-C 2m', ref: 'REF-0234', warehouse: 'Paris Nord', stock: 3,  min: 50,  message: 'Stock critique' },
  { id: 2, type: 'warning', product: 'Boîtier PC Tour', ref: 'REF-1102', warehouse: 'Lyon Est',   stock: 12, min: 20,  message: 'Stock bas' },
  { id: 3, type: 'error',   product: 'Disque SSD 1To',  ref: 'REF-0891', warehouse: 'Marseille',  stock: 0,  min: 10,  message: 'Rupture de stock' },
  { id: 4, type: 'warning', product: 'RAM DDR5 16Go',   ref: 'REF-0445', warehouse: 'Paris Nord', stock: 8,  min: 15,  message: 'Stock bas' },
])

// ── Activités récentes ─────────────────────────────────────────────────
const activities = ref([
  { id: 1, icon: ArrowDownToLine, color: 'text-success', bg: 'bg-success-light', action: 'Réception validée',  detail: 'CMD-2024-0312 — 45 articles',     warehouse: 'Paris Nord', time: 'Il y a 5 min',  status: 'received' as const },
  { id: 2, icon: ArrowLeftRight,  color: 'text-info',    bg: 'bg-info-light',    action: 'Transfert expédié',  detail: 'TRF-2024-0089 — 120 unités',      warehouse: '→ Lyon Est',  time: 'Il y a 18 min', status: 'shipped'  as const },
  { id: 3, icon: ClipboardList,   color: 'text-warning', bg: 'bg-warning-light', action: 'Inventaire démarré', detail: 'INV-2024-014 — Zone B',            warehouse: 'Marseille',  time: 'Il y a 1h',    status: 'pending'  as const },
  { id: 4, icon: ArrowUpFromLine, color: 'text-primary', bg: 'bg-primary-100',   action: 'Sortie créée',       detail: 'SOR-2024-0456 — Commande client',  warehouse: 'Bordeaux',   time: 'Il y a 2h',    status: 'draft'    as const },
  { id: 5, icon: CheckCircle,     color: 'text-success', bg: 'bg-success-light', action: 'Commande livrée',    detail: 'CMD-2024-0298 — Fournisseur Tech', warehouse: 'Lyon Est',   time: 'Il y a 3h',    status: 'received' as const },
])

// ── Transferts en transit ──────────────────────────────────────────────
const transfers = ref([
  { id: 'TRF-089', from: 'Paris', to: 'Lyon',      qty: 120, progress: 65, status: 'shipped'  as const },
  { id: 'TRF-087', from: 'Lyon',  to: 'Marseille', qty: 80,  progress: 30, status: 'shipped'  as const },
  { id: 'TRF-085', from: 'Paris', to: 'Bordeaux',  qty: 200, progress: 90, status: 'shipped'  as const },
])

// ── Taux d'occupation entrepôts ────────────────────────────────────────
const warehouseOccupancy = ref([
  { name: 'Paris Nord',  used: 87, capacity: 5000, status: 'error'   as const },
  { name: 'Lyon Est',    used: 62, capacity: 3000, status: 'success' as const },
  { name: 'Marseille',   used: 74, capacity: 4000, status: 'warning' as const },
  { name: 'Bordeaux',    used: 45, capacity: 2500, status: 'success' as const },
])

function occupancyColor(pct: number) {
  if (pct >= 85) return '#ef4444'
  if (pct >= 70) return '#f59e0b'
  return '#10b981'
}
</script>

<template>
  <div class="space-y-6 animate-fade-in">

    <!-- ── Header ── -->
    <PageHeader
      title="Centre de Contrôle"
      :subtitle="`Bienvenue, ${auth.user?.username} · ${new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })}`"
    >
      <template #actions>
        <Button variant="secondary" size="sm">
          <RefreshCw class="w-4 h-4" />
          Actualiser
        </Button>
        <Button variant="accent" size="sm">
          <Activity class="w-4 h-4" />
          Vue temps réel
        </Button>
      </template>
    </PageHeader>

    <!-- ── KPIs 8 colonnes ── -->
    <div class="grid grid-cols-2 sm:grid-cols-4 xl:grid-cols-8 gap-4">
      <MetricCard
        v-for="kpi in kpis"
        :key="kpi.title"
        :title="kpi.title"
        :value="kpi.value"
        :icon="kpi.icon"
        :trend="kpi.trend"
        :trend-value="kpi.trendValue"
        :color="kpi.color"
        :loading="loading"
        class="xl:col-span-1 col-span-1"
      />
    </div>

    <!-- ── Graphiques ligne 1 ── -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">

      <!-- Évolution des stocks -->
      <BaseCard class="lg:col-span-2" padding="md">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h2 class="heading-4">Évolution des Flux</h2>
            <p class="text-caption mt-0.5">Entrées et sorties sur 12 mois</p>
          </div>
          <Badge variant="info" size="sm" dot>En direct</Badge>
        </div>
        <apexchart type="area" height="220" :options="stockChartOptions" :series="stockSeries" />
      </BaseCard>

      <!-- Répartition entrepôts -->
      <BaseCard padding="md">
        <div class="mb-2">
          <h2 class="heading-4">Stock par Entrepôt</h2>
          <p class="text-caption mt-0.5">Répartition des unités</p>
        </div>
        <apexchart type="donut" height="200" :options="warehouseChartOptions" :series="warehouseSeries" />
      </BaseCard>
    </div>

    <!-- ── Contenu ligne 2 ── -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">

      <!-- Activités récentes -->
      <BaseCard class="lg:col-span-2" padding="none">
        <div class="flex items-center justify-between px-5 py-4 border-b border-border-light">
          <h2 class="heading-4">Activités Récentes</h2>
          <button class="text-sm text-primary font-semibold hover:underline flex items-center gap-1">
            Voir tout <ChevronRight class="w-4 h-4" />
          </button>
        </div>
        <ul class="divide-y divide-border-light">
          <li
            v-for="act in activities"
            :key="act.id"
            class="flex items-center gap-3 px-5 py-3.5 hover:bg-background transition-fast"
          >
            <div :class="['w-9 h-9 rounded-xl flex items-center justify-center shrink-0', act.bg]">
              <component :is="act.icon" :class="['w-4.5 h-4.5', act.color]" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold text-text-main">{{ act.action }}</p>
              <p class="text-xs text-text-muted truncate">{{ act.detail }} · {{ act.warehouse }}</p>
            </div>
            <div class="text-right shrink-0">
              <Badge :variant="act.status" size="sm" dot>{{ act.status }}</Badge>
              <p class="text-xs text-text-subtle mt-1">{{ act.time }}</p>
            </div>
          </li>
        </ul>
      </BaseCard>

      <!-- Alertes critiques -->
      <BaseCard padding="none">
        <div class="flex items-center justify-between px-5 py-4 border-b border-border-light">
          <div class="flex items-center gap-2">
            <AlertTriangle class="w-4 h-4 text-error" />
            <h2 class="heading-4">Alertes Stock</h2>
          </div>
          <Badge variant="critical" size="sm">{{ alerts.length }}</Badge>
        </div>
        <ul class="divide-y divide-border-light">
          <li
            v-for="alert in alerts"
            :key="alert.id"
            class="px-5 py-3.5 hover:bg-background transition-fast"
          >
            <div class="flex items-start justify-between gap-2">
              <div class="flex-1 min-w-0">
                <p class="text-sm font-semibold text-text-main truncate">{{ alert.product }}</p>
                <p class="text-xs text-text-muted">{{ alert.ref }} · {{ alert.warehouse }}</p>
                <div class="flex items-center gap-2 mt-1.5">
                  <div class="flex-1 h-1.5 bg-border-light rounded-full overflow-hidden">
                    <div
                      class="h-full rounded-full transition-base"
                      :style="{ width: `${(alert.stock / alert.min) * 100}%`, background: alert.type === 'error' ? '#ef4444' : '#f59e0b' }"
                    />
                  </div>
                  <span class="text-xs font-bold" :class="alert.type === 'error' ? 'text-error' : 'text-warning'">
                    {{ alert.stock }}/{{ alert.min }}
                  </span>
                </div>
              </div>
              <Badge :variant="alert.type === 'error' ? 'critical' : 'warning'" size="sm">
                {{ alert.message }}
              </Badge>
            </div>
          </li>
        </ul>
        <div class="px-5 py-3 border-t border-border-light">
          <router-link to="/stocks" class="text-sm text-primary font-semibold hover:underline flex items-center gap-1">
            Gérer les stocks <ChevronRight class="w-4 h-4" />
          </router-link>
        </div>
      </BaseCard>
    </div>

    <!-- ── Ligne 3 ── -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">

      <!-- Flux de la semaine -->
      <BaseCard padding="md">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h2 class="heading-4">Flux de la Semaine</h2>
            <p class="text-caption mt-0.5">Entrées vs Sorties</p>
          </div>
        </div>
        <apexchart type="bar" height="180" :options="fluxChartOptions" :series="fluxSeries" />
      </BaseCard>

      <!-- Occupation entrepôts -->
      <BaseCard padding="none">
        <div class="px-5 py-4 border-b border-border-light">
          <h2 class="heading-4">Taux d'Occupation</h2>
          <p class="text-caption mt-0.5">Capacité utilisée par entrepôt</p>
        </div>
        <ul class="divide-y divide-border-light">
          <li
            v-for="wh in warehouseOccupancy"
            :key="wh.name"
            class="px-5 py-4 hover:bg-background transition-fast"
          >
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <Warehouse class="w-4 h-4 text-text-muted" />
                <span class="text-sm font-semibold text-text-main">{{ wh.name }}</span>
              </div>
              <span class="text-sm font-bold" :style="{ color: occupancyColor(wh.used) }">
                {{ wh.used }}%
              </span>
            </div>
            <div class="h-2 bg-border-light rounded-full overflow-hidden">
              <div
                class="h-full rounded-full transition-slow"
                :style="{ width: `${wh.used}%`, background: occupancyColor(wh.used) }"
              />
            </div>
            <p class="text-xs text-text-muted mt-1">
              {{ Math.round(wh.capacity * wh.used / 100).toLocaleString() }} / {{ wh.capacity.toLocaleString() }} emplacements
            </p>
          </li>
        </ul>
      </BaseCard>
    </div>

    <!-- ── Transferts en transit ── -->
    <BaseCard padding="none">
      <div class="flex items-center justify-between px-5 py-4 border-b border-border-light">
        <div class="flex items-center gap-2">
          <ArrowLeftRight class="w-4 h-4 text-primary" />
          <h2 class="heading-4">Transferts en Transit</h2>
        </div>
        <router-link to="/transfers" class="text-sm text-primary font-semibold hover:underline flex items-center gap-1">
          Voir tout <ChevronRight class="w-4 h-4" />
        </router-link>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-3 divide-y sm:divide-y-0 sm:divide-x divide-border-light">
        <div v-for="tr in transfers" :key="tr.id" class="px-5 py-4">
          <div class="flex items-center justify-between mb-1.5">
            <span class="text-sm font-bold text-text-main">{{ tr.id }}</span>
            <Badge variant="shipped" size="sm" dot>En transit</Badge>
          </div>
          <p class="text-xs text-text-muted mb-3">
            <span class="font-medium">{{ tr.from }}</span>
            <span class="mx-1.5 text-text-subtle">→</span>
            <span class="font-medium">{{ tr.to }}</span>
            <span class="ml-2 text-text-subtle">· {{ tr.qty }} unités</span>
          </p>
          <div class="h-2 bg-border-light rounded-full overflow-hidden">
            <div
              class="h-full rounded-full bg-primary transition-slow"
              :style="{ width: `${tr.progress}%` }"
            />
          </div>
          <div class="flex justify-between mt-1">
            <span class="text-caption">Progression</span>
            <span class="text-caption font-semibold text-primary">{{ tr.progress }}%</span>
          </div>
        </div>
      </div>
    </BaseCard>

  </div>
</template>

<style scoped>
.w-4\.5 { width: 1.125rem; }
.h-4\.5 { height: 1.125rem; }
</style>
