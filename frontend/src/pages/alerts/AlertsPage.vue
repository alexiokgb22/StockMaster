<template>
  <div class="space-y-6">

    <!-- ── VUE ADMIN ─────────────────────────────────────────── -->
    <template v-if="isAdmin">

      <!-- Niveau 1 : liste des entrepôts — aucun entrepôt sélectionné -->
      <template v-if="!selectedWarehouse">
        <PageHeader
          title="Alertes"
          subtitle="Vue d'ensemble par entrepôt"
        />

        <div v-if="loadingWarehouses" class="py-12 text-center text-sm text-text-secondary">
          Chargement…
        </div>
        <div v-else-if="warehouseAlertSummaries.length === 0" class="py-12 text-center text-sm text-text-secondary">
          Aucun entrepôt actif.
        </div>
        <div v-else class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <button
            v-for="wh in warehouseAlertSummaries"
            :key="wh.id"
            @click="selectWarehouse(wh)"
            class="rounded-2xl border border-border bg-white p-5 text-left transition hover:border-primary hover:shadow-md"
          >
            <div class="flex items-center justify-between gap-3">
              <div class="min-w-0">
                <div class="truncate font-semibold text-text-main">{{ wh.name }}</div>
                <div class="mt-0.5 text-xs text-text-secondary">{{ wh.city ?? '—' }}</div>
              </div>
              <!-- Badge total alertes non résolues -->
              <span
                :class="[
                  'flex-shrink-0 rounded-full px-2.5 py-1 text-xs font-bold',
                  wh.criticalCount > 0
                    ? 'bg-red-100 text-red-600'
                    : wh.totalCount > 0
                      ? 'bg-amber-100 text-amber-600'
                      : 'bg-green-100 text-green-600',
                ]"
              >
                {{ wh.totalCount > 0 ? wh.totalCount + ' alerte(s)' : '✓ OK' }}
              </span>
            </div>

            <!-- Mini-résumé sévérités -->
            <div v-if="wh.totalCount > 0" class="mt-3 flex gap-2 text-xs">
              <span v-if="wh.criticalCount > 0" class="rounded-full bg-red-50 px-2 py-0.5 text-red-600">
                {{ wh.criticalCount }} critique(s)
              </span>
              <span v-if="wh.warningCount > 0" class="rounded-full bg-amber-50 px-2 py-0.5 text-amber-600">
                {{ wh.warningCount }} avertissement(s)
              </span>
              <span v-if="wh.unreadCount > 0" class="rounded-full bg-blue-50 px-2 py-0.5 text-blue-600">
                {{ wh.unreadCount }} non lue(s)
              </span>
            </div>
          </button>
        </div>
      </template>

      <!-- Niveau 2 : alertes d'un entrepôt sélectionné -->
      <template v-else>
        <div class="flex items-center gap-3">
          <button
            @click="selectedWarehouse = null; alerts = []"
            class="flex items-center gap-1 text-sm text-text-secondary transition hover:text-primary"
          >
            ← Retour
          </button>
          <PageHeader
            :title="`Alertes — ${selectedWarehouse.name}`"
            subtitle="Alertes de cet entrepôt"
          />
        </div>

        <!-- Filtres + actions -->
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div class="flex flex-wrap gap-3">
            <select
              v-model="filters.severity"
              @change="page = 0; fetchAlerts()"
              class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
            >
              <option value="">Toutes les sévérités</option>
              <option value="CRITICAL">Critique</option>
              <option value="WARNING">Avertissement</option>
              <option value="INFO">Info</option>
            </select>
            <select
              v-model="filters.type"
              @change="page = 0; fetchAlerts()"
              class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
            >
              <option value="">Tous les types</option>
              <option value="STOCK_OUT">Rupture de stock</option>
              <option value="STOCK_BELOW_MIN">Stock sous seuil</option>
              <option value="ZONE_NEAR_CAPACITY">Zone proche saturation</option>
              <option value="WAREHOUSE_NEAR_CAPACITY">Entrepôt proche saturation</option>
            </select>
            <select
              v-model="filters.isResolved"
              @change="page = 0; fetchAlerts()"
              class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
            >
              <option :value="undefined">Toutes</option>
              <option :value="false">Non résolues</option>
              <option :value="true">Résolues</option>
            </select>
          </div>
          <BaseButton
            v-if="unreadCount > 0"
            variant="secondary"
            size="sm"
            @click="markAllRead"
          >
            Tout marquer comme lu
          </BaseButton>
        </div>

        <AlertList
          :alerts="alerts"
          :loading="loading"
          :page="page"
          :total-pages="totalPages"
          :total-elements="totalElements"
          @prev="page--; fetchAlerts()"
          @next="page++; fetchAlerts()"
          @mark-read="markRead"
          @resolve="resolveAlert"
        />
      </template>
    </template>

    <!-- ── VUE GESTIONNAIRE ───────────────────────────────────── -->
    <template v-else>
      <div class="flex items-center justify-between">
        <PageHeader title="Alertes" subtitle="Alertes de votre entrepôt" />
        <BaseButton
          v-if="unreadCount > 0"
          variant="secondary"
          size="sm"
          @click="markAllRead"
        >
          Tout marquer comme lu
        </BaseButton>
      </div>

      <!-- Filtres -->
      <BaseCard>
        <div class="flex flex-wrap gap-3">
          <select
            v-model="filters.severity"
            @change="page = 0; fetchAlerts()"
            class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
          >
            <option value="">Toutes les sévérités</option>
            <option value="CRITICAL">Critique</option>
            <option value="WARNING">Avertissement</option>
            <option value="INFO">Info</option>
          </select>
          <select
            v-model="filters.type"
            @change="page = 0; fetchAlerts()"
            class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
          >
            <option value="">Tous les types</option>
            <option value="STOCK_OUT">Rupture de stock</option>
            <option value="STOCK_BELOW_MIN">Stock sous seuil</option>
            <option value="ZONE_NEAR_CAPACITY">Zone proche saturation</option>
            <option value="WAREHOUSE_NEAR_CAPACITY">Entrepôt proche saturation</option>
          </select>
          <select
            v-model="filters.isResolved"
            @change="page = 0; fetchAlerts()"
            class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
          >
            <option :value="undefined">Toutes</option>
            <option :value="false">Non résolues</option>
            <option :value="true">Résolues</option>
          </select>
        </div>
      </BaseCard>

      <AlertList
        :alerts="alerts"
        :loading="loading"
        :page="page"
        :total-pages="totalPages"
        :total-elements="totalElements"
        @prev="page--; fetchAlerts()"
        @next="page++; fetchAlerts()"
        @mark-read="markRead"
        @resolve="resolveAlert"
      />
    </template>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { alertService } from '@/services/alert.service'
