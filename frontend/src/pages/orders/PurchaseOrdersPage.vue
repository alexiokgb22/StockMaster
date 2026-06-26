<template>
  <div class="space-y-6">
    <PageHeader title="Commandes fournisseurs" subtitle="Demandes d'approvisionnement de votre entrepôt">
      <BaseButton @click="showCreateModal = true">+ Nouvelle demande</BaseButton>
    </PageHeader>

    <!-- Filtres -->
    <BaseCard>
      <div class="flex flex-wrap gap-3">
        <select
          v-model="statusFilter"
          @change="fetchOrders"
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option value="">Tous les statuts</option>
          <option value="DRAFT">En attente de validation</option>
          <option value="VALIDATED">Validée — fournisseur assigné</option>
          <option value="DELIVERED">En cours de réception</option>
          <option value="CLOSED">Clôturée</option>
          <option value="CANCELLED">Annulée</option>
        </select>
      </div>
    </BaseCard>

    <!-- Liste -->
    <BaseCard>
      <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">Chargement…</div>

      <div v-else-if="orders.length === 0">
        <EmptyState title="Aucune commande" description="Créez votre première demande d'approvisionnement." />
      </div>

      <div v-else class="space-y-3">
        <div
          v-for="order in orders"
          :key="order.id"
          class="rounded-xl border border-border bg-surface p-4 cursor-pointer hover:border-primary transition"
          @click="selectedOrder = order"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">{{ order.orderNumber }}</code>
              <StatusBadge :label="statusLabel(order.status)" :variant="statusVariant(order.status)" />
            </div>
            <div class="text-sm text-text-secondary">
              {{ order.orderDate ? new Date(order.orderDate).toLocaleDateString('fr-FR') : '—' }}
            </div>
          </div>

          <div class="mt-2 flex flex-wrap gap-4 text-sm text-text-secondary">
            <span>{{ order.lines.length }} produit(s)</span>
            <span v-if="order.totalAmount">{{ order.totalAmount.toLocaleString('fr-FR') }} €</span>
            <span v-if="order.supplierName" class="text-primary font-medium">Fournisseur : {{ order.supplierName }}</span>
            <span v-if="order.expectedDate">Livraison prévue : {{ new Date(order.expectedDate).toLocaleDateString('fr-FR') }}</span>
          </div>

          <div v-if="order.note" class="mt-1 text-xs italic text-text-secondary">{{ order.note }}</div>

          <!-- Actions selon statut -->
          <div class="mt-3 flex gap-2" @click.stop>
            <BaseButton
              v-if="order.status === 'DRAFT'"
              size="sm"
              variant="danger"
              @click="handleCancel(order)"
            >
              Annuler
            </BaseButton>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="mt-4 flex items-center justify-between">
        <span class="text-sm text-text-secondary">{{ totalElements }} commande(s)</span>
        <div class="flex gap-2">
          <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetchOrders()">Précédent</BaseButton>
          <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
          <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetchOrders()">Suivant</BaseButton>
        </div>
      </div>
    </BaseCard>

    <!-- Modale détail commande -->
    <PurchaseOrderDetailModal
      v-if="selectedOrder"
      :order="selectedOrder"
      @close="selectedOrder = null"
    />

    <!-- Modale création -->
    <PurchaseOrderCreateModal
      v-if="showCreateModal"
      :warehouse-id="warehouseId"
      @close="showCreateModal = false"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { purchaseOrderService } from '@/services/purchaseorder.service'
import { useToastStore } from '@/stores/toast'
import type { PurchaseOrderResponse, PurchaseOrderStatus } from '@/types/purchaseorder.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PurchaseOrderCreateModal from '@/components/order/PurchaseOrderCreateModal.vue'
import PurchaseOrderDetailModal from '@/components/order/PurchaseOrderDetailModal.vue'

const authStore = useAuthStore()
const toast = useToastStore()

const warehouseId = computed(() => authStore.currentUser?.warehouseId!)

const orders = ref<PurchaseOrderResponse[]>([])
const loading = ref(false)
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const statusFilter = ref<PurchaseOrderStatus | ''>('')
const selectedOrder = ref<PurchaseOrderResponse | null>(null)
const showCreateModal = ref(false)

const statusLabel = (s: PurchaseOrderStatus) => ({
  DRAFT: 'En attente de validation',
  VALIDATED: 'Validée',
  DELIVERED: 'En cours de réception',
  CLOSED: 'Clôturée',
  CANCELLED: 'Annulée',
}[s] ?? s)

const statusVariant = (s: PurchaseOrderStatus) => ({
  DRAFT: 'secondary',
  VALIDATED: 'info',
  DELIVERED: 'warning',
  CLOSED: 'success',
  CANCELLED: 'danger',
}[s] ?? 'secondary') as any

async function fetchOrders() {
  loading.value = true
  try {
    const res = await purchaseOrderService.listByWarehouse(warehouseId.value, {
      status: statusFilter.value || undefined,
      page: page.value,
      size: 20,
    })
    orders.value = res.content
    totalPages.value = res.totalPages
    totalElements.value = res.totalElements
  } finally {
    loading.value = false
  }
}

async function handleClose(order: PurchaseOrderResponse) {
  try {
    await purchaseOrderService.close(order.warehouseId, order.id)
    toast.success('Réception confirmée — commande clôturée')
    fetchOrders()
  } catch (e: any) {
    toast.error(e.response?.data?.message ?? 'Erreur')
  }
}

async function handleCancel(order: PurchaseOrderResponse) {
  try {
    await purchaseOrderService.cancel(order.warehouseId, order.id)
    toast.success('Commande annulée')
    fetchOrders()
  } catch (e: any) {
    toast.error(e.response?.data?.message ?? 'Erreur')
  }
}

function onSaved() { showCreateModal.value = false; fetchOrders() }

onMounted(fetchOrders)
</script>
