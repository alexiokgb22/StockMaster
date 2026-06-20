<template>
  <div
    class="group relative flex flex-col rounded-2xl border bg-white shadow-sm transition-all duration-200 hover:shadow-md"
    :class="warehouse.isActive ? 'border-border' : 'border-border opacity-70'"
  >
    <!-- Bande colorée en haut selon l'état -->
    <div
      class="h-1 w-full rounded-t-2xl"
      :class="warehouse.isActive ? 'bg-primary' : 'bg-gray-300'"
    />

    <div class="flex flex-col gap-4 p-5">

      <!-- En-tête : nom + badge statut -->
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0">
          <router-link
            :to="{ name: 'WarehouseDetail', params: { id: warehouse.id } }"
            class="block truncate text-base font-semibold text-text-main hover:text-primary transition-colors"
          >
            {{ warehouse.name }}
          </router-link>
          <p class="mt-0.5 flex items-center gap-1 text-sm text-text-secondary">
            <!-- Icône localisation -->
            <svg class="h-3.5 w-3.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            {{ warehouse.city }}
          </p>
        </div>
        <StatusBadge
          :label="warehouse.isActive ? 'Actif' : 'Inactif'"
          :variant="warehouse.isActive ? 'success' : undefined"
          class="flex-shrink-0"
        />
      </div>

      <!-- Séparateur -->
      <div class="border-t border-border" />

      <!-- Infos intermédiaires -->
      <div class="grid grid-cols-2 gap-3 text-sm">
        <!-- Gestionnaire -->
        <div>
          <p class="text-xs font-medium uppercase tracking-wide text-text-secondary">Gestionnaire</p>
          <p class="mt-0.5 truncate font-medium text-text-main">
            {{ warehouse.managerName || '—' }}
          </p>
        </div>
        <!-- Adresse -->
        <div>
          <p class="text-xs font-medium uppercase tracking-wide text-text-secondary">Adresse</p>
          <p class="mt-0.5 truncate text-text-main">
            {{ warehouse.address || '—' }}
          </p>
        </div>
      </div>

      <!-- Barre de capacité -->
      <div>
        <div class="mb-1.5 flex items-center justify-between text-xs">
          <span class="font-medium text-text-secondary">Capacité utilisée</span>
          <span class="font-semibold" :class="capacityColor">
            {{ capacityPercent }}%
          </span>
        </div>
        <div class="h-2 w-full overflow-hidden rounded-full bg-gray-100">
          <div
            class="h-full rounded-full transition-all duration-500"
            :class="capacityBarColor"
            :style="{ width: capacityPercent + '%' }"
          />
        </div>
        <p class="mt-1 text-xs text-text-secondary">
          {{ warehouse.usedCapacity ?? 0 }} / {{ warehouse.totalCapacity ?? 0 }} m³
        </p>
      </div>

      <!-- Séparateur -->
      <div class="border-t border-border" />

      <!-- Actions -->
      <div class="flex items-center gap-2">
        <router-link
          :to="{ name: 'WarehouseDetail', params: { id: warehouse.id } }"
          class="flex-1"
        >
          <BaseButton variant="secondary" class="w-full justify-center text-sm">
            Voir le détail
          </BaseButton>
        </router-link>
        <BaseButton
          v-if="canUpdate"
          variant="secondary"
          class="text-sm"
          @click.prevent="$emit('edit', warehouse)"
        >
          Éditer
        </BaseButton>
        <BaseButton
          v-if="canDisable"
          :variant="warehouse.isActive ? 'danger' : 'secondary'"
          class="text-sm"
          @click.prevent="$emit('toggle', warehouse)"
        >
          {{ warehouse.isActive ? 'Désactiver' : 'Activer' }}
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import type { WarehouseResponse } from '@/types/warehouse.types'

const props = defineProps<{
  warehouse: WarehouseResponse
  canUpdate?: boolean
  canDisable?: boolean
}>()

defineEmits<{
  edit:   [warehouse: WarehouseResponse]
  toggle: [warehouse: WarehouseResponse]
}>()

const capacityPercent = computed(() => {
  if (!props.warehouse.totalCapacity) return 0
  return Math.min(
    Math.round((props.warehouse.usedCapacity / props.warehouse.totalCapacity) * 100),
    100,
  )
})

// Couleur de la barre selon le taux d'occupation
const capacityBarColor = computed(() => {
  const p = capacityPercent.value
  if (p >= 90) return 'bg-red-500'
  if (p >= 70) return 'bg-amber-400'
  return 'bg-primary'
})

const capacityColor = computed(() => {
  const p = capacityPercent.value
  if (p >= 90) return 'text-red-600'
  if (p >= 70) return 'text-amber-600'
  return 'text-primary'
})
</script>