import { warehouseService } from '@/services/warehouse.service'
import type { AlertResponse, AlertType, AlertSeverity } from '@/types/alert.types'
import type { WarehouseResponse } from '@/types/warehouse.types'
import PageHeader  from '@/components/ui/PageHeader.vue'
import BaseCard    from '@/components/ui/BaseCard.vue'
import BaseButton  from '@/components/ui/BaseButton.vue'
import AlertList   from '@/components/alert/AlertList.vue'

const authStore = useAuthStore()
const toast     = useToastStore()

const isAdmin     = computed(() => authStore.currentUser?.role === 'Administrateur')
const warehouseId = computed(() => authStore.currentUser?.warehouseId ?? undefined)

// ── État commun ───────────────────────────────────────────────
const alerts       = ref<AlertResponse[]>([])
const unreadCount  = ref(0)
const loading      = ref(false)
const page         = ref(0)
const totalPages   = ref(1)
const totalElements = ref(0)

const filters = ref<{
  severity:   AlertSeverity | ''
  type:       AlertType | ''
  isResolved: boolean | undefined
}>({
  severity:   '',
  type:       '',
  isResolved: false,
})

// ── État admin — sélection entrepôt ──────────────────────────
interface WarehouseAlertSummary {
  id:            number
  name:          string
  city:          string | null
  totalCount:    number
  criticalCount: number
  warningCount:  number
  unreadCount:   number
}

const loadingWarehouses      = ref(false)
const warehouseAlertSummaries = ref<WarehouseAlertSummary[]>([])
const selectedWarehouse       = ref<WarehouseAlertSummary | null>(null)

