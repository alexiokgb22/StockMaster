<template>
  <div class="space-y-6">
    <PageHeader title="Réceptions" subtitle="Historique des réceptions de l'entrepôt" />

    <!-- Onglets -->
    <div class="flex gap-2 border-b border-border">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        @click="activeTab = tab.key; page = 0; fetchReceptions()"
        :class="[
          'px-4 py-2 text-sm font-medium transition border-b-2 -mb-px',
          activeTab === tab.key
            ? 'border-primary text-primary'
            : 'border-transparent text-text-secondary hover:text-text-main',
        ]"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Onglet : À réceptionner (Magasinier) -->
    <template v-if="activeTab === 'deliverable'">
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
                Réceptionner
              </BaseButton>
            </div>
          </div>
        </div>
      </BaseCard>
    </template>

    <!-- Onglet : Historique -->
    <template v-else>
      <!-- Filtre statut -->
      <BaseCard>
        <select
          v-model="statusFilter"
          @change="page = 0; fetchReceptions()"
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option value="">Tous les statuts</option>
          <option value="VALIDATED">Validé</option>
        </select>
      </BaseCard>

      <BaseCard>
        <div v-if="loadingReceptions" class="py-8 text-center text-sm text-text-secondary">
          Chargement…
        </div>
        <EmptyState
          v-else-if="receptions.length === 0"
          title="Aucun bon de réception"
          description="L'historique des réceptions apparaîtra ici."
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
                <StatusBadge label="Validé" variant="success" />
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
              <span v-if="r.supplierName" class="ml-2 font-medium text-primary">
                {{ r.supplierName }}
              </span>
              · {{ r.lines.length }} produit(s) · Par {{ r.createdByUsername }}
            </div>

            <div class="mt-3">
              <BaseButton size="sm" variant="secondary" @click="selectedReception = r">
                Voir le détail
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { receptionService } from '@/services/reception.service'
import type { ReceptionResponse, ReceptionStatus } from '@/types/reception.types'
import type { PurchaseOrderResponse } from '@/types/purchaseorder.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ReceptionCreateModal from '@/components/reception/ReceptionCreateModal.vue'
import ReceptionDetailModal from '@/components/reception/ReceptionDetailModal.vue'

const authStore = useAuthStore()
const toast = useToastStore()

const warehouseId   = computed(() => authStore.currentUser?.warehouseId!)
const isStorekeeper = computed(() => authStore.currentUser?.role === 'Magasinier')

// Magasinier : onglet "À réceptionner" en premier
// Gestionnaire / autres : historique directement
const tabs = computed(() => {
  if (isStorekeeper.value) {
    return [
      { key: 'deliverable', label: 'À réceptionner' },
      { key: 'list',        label: 'Historique' },
    ]
  }
  return [{ key: 'list', label: 'Historique' }]
})

const activeTab = ref(isStorekeeper.value ? 'deliverable' : 'list')

// ── État ──────────────────────────────────────────────────────
const receptions        = ref<ReceptionResponse[]>([])
const deliverableOrders = ref<PurchaseOrderResponse[]>([])
const loadingReceptions  = ref(false)
const loadingDeliverable = ref(false)
const page          = ref(0)
const totalPages    = ref(1)
const totalElements = ref(0)
const statusFilter  = ref<ReceptionStatus | ''>('')

const selectedReception = ref<ReceptionResponse | null>(null)
const orderForCreation  = ref<PurchaseOrderResponse | null>(null)

// ── Chargements ───────────────────────────────────────────────
async function fetchReceptions() {
  loadingReceptions.value = true
  try {
    const res = await receptionService.list(warehouseId.value, {
      status: (statusFilter.value || undefined) as ReceptionStatus | undefined,
      page:   page.value,
      size:   20,
    })
    receptions.value    = res.content
    totalPages.value    = res.totalPages
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

function openCreateModal(order: PurchaseOrderResponse) {
  orderForCreation.value = order
}

function onSaved() {
  orderForCreation.value = null
  toast.success('Réception enregistrée')
  fetchReceptions()
  if (isStorekeeper.value) fetchDeliverable()
}

onMounted(() => {
  fetchReceptions()
  if (isStorekeeper.value) fetchDeliverable()
})
</script>
