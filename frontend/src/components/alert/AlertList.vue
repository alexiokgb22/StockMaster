<template>
  <BaseCard>
    <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">
      Chargement…
    </div>
    <EmptyState
      v-else-if="alerts.length === 0"
      title="Aucune alerte"
      description="Aucune alerte ne correspond aux filtres sélectionnés."
    />
    <div v-else class="space-y-3">
      <div
        v-for="alert in alerts"
        :key="alert.id"
        :class="[
          'rounded-xl border p-4 transition',
          alert.isRead ? 'border-border bg-surface' : 'border-l-4 bg-white',
          !alert.isRead && alert.severity === 'CRITICAL' ? 'border-l-red-500'  : '',
          !alert.isRead && alert.severity === 'WARNING'  ? 'border-l-amber-500': '',
          !alert.isRead && alert.severity === 'INFO'     ? 'border-l-blue-400' : '',
        ]"
      >
        <div class="flex items-start justify-between gap-4">
          <div class="flex items-center gap-3">
            <!-- Icône sévérité -->
            <span
              :class="[
                'inline-flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full text-sm font-bold',
                alert.severity === 'CRITICAL' ? 'bg-red-100 text-red-600'   : '',
                alert.severity === 'WARNING'  ? 'bg-amber-100 text-amber-600': '',
                alert.severity === 'INFO'     ? 'bg-blue-100 text-blue-500'  : '',
              ]"
            >
              {{ alert.severity === 'CRITICAL' ? '!' : alert.severity === 'WARNING' ? '⚠' : 'i' }}
            </span>
            <div>
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-text-main">{{ alert.message }}</span>
                <span
                  v-if="!alert.isRead"
                  class="inline-block h-2 w-2 rounded-full bg-primary"
                />
              </div>
              <div class="mt-0.5 text-xs text-text-secondary">
                {{ alert.warehouseName }}
                <span v-if="alert.zoneName"> · {{ alert.zoneName }}</span>
                · {{ formatDate(alert.createdAt) }}
              </div>
            </div>
          </div>

          <div class="flex flex-shrink-0 gap-2">
            <BaseButton
              v-if="!alert.isRead"
              size="sm"
              variant="secondary"
              @click="$emit('markRead', alert)"
            >
              Lu
            </BaseButton>
            <BaseButton
              v-if="!alert.isResolved"
              size="sm"
              variant="secondary"
              @click="$emit('resolve', alert)"
            >
              Résoudre
            </BaseButton>
            <span
              v-if="alert.isResolved"
              class="py-1 px-2 text-xs font-medium text-green-600"
            >
              ✓ Résolue
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="mt-4 flex items-center justify-between">
      <span class="text-sm text-text-secondary">{{ totalElements }} alerte(s)</span>
      <div class="flex gap-2">
        <BaseButton
          size="sm"
          variant="secondary"
          :disabled="page === 0"
          @click="$emit('prev')"
        >
          Précédent
        </BaseButton>
        <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
        <BaseButton
          size="sm"
          variant="secondary"
          :disabled="page + 1 >= totalPages"
          @click="$emit('next')"
        >
          Suivant
        </BaseButton>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import type { AlertResponse } from '@/types/alert.types'
import BaseCard   from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

defineProps<{
  alerts:        AlertResponse[]
  loading:       boolean
  page:          number
  totalPages:    number
  totalElements: number
}>()

defineEmits<{
  prev:     []
  next:     []
  markRead: [alert: AlertResponse]
  resolve:  [alert: AlertResponse]
}>()

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleString('fr-FR', {
    day:    '2-digit',
    month:  '2-digit',
    year:   'numeric',
    hour:   '2-digit',
    minute: '2-digit',
  })
}
</script>
