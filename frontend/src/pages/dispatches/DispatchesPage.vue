<template>
  <div class="space-y-6">
    <PageHeader title="Sorties" subtitle="Bons de sortie de votre entrepôt" />

    <!-- Onglets -->
    <div class="flex gap-2 border-b border-border">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        @click="activeTab = tab.key"
        :class="[
          'px-4 py-2 text-sm font-medium transition border-b-2 -mb-px',
          activeTab === tab.key
            ? 'border-primary text-primary'
            : 'border-transparent text-text-secondary hover:text-text-main',
        ]"
      >
        {{ tab.label }}
        <span
          v-if="tab.key === 'pending' && pendingCount > 0"
          class="ml-1 rounded-full bg-red-500 px-1.5 py-0.5 text-xs text-white"
        >
          {{ pendingCount }}
        </span>
      </button>
    </div>

    <!-- Bouton créer (magasinier) -->
    <div v-if="isStorekeeper" class="flex justify-end">
      <BaseButton @click="showCreateModal = true">+ Nouveau bon de sortie</BaseButton>
    </div>

    <!-- Filtre statut -->
    <BaseCard v-if="activeTab !== 'pending'">
      <select
        v-model="statusFilter"
        @change="fetchDispatches"
        class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
      >
        <option value="">Tous les statuts</option>
        <option value="PENDING">En attente</option>
        <option value="VALIDATED">Validé</option>
        <option value="REJECTED">Rejeté</option>
      </select>
    </BaseCard>

    <!-- Liste des bons -->
    <BaseCard>
      <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">Chargement…</div>
      <EmptyState
        v-else-if="dispatches.length === 0"
        title="Aucun bon de sortie"
        description="Les bons de sortie apparaîtront ici."
      />
      <div v-else class="space-y-3">
        <div
          v-for="d in dispatches"
          :key="d.id"
          class="rounded-xl border border-border bg-surface p-4"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">
                {{ d.dispatchNumber }}
              </code>
              <StatusBadge :label="statusLabel(d.status)" :variant="statusVariant(d.status)" />
            </div>
            <span class="text-sm text-text-secondary">
              {{ new Date(d.createdAt).toLocaleDateString('fr-FR') }}
            </span>
          </div>

          <div class="mt-2 text-sm text-text-secondary">
            {{ d.lines.length }} produit(s) · Par {{ d.createdByUsername }}
            <span v-if="d.note" class="ml-2 italic">— {{ d.note }}</span>
          </div>

          <div class="mt-3 flex gap-2">
            <BaseButton
              v-if="d.status === 'PENDING' && isManager"
              size="sm"
              @click="dispatchToValidate = d"
            >
              Valider / Rejeter
            </BaseButton>
            <BaseButton
              v-if="can('dispatch.print_bordereau')"
              size="sm"
              variant="secondary"
              @click="printBordereau(d.id)"
            >
              Imprimer bordereau
            </BaseButton>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="mt-4 flex items-center justify-between">
        <span class="text-sm text-text-secondary">{{ totalElements }} bon(s)</span>
        <div class="flex gap-2">
          <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetchDispatches()">
            Précédent
          </BaseButton>
          <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
          <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetchDispatches()">
            Suivant
          </BaseButton>
        </div>
      </div>
    </BaseCard>

    <!-- Modales -->
    <DispatchCreateModal
      v-if="showCreateModal"
      :warehouse-id="warehouseId"
      @close="showCreateModal = false"
      @saved="onSaved"
    />
    <DispatchValidateModal
      v-if="dispatchToValidate"
      :dispatch="dispatchToValidate"
      @close="dispatchToValidate = null"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePermissions } from '@/composables/usePermissions'
import { dispatchService } from '@/services/dispatch.service'
import type { DispatchResponse, DispatchStatus } from '@/types/dispatch.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import DispatchCreateModal from '@/components/dispatch/DispatchCreateModal.vue'
import DispatchValidateModal from '@/components/dispatch/DispatchValidateModal.vue'

const authStore = useAuthStore()
const { can } = usePermissions()
const warehouseId = computed(() => authStore.currentUser?.warehouseId!)
const isManager = computed(() => authStore.currentUser?.role === "Gestionnaire d'entrepôt")
const isStorekeeper = computed(() => authStore.currentUser?.role === 'Magasinier')

const tabs = computed(() => {
  const base = [{ key: 'list', label: 'Tous les bons' }]
  if (isManager.value) {
    return [{ key: 'pending', label: 'En attente' }, ...base]
  }
  return base
})

const activeTab = ref(isManager.value ? 'pending' : 'list')

const dispatches = ref<DispatchResponse[]>([])
const pendingCount = ref(0)
const loading = ref(false)
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const statusFilter = ref<DispatchStatus | ''>('')
const showCreateModal = ref(false)
const dispatchToValidate = ref<DispatchResponse | null>(null)

const statusLabel = (s: DispatchStatus) =>
  ({ PENDING: 'En attente', VALIDATED: 'Validé', REJECTED: 'Rejeté' }[s] ?? s)

const statusVariant = (s: DispatchStatus) =>
  ({ PENDING: 'warning', VALIDATED: 'success', REJECTED: 'danger' }[s] ?? 'secondary') as any

async function fetchDispatches() {
  loading.value = true
  try {
    const effectiveStatus = activeTab.value === 'pending'
      ? 'PENDING'
      : (statusFilter.value || undefined)
    const res = await dispatchService.list(warehouseId.value, {
      status: effectiveStatus as DispatchStatus | undefined,
      page: page.value,
      size: 20,
    })
    dispatches.value = res.content
    totalPages.value = res.totalPages
    totalElements.value = res.totalElements
  } finally {
    loading.value = false
  }
}

async function fetchPendingCount() {
  if (isManager.value) {
    pendingCount.value = await dispatchService.countPending(warehouseId.value)
  }
}

async function printBordereau(dispatchId: number) {
  const url = dispatchService.getBordereauUrl(warehouseId.value, dispatchId)
  const dispatch = dispatches.value.find((item) => item.id === dispatchId)
  const fileName = dispatch
    ? `${dispatch.dispatchNumber.replace(/[^a-z0-9]/gi, '-').toLowerCase()}.html`
    : `bordereau-${dispatchId}.html`

  try {
    const response = await fetch(url, {
      credentials: 'include',
      headers: { Accept: 'text/html' },
    })
    if (!response.ok) {
      throw new Error('Impossible de générer le bordereau')
    }

    const html = await response.text()
    const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
    const blobUrl = URL.createObjectURL(blob)

    window.open(blobUrl, '_blank', 'noopener,noreferrer')

    const link = document.createElement('a')
    link.href = blobUrl
    link.download = fileName
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    setTimeout(() => URL.revokeObjectURL(blobUrl), 1000)
  } catch (error) {
    console.error('Erreur lors du téléchargement du bordereau', error)
    window.open(url, '_blank', 'noopener,noreferrer')
  }
}

function onSaved() {
  showCreateModal.value = false
  dispatchToValidate.value = null
  fetchDispatches()
  fetchPendingCount()
}

onMounted(() => {
  fetchDispatches()
  fetchPendingCount()
})
</script>