// ── Chargement liste entrepôts avec résumé alertes (admin) ───
async function loadWarehouseSummaries() {
  loadingWarehouses.value = true
  try {
    // Récupérer tous les entrepôts actifs
    const whRes = await warehouseService.list({ active: true, size: 100 })
    const warehouses = whRes.content

    // Pour chaque entrepôt, récupérer le comptage d'alertes non résolues
    const summaries = await Promise.all(
      warehouses.map(async (wh: WarehouseResponse) => {
        const [total, critical, warning, unread] = await Promise.all([
          alertService.list({ warehouseId: wh.id, isResolved: false, size: 1 }),
          alertService.list({ warehouseId: wh.id, isResolved: false, severity: 'CRITICAL', size: 1 }),
          alertService.list({ warehouseId: wh.id, isResolved: false, severity: 'WARNING',  size: 1 }),
          alertService.list({ warehouseId: wh.id, isResolved: false, isRead: false,        size: 1 }),
        ])
        return {
          id:            wh.id,
          name:          wh.name,
          city:          wh.city ?? null,
          totalCount:    total.totalElements,
          criticalCount: critical.totalElements,
          warningCount:  warning.totalElements,
          unreadCount:   unread.totalElements,
        }
      })
    )

    warehouseAlertSummaries.value = summaries
  } catch {
    toast.error('Impossible de charger les entrepôts')
  } finally {
    loadingWarehouses.value = false
  }
}

function selectWarehouse(wh: WarehouseAlertSummary) {
  selectedWarehouse.value = wh
  page.value = 0
  alerts.value = []
  filters.value = { severity: '', type: '', isResolved: false }
  fetchAlerts()
  fetchUnreadCount()
}

// ── Chargement alertes ────────────────────────────────────────
async function fetchAlerts() {
  loading.value = true
  try {
    const wid = isAdmin.value
      ? selectedWarehouse.value?.id
      : warehouseId.value

    const res = await alertService.list({
      warehouseId: wid,
      severity:    filters.value.severity   || undefined,
      type:        filters.value.type       || undefined,
      isResolved:  filters.value.isResolved,
      page:        page.value,
      size:        20,
    })
    alerts.value        = res.content
    totalPages.value    = res.totalPages
    totalElements.value = res.totalElements
  } catch {
    toast.error('Impossible de charger les alertes')
  } finally {
    loading.value = false
  }
}

async function fetchUnreadCount() {
  try {
    const wid = isAdmin.value
      ? selectedWarehouse.value?.id
      : warehouseId.value
    const res = await alertService.countUnread({ warehouseId: wid })
    unreadCount.value = res.count
  } catch { /* silencieux */ }
}

// ── Actions ───────────────────────────────────────────────────
async function markRead(alert: AlertResponse) {
  try {
    await alertService.markAsRead(alert.id)
    alert.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    // Rafraîchir le compteur du résumé
    if (isAdmin.value && selectedWarehouse.value) {
      const wh = warehouseAlertSummaries.value.find(w => w.id === selectedWarehouse.value!.id)
      if (wh) wh.unreadCount = Math.max(0, wh.unreadCount - 1)
    }
  } catch {
    toast.error("Impossible de marquer l'alerte")
  }
}

async function resolveAlert(alert: AlertResponse) {
  try {
    await alertService.resolve(alert.id)
    alert.isResolved = true
    alert.isRead = true
    toast.success('Alerte résolue')
    fetchUnreadCount()
    // Rafraîchir le résumé de l'entrepôt
    if (isAdmin.value && selectedWarehouse.value) {
      const wh = warehouseAlertSummaries.value.find(w => w.id === selectedWarehouse.value!.id)
      if (wh) { wh.totalCount = Math.max(0, wh.totalCount - 1) }
    }
  } catch {
    toast.error("Impossible de résoudre l'alerte")
  }
}

async function markAllRead() {
  try {
    const wid = isAdmin.value
      ? selectedWarehouse.value?.id
      : warehouseId.value
    await alertService.markAllAsRead({ warehouseId: wid })
    toast.success('Toutes les alertes marquées comme lues')
    unreadCount.value = 0
    fetchAlerts()
    if (isAdmin.value && selectedWarehouse.value) {
      const wh = warehouseAlertSummaries.value.find(w => w.id === selectedWarehouse.value!.id)
      if (wh) wh.unreadCount = 0
    }
  } catch {
    toast.error('Impossible de marquer les alertes')
  }
}

// ── Init ──────────────────────────────────────────────────────
onMounted(() => {
  if (isAdmin.value) {
    loadWarehouseSummaries()
  } else {
    fetchAlerts()
    fetchUnreadCount()
  }
})
</script>
