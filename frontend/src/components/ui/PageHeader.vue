<template>
  <div class="mb-8 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
    <div>
      <div class="text-sm uppercase tracking-[0.22em] text-text-secondary/80">{{ subtitle }}</div>
      <h1 class="text-3xl font-semibold text-primary">{{ title }}</h1>
      <p v-if="description" class="mt-2 text-sm text-text-secondary">{{ description }}</p>
    </div>
    <div class="flex items-center gap-3"><slot /></div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  title: string
  subtitle?: string
  description?: string
}>()
</script>
