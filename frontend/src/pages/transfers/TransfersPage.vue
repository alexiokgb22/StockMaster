<template>
  <div class="space-y-6">
    <PageHeader title="Transferts" subtitle="Transferts inter-entrepôts" />

    <!-- Onglets selon le rôle -->
    <div class="flex gap-2 border-b border-border">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        @click="switchTab(tab.key)"
        :class="[
          'px-4 py-2 text-sm font-medium transition border-b-2 -mb-px',
          activeTab === tab.key
            ? 'border-primary text-primary'
            : 'border-transparent text-text-secondary hover:text-text-main',
        ]"
      >
        {{ tab.label }}
        <span
          v-if="tab.key === 'incoming' && incomingCount > 0"
          class="ml-1 rounded-full bg-orange-500 px-1.5 py-0.5 text-xs text-white"
        >
          {{ incomingCount }}
        </span>
        <span
          v-if="tab.key === 'pending' && pendingCount > 0"
          class="ml-1 rounded-full bg-red-500 px-1.5 py-0.5 text-xs text-white"
        >
          {{ pendingCount }}
        </span>
      </button>
    </div>

    <!-- Bouton créer (gestionnaire source) -->
    <div v-if="isManager && activeTab === 'outgoing'" class="flex justify-end">
      <BaseButton @click="showCreateModal = true">+ Nouveau transfert</BaseButton>
    </div>

    <!-- Filtre statut -->
    <BaseCard>
      <select
        v-model="statusFilter"
        @change="fetchTransfers"
        class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
      >
        <option value="">Tous les statuts</option>
        <option value="PENDING">En attente</option>
        <option value="VALIDATED">Validé</option>
        <option value="RECEIVED">Reçu</option>
        <option value="CANCELLED">Annulé</option>
      </select>
    </BaseCard>

    <!-- Liste -->
    <BaseCard>
      <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">Chargement…</div>
      <EmptyState
        v-else-if="transfers.length === 0"
        title="Aucun transfert"
        description="Les transferts apparaîtront ici."
      />
      <div v-else class="space-y-3">
        <div
          v-for="t in transfers"
          :key="t.id"
          class="rounded-xl border border-border bg-surface p-4"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">
                {{ t.transferNumber }}
              </code>
              <StatusBadge :label="statusLabel(t.status)" :variant="statusVariant(t.status)" />
            </div>
            <span class="text-sm text-text-secondary">
              {{ new Date(t.createdAt).toLocaleDateString('fr-FR') }}
            </span>
          </div>

          <div class="mt-2 text-sm text-text-secondary">
            <span class="font-medium text-text-main">{{ t.sourceWarehouseName }}</span>
            <span class="mx-2">→</span>
            <span class="font-medium text-text-main">{{ t.targetWarehouseName }}</span>
            · {{ t.lines.length }} produit(s) · Par {{ t.createdByUsername }}
          </div>

          <div class="mt-3 flex gap-2">
            <!-- Admin : valider/annuler -->
            <BaseButton
              v-if="isAdmin && (t.status === 'PENDING' || t.status === 'VALIDATED')"
              size="sm"
              @click="transferForAdmin = t"
            >
              {{ t.status === 'PENDING' ? 'Valider / Annuler' : 'Annuler' }}
            </BaseButton>

            <!-- Gestionnaire cible : réceptionner -->
            <BaseButton
              v-if="isManager && activeTab === 'incoming' && t.status === 'VALIDATED'"
              size="sm"
              @click="transferToReceive = t"
            >
              Confirmer la réception
            </BaseButton>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="mt-4 flex items-center justify-between">
        <span class="text-sm text-text-secondary">{{ totalElements }} transfert(s)</span>
        <div class="flex gap-2">
          <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetchTransfers()">
            Précédent
          </BaseButton>
          <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
          <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetchTransfers()">
            Suivant
          </BaseButton>
        </div>
      </div>
    </BaseCard>

    <!-- Modales -->
    <TransferCreateModal
      v-if="showCreateModal"
      :warehouse-id="warehouseId!"
      @close="showCreateModal = false"
      @saved="onSaved"
    />
    <TransferReceiveModal
      v-if="transferToReceive"
      :transfer="transferToReceive"
      :warehouse-id="warehouseId!"
      @close="transferToReceive = null"
      @saved="onSaved"
    />
    <TransferAdminModal
      v-if="transferForAdmin"
      :transfer="transferForAdmin"
      @close="transferForAdmin = null"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { transferService } from '@/services/transfer.service'
