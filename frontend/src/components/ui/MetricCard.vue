<template>
  <div class="rounded-[32px] border border-border bg-white p-6 shadow-card">
    <div class="text-sm font-semibold uppercase tracking-[0.2em] text-text-secondary">{{ label }}</div>
    <div class="mt-4 text-3xl font-semibold text-primary">{{ value }}</div>
    <p v-if="description" class="mt-2 text-sm text-text-secondary">{{ description }}</p>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  label: string
  value: string | number
  description?: string
}>()
</script>
