<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <PageHeader title="Alertes" subtitle="Surveillance des seuils et de la capacité" />
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
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option value="">Toutes les sévérités</option>
          <option value="CRITICAL">Critique</option>
          <option value="WARNING">Avertissement</option>
          <option value="INFO">Info</option>
        </select>

        <select
          v-model="filters.type"
          @change="page = 0; fetchAlerts()"
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
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
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option :value="undefined">Toutes</option>
          <option :value="false">Non résolues</option>
          <option :value="true">Résolues</option>
        </select>
      </div>
    </BaseCard>

    <!-- Liste -->
    <BaseCard>
      <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">
        Chargement…
      </div>
      <EmptyState
        v-else-if="alerts.length === 0"
        title="Aucune alerte"
        description="Tout est sous contrôle. Aucune alerte ne correspond aux filtres sélectionnés."
      />
      <div v-else class="space-y-3">
        <div
          v-for="alert in alerts"
          :key="alert.id"
          :class="[
            'rounded-xl border p-4 transition',
            alert.isRead ? 'border-border bg-surface' : 'border-l-4 bg-white',
            !alert.isRead && alert.severity === 'CRITICAL' ? 'border-l-red-500' : '',
            !alert.isRead && alert.severity === 'WARNING'  ? 'border-l-amber-500' : '',
            !alert.isRead && alert.severity === 'INFO'     ? 'border-l-blue-400' : '',
          ]"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-center gap-3">
              <!-- Icône sévérité -->
              <span
                :class="[
                  'inline-flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full text-sm font-bold',
                  alert.severity === 'CRITICAL' ? 'bg-red-100 text-red-600' : '',
                  alert.severity === 'WARNING'  ? 'bg-amber-100 text-amber-600' : '',
                  alert.severity === 'INFO'     ? 'bg-blue-100 text-blue-500' : '',
                ]"
              >
                {{ alert.severity === 'CRITICAL' ? '!' : alert.severity === 'WARNING' ? '⚠' : 'i' }}
              </span>
              <div>
                <div class="flex items-center gap-2">
                  <span class="text-sm font-medium text-text-main">{{ alert.message }}</span>
                  <span
                    v-if="!alert.isRead"
                    class="inline-block h-2 w-2 rounded-full bg-primary"
                  />
                </div>
                <div class="mt-0.5 text-xs text-text-secondary">
                  {{ alert.warehouseName }}
                  <span v-if="alert.zoneName"> · {{ alert.zoneName }}</span>
                  · {{ formatDate(alert.createdAt) }}
                </div>
              </div>
            </div>

            <div class="flex flex-shrink-0 gap-2">
              <BaseButton
                v-if="!alert.isRead"
                size="sm"
                variant="secondary"
                @click="markRead(alert)"
              >
                Lu
              </BaseButton>
              <BaseButton
                v-if="!alert.isResolved"
                size="sm"
                variant="secondary"
                @click="resolveAlert(alert)"
              >
                Résoudre
              </BaseButton>
              <span
                v-if="alert.isResolved"
                class="text-xs text-green-600 font-medium py-1 px-2"
              >
                ✓ Résolue
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="mt-4 flex items-center justify-between">
        <span class="text-sm text-text-secondary">{{ totalElements }} alerte(s)</span>
        <div class="flex gap-2">
          <BaseButton
            size="sm"
            variant="secondary"
            :disabled="page === 0"
            @click="page--; fetchAlerts()"
          >
            Précédent
          </BaseButton>
          <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
          <BaseButton
            size="sm"
            variant="secondary"
            :disabled="page + 1 >= totalPages"
            @click="page++; fetchAlerts()"
          >
            Suivant
          </BaseButton>
        </div>
      </div>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { alertService } from '@/services/alert.service'
import type { AlertResponse, AlertType, AlertSeverity } from '@/types/alert.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const authStore = useAuthStore()
const toast = useToastStore()

const isAdmin = computed(() => authStore.currentUser?.role === 'Administrateur')
const warehouseId = computed(() => authStore.currentUser?.warehouseId ?? undefined)

// ── État ─────────────────────────────────────────────────────
const alerts = ref<AlertResponse[]>([])
const unreadCount = ref(0)
const loading = ref(false)
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)

const filters = ref<{
  severity: AlertSeverity | ''
  type: AlertType | ''
  isResolved: boolean | undefined
}>({
  severity: '',
  type: '',
  isResolved: false, // par défaut : non résolues uniquement
})

// ── Chargements ───────────────────────────────────────────────
async function fetchAlerts() {
  loading.value = true
  try {
    const res = await alertService.list({
      warehouseId: isAdmin.value ? undefined : warehouseId.value,
      severity:    filters.value.severity   || undefined,
      type:        filters.value.type       || undefined,
      isResolved:  filters.value.isResolved,
      page:        page.value,
      size:        20,
    })
    alerts.value = res.content
    totalPages.value = res.totalPages
    totalElements.value = res.totalElements
  } catch {
    toast.error('Impossible de charger les alertes')
  } finally {
    loading.value = false
  }
}

async function fetchUnreadCount() {
  try {
    const res = await alertService.countUnread({
      warehouseId: isAdmin.value ? undefined : warehouseId.value,
    })
    unreadCount.value = res.count
  } catch { /* silencieux */ }
}

// ── Actions ───────────────────────────────────────────────────
async function markRead(alert: AlertResponse) {
  try {
    await alertService.markAsRead(alert.id)
    alert.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch {
    toast.error('Impossible de marquer l\'alerte')
  }
}

async function resolveAlert(alert: AlertResponse) {
  try {
    await alertService.resolve(alert.id)
    alert.isResolved = true
    alert.isRead = true
    toast.success('Alerte résolue')
    fetchUnreadCount()
  } catch {
    toast.error('Impossible de résoudre l\'alerte')
  }
}

async function markAllRead() {
  try {
    await alertService.markAllAsRead({
      warehouseId: isAdmin.value ? undefined : warehouseId.value,
    })
    toast.success('Toutes les alertes marquées comme lues')
    unreadCount.value = 0
    fetchAlerts()
  } catch {
    toast.error('Impossible de marquer les alertes')
  }
}

// ── Helpers ───────────────────────────────────────────────────
function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  fetchAlerts()
  fetchUnreadCount()
})
</script>
