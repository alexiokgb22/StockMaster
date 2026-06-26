<template>
  <BaseModal :title="`Commande ${order.orderNumber}`" @close="$emit('close')">
    <div class="space-y-5">

      <!-- En-tête statut + méta -->
      <div class="flex flex-wrap items-center gap-3">
        <StatusBadge :label="statusLabel(order.status)" :variant="statusVariant(order.status)" />
        <span class="text-sm text-text-secondary">
          Créée le {{ order.orderDate ? new Date(order.orderDate).toLocaleDateString('fr-FR') : '—' }}
          par {{ order.createdByUsername }}
        </span>
      </div>

      <!-- Fournisseur (visible une fois validée) -->
      <div v-if="order.supplierId" class="rounded-xl border border-border bg-surface p-4">
        <p class="text-xs font-medium uppercase tracking-wide text-text-secondary mb-1">Fournisseur</p>
        <p class="font-semibold text-text-main">{{ order.supplierName }}</p>
        <div class="mt-1 flex flex-wrap gap-3 text-sm text-text-secondary">
          <span v-if="order.supplierContactName">{{ order.supplierContactName }}</span>
          <span v-if="order.supplierPhone">{{ order.supplierPhone }}</span>
          <span v-if="order.supplierEmail">{{ order.supplierEmail }}</span>
        </div>
      </div>

      <div v-else class="rounded-xl border border-dashed border-border p-3 text-sm italic text-text-secondary">
        En attente d'un fournisseur — l'administrateur validera et assignera le fournisseur.
      </div>

      <!-- Dates -->
      <div class="grid grid-cols-2 gap-3 text-sm">
        <div>
          <span class="text-text-secondary">Date de commande :</span>
          <span class="ml-1 font-medium">{{ order.orderDate ? new Date(order.orderDate).toLocaleDateString('fr-FR') : '—' }}</span>
        </div>
        <div>
          <span class="text-text-secondary">Livraison prévue :</span>
          <span class="ml-1 font-medium">{{ order.expectedDate ? new Date(order.expectedDate).toLocaleDateString('fr-FR') : '—' }}</span>
        </div>
      </div>

      <!-- Note -->
      <div v-if="order.note" class="rounded-xl bg-gray-50 px-4 py-3 text-sm italic text-text-secondary">
        {{ order.note }}
      </div>

      <!-- Lignes produits -->
      <div>
        <p class="mb-2 text-sm font-medium text-text-main">Produits commandés</p>
        <div class="overflow-hidden rounded-xl border border-border">
          <table class="w-full text-sm">
            <thead class="bg-gray-50 text-xs uppercase tracking-wide text-text-secondary">
              <tr>
                <th class="px-4 py-2 text-left">Produit</th>
                <th class="px-4 py-2 text-left">Catégorie</th>
                <th class="px-4 py-2 text-right">Qté commandée</th>
                <th class="px-4 py-2 text-right">Qté reçue</th>
                <th class="px-4 py-2 text-right">Prix unit.</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-border">
              <tr v-for="line in order.lines" :key="line.id" class="hover:bg-gray-50">
                <td class="px-4 py-2">
                  <span class="font-medium">{{ line.productName }}</span>
                  <code class="ml-2 rounded bg-gray-100 px-1 text-xs">{{ line.productReference }}</code>
                </td>
                <td class="px-4 py-2 text-text-secondary">{{ line.categoryName }}</td>
                <td class="px-4 py-2 text-right">{{ line.quantity }}</td>
                <td class="px-4 py-2 text-right">
                  <span :class="line.receivedQty > 0 ? 'text-green-600 font-medium' : 'text-text-secondary'">
                    {{ line.receivedQty }}
                  </span>
                </td>
                <td class="px-4 py-2 text-right text-text-secondary">
                  {{ line.unitPrice != null ? line.unitPrice.toLocaleString('fr-FR') + ' €' : '—' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Total -->
      <div v-if="order.totalAmount" class="flex justify-end text-sm">
        <span class="text-text-secondary mr-2">Total estimé :</span>
        <span class="font-semibold text-text-main">{{ order.totalAmount.toLocaleString('fr-FR') }} €</span>
      </div>

    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import type { PurchaseOrderResponse, PurchaseOrderStatus } from '@/types/purchaseorder.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'

defineProps<{ order: PurchaseOrderResponse }>()
defineEmits<{ close: [] }>()

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
</script>
