<template>
  <div class="space-y-6">

    <!-- ── VUE MAGASINIER ────────────────────────────────────────── -->
    <template v-if="isStorekeeper">
      <div class="flex items-center justify-between">
        <PageHeader title="Mes rapports" subtitle="Rapports journaliers d'activité" />
        <BaseButton @click="showCreateModal = true">+ Nouveau rapport</BaseButton>
      </div>

      <!-- Liste de mes rapports -->
      <BaseCard>
        <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">
          Chargement…
        </div>
        <EmptyState
          v-else-if="reports.length === 0"
          title="Aucun rapport"
          description="Créez votre premier rapport journalier."
        />
        <div v-else class="space-y-3">
          <div
            v-for="r in reports"
            :key="r.id"
            class="rounded-xl border border-border bg-surface p-4"
          >
            <div class="flex items-start justify-between gap-3">
              <div>
                <div class="flex items-center gap-3">
                  <span class="font-semibold text-text-main">
                    {{ formatDate(r.reportDate) }}
                  </span>
                  <StatusBadge
                    :label="r.status === 'DRAFT' ? 'Brouillon' : 'Soumis'"
                    :variant="r.status === 'DRAFT' ? 'warning' : 'success'"
                  />
                </div>
                <div class="mt-1 flex gap-4 text-xs text-text-secondary">
                  <span v-if="r.arrivalTime">🕐 Arrivée : {{ r.arrivalTime }}</span>
                  <span v-if="r.departureTime">🕐 Départ : {{ r.departureTime }}</span>
                  <span>📥 {{ r.receptionCount }} réception(s)</span>
                  <span>📤 {{ r.dispatchCount }} sortie(s)</span>
                </div>
                <p v-if="r.incidents" class="mt-2 text-xs text-amber-600">
                  ⚠ Incident(s) signalé(s)
                </p>
              </div>
              <div class="flex flex-shrink-0 gap-2">
                <BaseButton size="sm" variant="secondary" @click="openDetail(r)">
                  Voir
                </BaseButton>
                <BaseButton
                  v-if="r.status === 'DRAFT'"
                  size="sm"
                  variant="secondary"
                  @click="editReport = r"
                >
                  Modifier
                </BaseButton>
                <BaseButton
                  v-if="r.status === 'DRAFT'"
                  size="sm"
                  @click="confirmSubmit(r)"
                >
                  Soumettre
                </BaseButton>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="mt-4 flex items-center justify-between">
          <span class="text-sm text-text-secondary">{{ totalElements }} rapport(s)</span>
          <div class="flex gap-2">
            <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetch()">
              Précédent
            </BaseButton>
            <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
            <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetch()">
              Suivant
            </BaseButton>
          </div>
        </div>
      </BaseCard>
    </template>

    <!-- ── VUE GESTIONNAIRE ───────────────────────────────────────── -->
    <template v-else>
      <PageHeader title="Rapports d'activité" subtitle="Rapports des magasiniers de l'entrepôt" />

      <!-- Filtres -->
      <BaseCard>
        <div class="flex flex-wrap gap-3">
          <!-- Filtre par magasinier -->
          <select
            v-model="filterStorekeeperId"
            @change="page = 0; fetch()"
            class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
          >
            <option :value="undefined">Tous les magasiniers</option>
            <option v-for="sk in storekeepers" :key="sk.id" :value="sk.id">
              {{ sk.storekeeperUsername }}
            </option>
          </select>

          <!-- Filtre statut -->
          <select
            v-model="filterStatus"
            @change="page = 0; fetch()"
            class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
          >
            <option :value="undefined">Tous les statuts</option>
            <option value="DRAFT">Brouillon</option>
            <option value="SUBMITTED">Soumis</option>
          </select>

          <!-- Filtre date de -->
          <input
            type="date"
            v-model="filterFrom"
            @change="page = 0; fetch()"
            class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
          />

          <!-- Filtre date à -->
          <input
            type="date"
            v-model="filterTo"
            @change="page = 0; fetch()"
            class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
          />
        </div>
      </BaseCard>

      <!-- Liste -->
      <BaseCard>
        <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">
          Chargement…
        </div>
        <EmptyState
          v-else-if="reports.length === 0"
          title="Aucun rapport"
          description="Aucun rapport d'activité pour les filtres sélectionnés."
        />
        <div v-else class="space-y-3">
          <div
            v-for="r in reports"
            :key="r.id"
            class="rounded-xl border border-border bg-surface p-4"
          >
            <div class="flex items-start justify-between gap-3">
              <div>
                <div class="flex items-center gap-3">
                  <span class="font-semibold text-text-main">
                    {{ formatDate(r.reportDate) }}
                  </span>
                  <span class="text-sm text-primary font-medium">
                    {{ r.storekeeperUsername }}
                  </span>
                  <StatusBadge
                    :label="r.status === 'DRAFT' ? 'Brouillon' : 'Soumis'"
                    :variant="r.status === 'DRAFT' ? 'warning' : 'success'"
                  />
                </div>
                <div class="mt-1 flex gap-4 text-xs text-text-secondary">
                  <span v-if="r.arrivalTime">🕐 {{ r.arrivalTime }} – {{ r.departureTime ?? '?' }}</span>
                  <span>📥 {{ r.receptionCount }} réception(s)</span>
                  <span>📤 {{ r.dispatchCount }} sortie(s)</span>
                </div>
                <p v-if="r.incidents" class="mt-1 text-xs text-amber-600">
                  ⚠ Incident(s) signalé(s)
                </p>
              </div>
              <BaseButton size="sm" variant="secondary" @click="openDetail(r)">
                Voir le détail
              </BaseButton>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="mt-4 flex items-center justify-between">
          <span class="text-sm text-text-secondary">{{ totalElements }} rapport(s)</span>
          <div class="flex gap-2">
            <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetch()">
              Précédent
            </BaseButton>
            <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
            <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetch()">
              Suivant
            </BaseButton>
          </div>
        </div>
      </BaseCard>
    </template>

    <!-- ── Modales ────────────────────────────────────────────────── -->
    <ActivityReportCreateModal
      v-if="showCreateModal"
      @close="showCreateModal = false"
      @saved="onSaved"
    />
    <ActivityReportEditModal
      v-if="editReport"
      :report="editReport"
      @close="editReport = null"
      @saved="onSaved"
    />
    <ActivityReportDetailModal
      v-if="detailReport"
      :report-id="detailReport.id"
      @close="detailReport = null"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { activityReportService } from '@/services/activityreport.service'
