<template>
  <BaseCard>
    <div class="mb-4 text-sm font-semibold text-text-main">
      Top 5 produits les plus mouvementés — 30 derniers jours
    </div>

    <div v-if="loading" class="py-6 text-center text-sm text-text-secondary">
      Chargement…
    </div>
    <div v-else-if="data.length === 0" class="py-6 text-center text-sm text-text-secondary">
      Aucun mouvement sur la période
    </div>
    <div v-else class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-border text-left text-xs font-medium uppercase tracking-wide text-text-secondary">
            <th class="pb-3 pr-4">#</th>
            <th class="pb-3 pr-4">Produit</th>
            <th class="pb-3 pr-4">Catégorie</th>
            <th class="pb-3 pr-4 text-right">Mouvements</th>
            <th class="pb-3 text-right">Qté totale</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(item, i) in data"
            :key="item.productId"
            class="border-b border-border/50 last:border-0"
          >
            <td class="py-3 pr-4 text-text-secondary font-medium">{{ i + 1 }}</td>
            <td class="py-3 pr-4">
              <div class="font-medium text-text-main">{{ item.productName }}</div>
              <div class="text-xs text-text-secondary font-mono">{{ item.productReference }}</div>
            </td>
            <td class="py-3 pr-4 text-text-secondary">{{ item.categoryName }}</td>
            <td class="py-3 pr-4 text-right font-semibold text-primary">
              {{ item.movementCount }}
            </td>
            <td class="py-3 text-right text-text-secondary">{{ item.totalQuantity }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import type { TopProductPoint } from '@/types/dashboard.types'
import BaseCard from '@/components/ui/BaseCard.vue'

defineProps<{
  data: TopProductPoint[]
  loading: boolean
}>()
</script>
