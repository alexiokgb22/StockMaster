<template>
  <BaseModal :title="`Inventaire — ${inventory.inventoryNumber}`" @close="$emit('close')">
    <div class="space-y-4">

      <!-- Infos générales -->
      <div class="rounded-xl bg-gray-50 px-4 py-3 text-sm space-y-1">
        <div class="flex items-center gap-3">
          <StatusBadge :label="statusLabel" :variant="statusVariant" />
          <span class="text-text-secondary">
            {{ inventory.inventoryType === 'FULL' ? 'Complet' : 'Partiel' }}
          </span>
        </div>
        <p class="text-text-secondary">
          Démarré le {{ new Date(inventory.startedAt).toLocaleDateString('fr-FR') }}
          par <span class="font-medium text-text-main">{{ inventory.createdByUsername }}</span>
        </p>
        <p v-if="inventory.completedAt" class="text-text-secondary">
          Clôturé le {{ new Date(inventory.completedAt).toLocaleDateString('fr-FR') }}
        </p>
      </div>

      <!-- Résumé écarts -->
      <div class="flex gap-4 text-sm">
        <span class="text-text-secondary">
          Lignes : <strong class="text-text-main">{{ inventory.totalLines }}</strong>
        </span>
        <span class="text-text-secondary">
          Écarts :
          <strong :class="inventory.gapLines > 0 ? 'text-amber-600' : 'text-green-600'">
            {{ inventory.gapLines }}
          </strong>
        </span>
        <span v-if="inventory.gapLines > 0" class="text-text-secondary">
          Total : <strong class="text-red-600">{{ inventory.totalGap }}</strong>
        </span>
      </div>

      <!-- Tableau lecture seule -->
      <div class="overflow-hidden rounded-xl border border-border max-h-96 overflow-y-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 text-xs uppercase tracking-wide text-text-secondary sticky top-0">
            <tr>
              <th class="px-4 py-2 text-left">Produit</th>
              <th class="px-4 py-2 text-left">Zone</th>
              <th class="px-4 py-2 text-right">Théorique</th>
              <th class="px-4 py-2 text-right">Physique</th>
              <th class="px-4 py-2 text-right">Écart</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-border">
            <tr
              v-for="line in inventory.lines"
              :key="line.id"
              :class="line.gap !== 0 && line.gap !== null ? 'bg-red-50/30' : ''"
            >
              <td class="px-4 py-2">
                <div class="font-medium">{{ line.productName }}</div>
                <code class="text-xs text-text-secondary">{{ line.productReference }}</code>
              </td>
              <td class="px-4 py-2 text-text-secondary">{{ line.zoneName }}</td>
              <td class="px-4 py-2 text-right">{{ line.theoreticalQty }}</td>
              <td class="px-4 py-2 text-right font-medium">{{ line.actualQty ?? '—' }}</td>
              <td class="px-4 py-2 text-right">
                <span
                  v-if="line.gap !== null"
                  class="font-medium"
                  :class="
                    line.gap === 0 ? 'text-green-600' :
                    line.gap > 0 ? 'text-amber-600' : 'text-red-600'
                  "
                >
                  {{ line.gap > 0 ? '+' : '' }}{{ line.gap }}
                </span>
                <span v-else class="text-text-secondary">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="flex justify-end">
        <BaseButton variant="secondary" @click="$emit('close')">Fermer</BaseButton>
      </div>
    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { InventoryResponse } from '@/types/inventory.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'

const props = defineProps<{ inventory: InventoryResponse }>()
defineEmits<{ close: [] }>()

const statusLabel = computed(() =>
  ({ IN_PROGRESS: 'En cours', COMPLETED: 'Clôturé', CANCELLED: 'Annulé' }[props.inventory.inventoryStatus] ?? props.inventory.inventoryStatus)
)
const statusVariant = computed(() =>
  ({ IN_PROGRESS: 'warning', COMPLETED: 'success', CANCELLED: 'secondary' }[props.inventory.inventoryStatus] ?? 'secondary') as any
)
</script>
