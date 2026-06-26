<template>
  <div class="space-y-6">
    <router-link :to="{ name: 'Suppliers' }" class="inline-flex items-center gap-1 text-sm text-primary hover:underline">
      ← Retour aux fournisseurs
    </router-link>

    <div v-if="loadingSupplier" class="py-8 text-center text-sm text-text-secondary">Chargement…</div>

    <template v-else-if="supplier">
      <!-- En-tête fournisseur -->
      <div class="flex items-start justify-between">
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
        <div class="flex items-center gap-3">
          <StatusBadge
            :label="supplier.isActive ? 'Actif' : 'Inactif'"
            :variant="supplier.isActive ? 'success' : 'secondary'"
          />
          <BaseButton size="sm" variant="secondary" @click="openEdit">Modifier</BaseButton>
        </div>
      </div>

      <!-- Historique des commandes -->
      <BaseCard>
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-base font-semibold text-text-main">Historique des commandes</h2>
          <span class="text-sm text-text-secondary">{{ totalOrders }} commande(s)</span>
        </div>

        <div v-if="loadingOrders" class="py-6 text-center text-sm text-text-secondary">
          Chargement de l'historique…
        </div>

        <div v-else-if="orders.length === 0" class="py-6 text-center text-sm italic text-text-secondary">
          Aucune commande associée à ce fournisseur pour l'instant.
        </div>

        <div v-else class="space-y-3">
          <div
            v-for="order in orders"
            :key="order.id"
            class="rounded-xl border border-border bg-surface p-4"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">{{ order.orderNumber }}</code>
                <StatusBadge :label="statusLabel(order.status)" :variant="statusVariant(order.status)" />
                <span class="text-sm font-medium text-text-main">{{ order.warehouseName }}</span>
              </div>
              <span class="text-sm text-text-secondary">
                {{ order.orderDate ? new Date(order.orderDate).toLocaleDateString('fr-FR') : '—' }}
              </span>
            </div>

            <!-- Lignes résumées -->
            <div class="mt-2 flex flex-wrap gap-2">
              <span
                v-for="line in order.lines"
                :key="line.id"
                class="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-text-secondary"
              >
                {{ line.productName }} × {{ line.quantity }}
                <span v-if="line.unitPrice != null"> — {{ line.unitPrice.toLocaleString('fr-FR') }} €/u</span>
              </span>
            </div>

            <div class="mt-2 flex flex-wrap gap-4 text-sm text-text-secondary">
              <span v-if="order.totalAmount && order.totalAmount > 0">
                Total : <span class="font-medium text-text-main">{{ order.totalAmount.toLocaleString('fr-FR') }} €</span>
              </span>
              <span v-if="order.expectedDate">
                Livraison prévue : {{ new Date(order.expectedDate).toLocaleDateString('fr-FR') }}
              </span>
              <span>Par {{ order.createdByUsername }}</span>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="mt-4 flex items-center justify-between">
          <span class="text-sm text-text-secondary">Page {{ page + 1 }} / {{ totalPages }}</span>
          <div class="flex gap-2">
            <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetchOrders()">
              Précédent
            </BaseButton>
            <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetchOrders()">
              Suivant
            </BaseButton>
          </div>
        </div>
      </BaseCard>
    </template>

    <SupplierFormModal
      v-if="showModal && supplier"
      :supplier="supplier"
      @close="showModal = false"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { supplierService } from '@/services/supplier.service'
import { purchaseOrderService } from '@/services/purchaseorder.service'
import type { SupplierResponse } from '@/types/supplier.types'
import type { PurchaseOrderResponse, PurchaseOrderStatus } from '@/types/purchaseorder.types'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import SupplierFormModal from '@/components/supplier/SupplierFormModal.vue'

const route = useRoute()
const supplierId = Number(route.params.id)

const supplier = ref<SupplierResponse | null>(null)
const loadingSupplier = ref(false)
const showModal = ref(false)

const orders = ref<PurchaseOrderResponse[]>([])
const loadingOrders = ref(false)
const page = ref(0)
const totalPages = ref(1)
const totalOrders = ref(0)

const statusLabel = (s: PurchaseOrderStatus) =>
  ({
    DRAFT: 'En attente de validation',
    VALIDATED: 'Validée',
    DELIVERED: 'En cours de réception',
    CLOSED: 'Clôturée',
    CANCELLED: 'Annulée',
  }[s] ?? s)

const statusVariant = (s: PurchaseOrderStatus) =>
  ({
    DRAFT: 'secondary',
    VALIDATED: 'info',
    DELIVERED: 'warning',
    CLOSED: 'success',
    CANCELLED: 'danger',
  }[s] ?? 'secondary') as any

async function fetchOrders() {
  loadingOrders.value = true
  try {
    const res = await purchaseOrderService.listBySupplierId(supplierId, {
      page: page.value,
      size: 10,
    })
    orders.value = res.content
    totalPages.value = res.totalPages
    totalOrders.value = res.totalElements
  } finally {
    loadingOrders.value = false
  }
}

function openEdit() {
  showModal.value = true
}

async function onSaved() {
  showModal.value = false
  loadingSupplier.value = true
  try {
    supplier.value = await supplierService.getById(supplierId)
  } finally {
    loadingSupplier.value = false
  }
}

onMounted(async () => {
  loadingSupplier.value = true
  try {
    supplier.value = await supplierService.getById(supplierId)
  } finally {
    loadingSupplier.value = false
  }
  fetchOrders()
})
</script>
