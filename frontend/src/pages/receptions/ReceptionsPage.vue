<template>
  <div class="space-y-6">
    <PageHeader title="Réceptions" subtitle="Bons de réception de votre entrepôt" />

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

    <!-- Onglet : À réceptionner (Magasinier) -->
    <template v-if="activeTab === 'deliverable' && isStorekeeper">
      <BaseCard>
        <div v-if="loadingDeliverable" class="py-8 text-center text-sm text-text-secondary">
          Chargement…
        </div>
        <EmptyState
          v-else-if="deliverableOrders.length === 0"
          title="Aucune commande à réceptionner"
          description="Les commandes marquées comme livrées apparaîtront ici."
        />
        <div v-else class="space-y-3">
          <div
            v-for="order in deliverableOrders"
            :key="order.id"
            class="rounded-xl border border-border bg-surface p-4"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">
                  {{ order.orderNumber }}
                </code>
                <span class="text-sm font-medium text-primary">{{ order.supplierName }}</span>
              </div>
              <span class="text-sm text-text-secondary">
                {{ order.expectedDate
                  ? 'Livraison prévue : ' + new Date(order.expectedDate).toLocaleDateString('fr-FR')
                  : '—' }}
              </span>
            </div>

            <div class="mt-2 flex flex-wrap gap-2">
              <span
                v-for="line in order.lines"
                :key="line.id"
                class="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-text-secondary"
              >
                {{ line.productName }} × {{ line.quantity }}
              </span>
            </div>

            <div class="mt-3">
              <BaseButton size="sm" @click="openCreateModal(order)">
                Créer un bon de réception
              </BaseButton>
            </div>
          </div>
        </div>
      </BaseCard>
    </template>

    <!-- Onglet : Mes bons / Tous les bons -->
    <template v-else-if="activeTab === 'list' || activeTab === 'pending'">
      <!-- Filtre statut -->
      <BaseCard>
        <select
          v-model="statusFilter"
          @change="fetchReceptions"
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option value="">Tous les statuts</option>
          <option value="PENDING">En attente</option>
          <option value="VALIDATED">Validé</option>
          <option value="REJECTED">Rejeté</option>
        </select>
      </BaseCard>

      <BaseCard>
        <div v-if="loadingReceptions" class="py-8 text-center text-sm text-text-secondary">
          Chargement…
        </div>
        <EmptyState
          v-else-if="receptions.length === 0"
          title="Aucun bon de réception"
          description="Les bons de réception apparaîtront ici."
        />
        <div v-else class="space-y-3">
          <div
            v-for="r in receptions"
            :key="r.id"
            class="rounded-xl border border-border bg-surface p-4"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">
                  {{ r.receptionNumber }}
                </code>
                <StatusBadge :label="statusLabel(r.status)" :variant="statusVariant(r.status)" />
                <span v-if="r.gapCount > 0" class="text-xs text-amber-600">
                  ⚠ {{ r.gapCount }} écart(s)
                </span>
              </div>
              <span class="text-sm text-text-secondary">
                {{ new Date(r.createdAt).toLocaleDateString('fr-FR') }}
              </span>
            </div>

            <div class="mt-2 text-sm text-text-secondary">
              <code class="rounded bg-gray-50 px-1 text-xs">{{ r.purchaseOrderNumber }}</code>
              <span v-if="r.supplierName" class="ml-2 text-primary font-medium">
                {{ r.supplierName }}
              </span>
              · {{ r.lines.length }} produit(s) · Par {{ r.createdByUsername }}
            </div>

            <div class="mt-3 flex gap-2">
              <BaseButton size="sm" variant="secondary" @click="selectedReception = r">
                Voir le détail
              </BaseButton>
              <BaseButton
                v-if="r.status === 'PENDING' && isManager"
                size="sm"
                @click="receptionToValidate = r"
              >
                Valider / Rejeter
              </BaseButton>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="mt-4 flex items-center justify-between">
          <span class="text-sm text-text-secondary">{{ totalElements }} bon(s)</span>
          <div class="flex gap-2">
            <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetchReceptions()">
              Précédent
            </BaseButton>
            <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
            <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetchReceptions()">
              Suivant
            </BaseButton>
          </div>
        </div>
      </BaseCard>
    </template>

    <!-- Modales -->
    <ReceptionCreateModal
      v-if="orderForCreation"
      :order="orderForCreation"
      :warehouse-id="warehouseId"
      @close="orderForCreation = null"
      @saved="onSaved"
    />
    <ReceptionDetailModal
      v-if="selectedReception"
      :reception="selectedReception"
      @close="selectedReception = null"
    />
    <ReceptionValidateModal
      v-if="receptionToValidate"
      :reception="receptionToValidate"
      @close="receptionToValidate = null"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { receptionService } from '@/services/reception.service'
