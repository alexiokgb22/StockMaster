<template>
  <BaseCard>
    <div class="mb-4 text-sm font-semibold text-text-main">Répartition par catégorie</div>

    <div v-if="loading" class="flex h-48 items-center justify-center text-sm text-text-secondary">
      Chargement…
    </div>
    <div v-else-if="data.length === 0" class="flex h-48 items-center justify-center text-sm text-text-secondary">
      Aucune donnée
    </div>
    <div v-else class="space-y-3">
      <div
        v-for="(item, i) in sortedData"
        :key="item.categoryId"
        class="flex items-center gap-3"
      >
        <!-- Couleur -->
        <span
          class="inline-block h-3 w-3 flex-shrink-0 rounded-full"
          :style="{ backgroundColor: colors[i % colors.length] }"
        />
        <!-- Label -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center justify-between text-xs">
            <span class="truncate text-text-main font-medium">{{ item.categoryName }}</span>
            <span class="ml-2 text-text-secondary">
              {{ item.totalQuantity }} unités
            </span>
          </div>
          <!-- Barre de progression -->
          <div class="mt-1 h-2 w-full rounded-full bg-gray-100">
            <div
              class="h-2 rounded-full transition-all"
              :style="{
                width: pct(item.totalQuantity) + '%',
                backgroundColor: colors[i % colors.length],
              }"
            />
          </div>
        </div>
        <!-- % -->
        <span class="text-xs text-text-secondary w-10 text-right flex-shrink-0">
          {{ pct(item.totalQuantity).toFixed(0) }}%
        </span>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CategoryStockPoint } from '@/types/dashboard.types'
import BaseCard from '@/components/ui/BaseCard.vue'

const props = defineProps<{
  data: CategoryStockPoint[]
  loading: boolean
}>()

const colors = [
  '#6366f1', '#22c55e', '#f59e0b', '#ef4444',
  '#3b82f6', '#8b5cf6', '#14b8a6', '#f97316',
]

const sortedData = computed(() =>
  [...props.data].sort((a, b) => b.totalQuantity - a.totalQuantity).slice(0, 8),
)

const total = computed(() =>
  sortedData.value.reduce((sum, d) => sum + d.totalQuantity, 0),
)

function pct(value: number): number {
  if (total.value === 0) return 0
  return Math.round((value / total.value) * 100)
}
</script>
