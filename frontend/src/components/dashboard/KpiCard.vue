<template>
  <div
    :class="[
      'rounded-2xl border p-5 flex items-center justify-between gap-4',
      value > 0 ? borderColor : 'border-border bg-surface',
    ]"
  >
    <div>
      <div class="text-sm text-text-secondary">{{ label }}</div>
      <div
        :class="[
          'mt-1 text-3xl font-bold',
          value > 0 ? textColor : 'text-text-main',
        ]"
      >
        {{ value }}
      </div>
      <button
        v-if="action && value > 0"
        @click="$emit('action')"
        class="mt-2 text-xs underline text-text-secondary hover:text-primary"
      >
        {{ action }}
      </button>
    </div>
    <span class="text-3xl select-none">{{ icon }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label: string
  value: number
  icon?: string
  color?: 'red' | 'amber' | 'blue' | 'purple' | 'teal' | 'green'
  action?: string
}>()

defineEmits<{ action: [] }>()

const borderColor = computed(() => ({
  red:    'border-red-200 bg-red-50',
  amber:  'border-amber-200 bg-amber-50',
  blue:   'border-blue-200 bg-blue-50',
  purple: 'border-purple-200 bg-purple-50',
  teal:   'border-teal-200 bg-teal-50',
  green:  'border-green-200 bg-green-50',
}[props.color ?? 'blue'] ?? 'border-border bg-surface'))

const textColor = computed(() => ({
  red:    'text-red-600',
  amber:  'text-amber-600',
  blue:   'text-blue-600',
  purple: 'text-purple-600',
  teal:   'text-teal-600',
  green:  'text-green-600',
}[props.color ?? 'blue'] ?? 'text-text-main'))
</script>
