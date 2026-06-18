<template>
  <span :class="badgeClass" class="inline-flex rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em]">
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label: string
  variant?: 'success' | 'danger' | 'warning' | 'info'
}>()

const badgeClass = computed(() => {
  const map: Record<string, string> = {
    default: 'bg-background text-text-secondary border border-border',
    success: 'bg-[#d1fae5] text-[#065f46]',
    danger: 'bg-[#fee2e2] text-[#991b1b]',
    warning: 'bg-[#fef3c7] text-[#92400e]',
  }
  return map[props.variant] ?? map.default
})
</script>