import type { TransferResponse, TransferStatus } from '@/types/transfer.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import TransferCreateModal from '@/components/transfer/TransferCreateModal.vue'
import TransferReceiveModal from '@/components/transfer/TransferReceiveModal.vue'
import TransferAdminModal from '@/components/transfer/TransferAdminModal.vue'

const authStore = useAuthStore()
const warehouseId = computed(() => authStore.currentUser?.warehouseId)
const isAdmin = computed(() => authStore.currentUser?.role === 'Administrateur')
const isManager = computed(() => authStore.currentUser?.role === "Gestionnaire d'entrepôt")

const tabs = computed(() => {
  if (isAdmin.value) {
    return [
      { key: 'all', label: 'Tous les transferts' },
      { key: 'pending', label: 'En attente' },
    ]
  }
  return [
    { key: 'outgoing', label: 'Sortants' },
    { key: 'incoming', label: 'Entrants' },
  ]
})

const activeTab = ref(isAdmin.value ? 'pending' : 'outgoing')
const transfers = ref<TransferResponse[]>([])
const pendingCount = ref(0)
const incomingCount = ref(0)
const loading = ref(false)
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const statusFilter = ref<TransferStatus | ''>('')
const showCreateModal = ref(false)
const transferToReceive = ref<TransferResponse | null>(null)
const transferForAdmin = ref<TransferResponse | null>(null)

const statusLabel = (s: TransferStatus) =>
  ({
    PENDING: 'En attente',
    VALIDATED: 'Validé',
    RECEIVED: 'Reçu',
    CANCELLED: 'Annulé',
  }[s] ?? s)

const statusVariant = (s: TransferStatus) =>
  ({
    PENDING: 'warning',
    VALIDATED: 'info',
    RECEIVED: 'success',
    CANCELLED: 'danger',
  }[s] ?? 'secondary') as any

function switchTab(key: string) {
  activeTab.value = key
  page.value = 0
  statusFilter.value = ''
  fetchTransfers()
}

async function fetchTransfers() {
  loading.value = true
  try {
    const params = {
      status: statusFilter.value || undefined,
      page: page.value,
      size: 20,
    } as any

    if (isAdmin.value) {
      if (activeTab.value === 'pending') params.status = 'PENDING'
      const res = await transferService.listAll(params)
      transfers.value = res.content
      totalPages.value = res.totalPages
      totalElements.value = res.totalElements
    } else if (activeTab.value === 'incoming') {
      const res = await transferService.listIncoming(warehouseId.value!, params)
      transfers.value = res.content
      totalPages.value = res.totalPages
      totalElements.value = res.totalElements
    } else {
      const res = await transferService.listOutgoing(warehouseId.value!, params)
      transfers.value = res.content
      totalPages.value = res.totalPages
      totalElements.value = res.totalElements
    }
  } finally {
    loading.value = false
  }
}

async function fetchCounts() {
  if (isAdmin.value) {
    const res = await transferService.listAll({ status: 'PENDING', page: 0, size: 1 })
    pendingCount.value = res.totalElements
  } else if (isManager.value && warehouseId.value) {
    incomingCount.value = await transferService.countIncoming(warehouseId.value)
  }
}

function onSaved() {
  showCreateModal.value = false
  transferToReceive.value = null
  transferForAdmin.value = null
  fetchTransfers()
  fetchCounts()
}

onMounted(() => {
  fetchTransfers()
  fetchCounts()
})
</script>
