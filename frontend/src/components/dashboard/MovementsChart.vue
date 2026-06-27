<template>
  <BaseCard>
    <div class="mb-4 text-sm font-semibold text-text-main">
      Entrées / Sorties — 30 derniers jours
    </div>

    <div v-if="loading" class="flex h-48 items-center justify-center text-sm text-text-secondary">
      Chargement…
    </div>
    <div v-else-if="data.length === 0" class="flex h-48 items-center justify-center text-sm text-text-secondary">
      Aucune donnée de mouvement
    </div>
    <div v-else>
      <!-- Légende -->
      <div class="mb-3 flex gap-4 text-xs text-text-secondary">
        <span class="flex items-center gap-1">
          <span class="inline-block h-2 w-3 rounded-sm bg-green-500" /> Entrées
        </span>
        <span class="flex items-center gap-1">
          <span class="inline-block h-2 w-3 rounded-sm bg-red-400" /> Sorties
        </span>
        <span class="flex items-center gap-1">
          <span class="inline-block h-2 w-3 rounded-sm bg-blue-400" /> Transferts
        </span>
      </div>

      <!-- Barres SVG simples -->
      <div class="overflow-x-auto">
        <div class="flex items-end gap-0.5" style="min-width: 600px; height: 160px">
          <template v-for="(point, i) in visibleData" :key="i">
            <div class="flex flex-1 flex-col items-center gap-0.5" style="min-width: 14px">
              <!-- Barre empilée -->
              <div class="flex w-full flex-col-reverse gap-px" :style="{ height: '140px' }">
                <div
                  v-if="point.entries > 0"
                  class="w-full rounded-t bg-green-500"
                  :style="{ height: barHeight(point.entries) + 'px' }"
                  :title="`Entrées : ${point.entries}`"
                />
                <div
                  v-if="point.exits > 0"
                  class="w-full bg-red-400"
                  :style="{ height: barHeight(point.exits) + 'px' }"
                  :title="`Sorties : ${point.exits}`"
                />
                <div
                  v-if="point.transfers > 0"
                  class="w-full bg-blue-400"
                  :style="{ height: barHeight(point.transfers) + 'px' }"
                  :title="`Transferts : ${point.transfers}`"
                />
              </div>
              <!-- Label date (1 sur 5) -->
              <div
                v-if="i % 5 === 0"
                class="text-[9px] text-text-secondary rotate-45 mt-1 origin-left"
                style="white-space: nowrap"
              >
                {{ formatLabel(point.label) }}
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { MovementChartPoint } from '@/types/dashboard.types'
import BaseCard from '@/components/ui/BaseCard.vue'

const props = defineProps<{
  data: MovementChartPoint[]
  loading: boolean
}>()

// Afficher les 30 derniers points
const visibleData = computed(() => props.data.slice(-30))

const maxValue = computed(() => {
  if (visibleData.value.length === 0) return 1
  return Math.max(
    1,
    ...visibleData.value.map((p) => p.entries + p.exits + p.transfers),
  )
})

function barHeight(value: number): number {
  return Math.max(2, Math.round((value / maxValue.value) * 120))
}

function formatLabel(label: string): string {
  // "2026-06-27" → "27/06"
  const parts = label.split('-')
  if (parts.length === 3) return `${parts[2]}/${parts[1]}`
  return label
}
</script>