import type { ActivityReportResponse, ActivityReportStatus } from '@/types/activityreport.types'
import PageHeader    from '@/components/ui/PageHeader.vue'
import BaseCard      from '@/components/ui/BaseCard.vue'
import BaseButton    from '@/components/ui/BaseButton.vue'
import StatusBadge   from '@/components/ui/StatusBadge.vue'
import EmptyState    from '@/components/ui/EmptyState.vue'
import ActivityReportCreateModal from '@/components/activityreport/ActivityReportCreateModal.vue'
import ActivityReportEditModal   from '@/components/activityreport/ActivityReportEditModal.vue'
import ActivityReportDetailModal from '@/components/activityreport/ActivityReportDetailModal.vue'

const authStore = useAuthStore()
const toast     = useToastStore()

const isStorekeeper = computed(() => authStore.currentUser?.role === 'Magasinier')
const isManager     = computed(() => authStore.currentUser?.role === "Gestionnaire d'entrepôt")
const warehouseId   = computed(() => authStore.currentUser?.warehouseId!)

// ── État commun ───────────────────────────────────────────────
const reports       = ref<ActivityReportResponse[]>([])
const loading       = ref(false)
const page          = ref(0)
const totalPages    = ref(1)
const totalElements = ref(0)

// ── État modales ──────────────────────────────────────────────
const showCreateModal = ref(false)
const editReport      = ref<ActivityReportResponse | null>(null)
const detailReport    = ref<ActivityReportResponse | null>(null)

// ── Filtres gestionnaire ──────────────────────────────────────
const filterStorekeeperId = ref<number | undefined>(undefined)
const filterStatus        = ref<ActivityReportStatus | undefined>(undefined)
const filterFrom          = ref<string>('')
const filterTo            = ref<string>('')

// ── Liste des magasiniers pour le filtre (dédupliqué depuis les rapports) ──
const storekeepers = computed(() => {
  const seen = new Set<number>()
  return reports.value
    .filter((r) => {
      if (seen.has(r.storekeeperId)) return false
      seen.add(r.storekeeperId)
      return true
    })
    .map((r) => ({ id: r.storekeeperId, storekeeperUsername: r.storekeeperUsername }))
})

// ── Chargement ────────────────────────────────────────────────
async function fetch() {
  loading.value = true
  try {
    if (isStorekeeper.value) {
      const res = await activityReportService.listMine({ page: page.value, size: 20 })
      reports.value       = res.content
      totalPages.value    = res.totalPages
      totalElements.value = res.totalElements
    } else {
      const res = await activityReportService.listByWarehouse(warehouseId.value, {
        storekeeperId: filterStorekeeperId.value,
        status:        filterStatus.value,
        from:          filterFrom.value  || undefined,
        to:            filterTo.value    || undefined,
        page:          page.value,
        size:          20,
      })
      reports.value       = res.content
      totalPages.value    = res.totalPages
      totalElements.value = res.totalElements
    }
  } catch {
    toast.error('Impossible de charger les rapports')
  } finally {
    loading.value = false
  }
}

// ── Actions ───────────────────────────────────────────────────
function openDetail(r: ActivityReportResponse) {
  detailReport.value = r
}

async function confirmSubmit(r: ActivityReportResponse) {
  if (!confirm(`Soumettre le rapport du ${formatDate(r.reportDate)} ? Cette action est irréversible.`)) return
  try {
    await activityReportService.submit(r.id)
    toast.success('Rapport soumis')
    fetch()
  } catch {
    toast.error('Impossible de soumettre le rapport')
  }
}

function onSaved() {
  showCreateModal.value = false
  editReport.value      = null
  fetch()
}

// ── Helpers ───────────────────────────────────────────────────
function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('fr-FR', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  })
}

onMounted(fetch)
</script>