import { useToastStore } from '@/stores/toast'
import type { ReceptionResponse, ReceptionStatus } from '@/types/reception.types'
import type { PurchaseOrderResponse } from '@/types/purchaseorder.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ReceptionCreateModal from '@/components/reception/ReceptionCreateModal.vue'
import ReceptionDetailModal from '@/components/reception/ReceptionDetailModal.vue'
import ReceptionValidateModal from '@/components/reception/ReceptionValidateModal.vue'

const authStore = useAuthStore()
const toast = useToastStore()

const warehouseId = computed(() => authStore.currentUser?.warehouseId!)
const isManager = computed(() => authStore.currentUser?.role === "Gestionnaire d'entrepôt")
const isStorekeeper = computed(() => authStore.currentUser?.role === 'Magasinier')

// Onglets selon le rôle
const tabs = computed(() => {
  const base = [{ key: 'list', label: 'Tous les bons' }]
  if (isManager.value) {
    return [{ key: 'pending', label: 'En attente de validation' }, ...base]
  }
  if (isStorekeeper.value) {
    return [{ key: 'deliverable', label: 'À réceptionner' }, ...base]
  }
  return base
})

const activeTab = ref(isManager.value ? 'pending' : isStorekeeper.value ? 'deliverable' : 'list')

// ── État ──────────────────────────────────────────────────────
const receptions = ref<ReceptionResponse[]>([])
const deliverableOrders = ref<PurchaseOrderResponse[]>([])
const pendingCount = ref(0)
const loadingReceptions = ref(false)
const loadingDeliverable = ref(false)
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const statusFilter = ref<ReceptionStatus | ''>('')

const selectedReception = ref<ReceptionResponse | null>(null)
const receptionToValidate = ref<ReceptionResponse | null>(null)
const orderForCreation = ref<PurchaseOrderResponse | null>(null)

// ── Helpers ───────────────────────────────────────────────────
const statusLabel = (s: ReceptionStatus) =>
  ({ PENDING: 'En attente', VALIDATED: 'Validé', REJECTED: 'Rejeté' }[s] ?? s)

const statusVariant = (s: ReceptionStatus) =>
  ({ PENDING: 'warning', VALIDATED: 'success', REJECTED: 'danger' }[s] ?? 'secondary') as any

// ── Chargements ───────────────────────────────────────────────
async function fetchReceptions() {
  loadingReceptions.value = true
  try {
    const effectiveStatus = activeTab.value === 'pending'
      ? 'PENDING'
      : (statusFilter.value || undefined)
    const res = await receptionService.list(warehouseId.value, {
      status: effectiveStatus as ReceptionStatus | undefined,
      page: page.value,
      size: 20,
    })
    receptions.value = res.content
    totalPages.value = res.totalPages
    totalElements.value = res.totalElements
  } finally {
    loadingReceptions.value = false
  }
}

async function fetchDeliverable() {
  loadingDeliverable.value = true
  try {
    const res = await receptionService.listDeliverable(warehouseId.value)
    deliverableOrders.value = res.content
  } finally {
    loadingDeliverable.value = false
  }
}

async function fetchPendingCount() {
  if (isManager.value) {
    pendingCount.value = await receptionService.countPending(warehouseId.value)
  }
}

function openCreateModal(order: PurchaseOrderResponse) {
  orderForCreation.value = order
}

function onSaved() {
  orderForCreation.value = null
  receptionToValidate.value = null
  toast.success('Opération réussie')
  fetchReceptions()
  fetchPendingCount()
  if (isStorekeeper.value) fetchDeliverable()
}

onMounted(() => {
  fetchReceptions()
  fetchPendingCount()
  if (isStorekeeper.value) fetchDeliverable()
})
</script>
