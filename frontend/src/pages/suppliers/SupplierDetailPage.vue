<template>
  <div class="space-y-6">
    <!-- Retour -->
    <router-link :to="{ name: 'Suppliers' }" class="inline-flex items-center gap-1 text-sm text-primary hover:underline">
      ← Retour aux fournisseurs
    </router-link>

    <!-- En-tête fournisseur -->
    <div v-if="supplier" class="flex items-start justify-between">
      <div>
        <h1 class="text-2xl font-bold text-text-main">{{ supplier.name }}</h1>
        <p class="text-text-secondary text-sm mt-1">
          <span v-if="supplier.contactName">Contact : {{ supplier.contactName }} &nbsp;·&nbsp;</span>
          <span v-if="supplier.phone">{{ supplier.phone }} &nbsp;·&nbsp;</span>
          <span v-if="supplier.email">{{ supplier.email }}</span>
        </p>
        <p v-if="supplier.address || supplier.city" class="text-text-secondary text-sm">
          {{ [supplier.address, supplier.city].filter(Boolean).join(', ') }}
        </p>
      </div>
      <StatusBadge :label="supplier.isActive ? 'Actif' : 'Inactif'" :variant="supplier.isActive ? 'success' : 'secondary'" />
    </div>
    <div v-else-if="loadingSupplier" class="py-4 text-sm text-text-secondary">Chargement…</div>

    <!-- Entrepôts affectés — vue liste pour sélectionner -->
    <BaseCard v-if="!selectedWarehouse">
      <h2 class="mb-4 text-base font-semibold text-text-main">Entrepôts approvisionnés</h2>

      <div v-if="loadingWarehouses" class="py-6 text-center text-sm text-text-secondary">Chargement…</div>

      <div v-else-if="supplierWarehouses.length === 0" class="py-6 text-center text-sm text-text-secondary italic">
        Ce fournisseur n'est affecté à aucun entrepôt.
      </div>

      <div v-else class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <div
          v-for="w in supplierWarehouses"
          :key="w.id"
          class="group flex cursor-pointer items-center justify-between rounded-xl border border-border bg-surface px-4 py-3 transition hover:border-primary hover:bg-primary-light/30"
          @click="selectWarehouse(w)"
        >
          <div>
            <div class="font-medium text-text-main group-hover:text-primary">{{ w.name }}</div>
            <div v-if="w.city" class="text-xs text-text-secondary">{{ w.city }}</div>
          </div>
          <div class="text-right">
            <div class="text-lg font-bold text-primary">{{ w.deliveryCount }}</div>
            <div class="text-xs text-text-secondary">livraison(s)</div>
          </div>
        </div>
      </div>
    </BaseCard>

    <!-- Historique de livraisons pour l'entrepôt sélectionné -->
    <BaseCard v-else>
      <div class="mb-4 flex items-center justify-between">
        <div>
          <button
            class="mb-1 inline-flex items-center gap-1 text-xs text-primary hover:underline"
            @click="selectedWarehouse = null"
          >
            ← Tous les entrepôts
          </button>
          <h2 class="text-base font-semibold text-text-main">
            Livraisons — {{ selectedWarehouse.name }}
          </h2>
        </div>
        <span class="text-sm text-text-secondary">{{ totalDeliveries }} commande(s)</span>
      </div>

      <div v-if="loadingHistory" class="py-6 text-center text-sm text-text-secondary">Chargement…</div>

      <div v-else-if="deliveries.length === 0" class="py-6 text-center text-sm italic text-text-secondary">
        Aucune livraison enregistrée pour cet entrepôt.
      </div>

      <div v-else class="space-y-3">
        <div
          v-for="d in deliveries"
          :key="d.orderId"
          class="rounded-xl border border-border bg-surface p-4 space-y-2"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">{{ d.orderNumber }}</code>
              <StatusBadge :label="statusLabel(d.status)" :variant="statusVariant(d.status)" />
            </div>
            <div class="text-right text-sm">
              <span v-if="d.totalAmount != null" class="font-semibold text-text-main">{{ d.totalAmount.toLocaleString('fr-FR') }} €</span>
              <div class="text-xs text-text-secondary">
                {{ d.orderDate ? new Date(d.orderDate).toLocaleDateString('fr-FR') : '—' }}
              </div>
            </div>
          </div>

          <!-- Lignes de la commande -->
          <div v-if="d.lines.length > 0" class="mt-2 overflow-x-auto">
            <table class="w-full text-xs">
              <thead>
                <tr class="text-left text-text-secondary border-b border-border">
                  <th class="pb-1 pr-4">Produit</th>
                  <th class="pb-1 pr-4">Réf.</th>
                  <th class="pb-1 pr-4 text-right">Qté</th>
                  <th class="pb-1 pr-4 text-right">Reçu</th>
                  <th class="pb-1 text-right">P.U.</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="l in d.lines" :key="l.productId" class="border-b border-border/50 last:border-0">
                  <td class="py-1 pr-4">{{ l.productName }}</td>
                  <td class="py-1 pr-4 font-mono text-text-secondary">{{ l.productReference }}</td>
                  <td class="py-1 pr-4 text-right">{{ l.quantity }}</td>
                  <td class="py-1 pr-4 text-right">{{ l.receivedQty ?? '—' }}</td>
                  <td class="py-1 text-right">{{ l.unitPrice != null ? l.unitPrice + ' €' : '—' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Pagination historique -->
      <div v-if="totalDeliveryPages > 1" class="mt-4 flex items-center justify-end gap-2">
        <BaseButton size="sm" variant="secondary" :disabled="deliveryPage === 0" @click="deliveryPage--; fetchHistory()">Précédent</BaseButton>
        <span class="py-1 text-sm">{{ deliveryPage + 1 }} / {{ totalDeliveryPages }}</span>
        <BaseButton size="sm" variant="secondary" :disabled="deliveryPage + 1 >= totalDeliveryPages" @click="deliveryPage++; fetchHistory()">Suivant</BaseButton>
      </div>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { supplierService } from '@/services/supplier.service'
import type { SupplierResponse, SupplierWarehouseResponse, DeliveryHistoryResponse } from '@/types/supplier.types'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'

const route = useRoute()

const supplier           = ref<SupplierResponse | null>(null)
const supplierWarehouses = ref<SupplierWarehouseResponse[]>([])
const deliveries         = ref<DeliveryHistoryResponse[]>([])
const selectedWarehouse  = ref<SupplierWarehouseResponse | null>(null)

const loadingSupplier  = ref(false)
const loadingWarehouses = ref(false)
const loadingHistory   = ref(false)

const deliveryPage         = ref(0)
const totalDeliveryPages   = ref(1)
const totalDeliveries      = ref(0)

const supplierId = Number(route.params.id)

const statusLabel = (s: DeliveryHistoryResponse['status']) => ({
  DRAFT: 'Brouillon', VALIDATED: 'Validée', DELIVERED: 'Livrée', CANCELLED: 'Annulée',
}[s] ?? s)

const statusVariant = (s: DeliveryHistoryResponse['status']) => ({
  DRAFT: 'secondary', VALIDATED: 'info', DELIVERED: 'success', CANCELLED: 'danger',
}[s] ?? 'secondary') as 'secondary' | 'info' | 'success' | 'danger'

async function selectWarehouse(w: SupplierWarehouseResponse) {
  selectedWarehouse.value = w
  deliveryPage.value = 0
  await fetchHistory()
}

async function fetchHistory() {
  if (!selectedWarehouse.value) return
  loadingHistory.value = true
  try {
    const res = await supplierService.getDeliveryHistory(supplierId, selectedWarehouse.value.id, {
      page: deliveryPage.value,
      size: 10,
    })
    deliveries.value         = res.content
    totalDeliveries.value    = res.totalElements
    totalDeliveryPages.value = res.totalPages
  } finally {
    loadingHistory.value = false
  }
}

onMounted(async () => {
  loadingSupplier.value = true
  loadingWarehouses.value = true
  try {
    const [s, ws] = await Promise.all([
      supplierService.getById(supplierId),
      supplierService.getWarehouses(supplierId),
    ])
    supplier.value = s
    supplierWarehouses.value = ws
  } finally {
    loadingSupplier.value = false
    loadingWarehouses.value = false
  }
})
</script>
