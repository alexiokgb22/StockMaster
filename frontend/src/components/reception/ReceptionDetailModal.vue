<template>
  <BaseModal :title="`Bon ${reception.receptionNumber}`" @close="$emit('close')">
    <div class="space-y-5">

      <!-- Statut + méta -->
      <div class="flex flex-wrap items-center gap-3">
        <StatusBadge :label="statusLabel(reception.status)" :variant="statusVariant(reception.status)" />
        <span class="text-sm text-text-secondary">
          Créé par {{ reception.createdByUsername }} ·
          {{ new Date(reception.createdAt).toLocaleDateString('fr-FR') }}
        </span>
        <span v-if="reception.validatedByUsername" class="text-sm text-text-secondary">
          · {{ reception.status === 'VALIDATED' ? 'Validé' : 'Rejeté' }} par
          {{ reception.validatedByUsername }}
        </span>
      </div>

      <!-- Commande associée -->
      <div class="rounded-xl border border-border bg-surface px-4 py-3 text-sm">
        <div class="flex items-center gap-2">
          <span class="text-text-secondary">Commande :</span>
          <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">
            {{ reception.purchaseOrderNumber }}
          </code>
          <span v-if="reception.supplierName" class="text-primary font-medium">
            — {{ reception.supplierName }}
          </span>
        </div>
      </div>

      <!-- Raison du rejet -->
      <div
        v-if="reception.status === 'REJECTED' && reception.rejectionReason"
        class="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700"
      >
        <span class="font-medium">Motif du rejet :</span> {{ reception.rejectionReason }}
      </div>

      <!-- Note générale -->
      <div v-if="reception.note" class="rounded-xl bg-gray-50 px-4 py-3 text-sm italic text-text-secondary">
        {{ reception.note }}
      </div>

      <!-- Alerte écarts -->
      <div
        v-if="reception.gapCount > 0"
        class="rounded-xl bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-700"
      >
        ⚠ {{ reception.gapCount }} produit(s) avec écart entre quantité attendue et reçue
      </div>

      <!-- Tableau des lignes -->
      <div class="overflow-hidden rounded-xl border border-border">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 text-xs uppercase tracking-wide text-text-secondary">
            <tr>
              <th class="px-4 py-2 text-left">Produit</th>
              <th class="px-4 py-2 text-right">Attendu</th>
              <th class="px-4 py-2 text-right">Reçu</th>
              <th class="px-4 py-2 text-right">Écart</th>
              <th class="px-4 py-2 text-left">Zone</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-border">
            <tr v-for="line in reception.lines" :key="line.id" class="hover:bg-gray-50">
              <td class="px-4 py-2">
                <span class="font-medium">{{ line.productName }}</span>
                <code class="ml-1 rounded bg-gray-100 px-1 text-xs">{{ line.productReference }}</code>
                <p v-if="line.note" class="text-xs italic text-text-secondary mt-0.5">{{ line.note }}</p>
              </td>
              <td class="px-4 py-2 text-right">{{ line.quantityExpected }}</td>
              <td class="px-4 py-2 text-right font-medium">{{ line.quantityReceived }}</td>
              <td class="px-4 py-2 text-right">
                <span
                  :class="line.gap === 0 ? 'text-green-600' : line.gap < 0 ? 'text-red-600' : 'text-amber-600'"
                  class="font-medium"
                >
                  {{ line.gap > 0 ? '+' : '' }}{{ line.gap }}
                </span>
              </td>
              <td class="px-4 py-2 text-text-secondary">{{ line.zoneName }}</td>
            </tr>
          </tbody>
        </table>
      </div>

    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import type { ReceptionResponse, ReceptionStatus } from '@/types/reception.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'

defineProps<{ reception: ReceptionResponse }>()
defineEmits<{ close: [] }>()

const statusLabel = (s: ReceptionStatus) =>
  ({ PENDING: 'En attente', VALIDATED: 'Validé', REJECTED: 'Rejeté' }[s] ?? s)

const statusVariant = (s: ReceptionStatus) =>
  ({ PENDING: 'warning', VALIDATED: 'success', REJECTED: 'danger' }[s] ?? 'secondary') as any
</script>
