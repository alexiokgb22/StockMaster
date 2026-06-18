<template>
  <div class="rounded-[32px] border border-border bg-white p-10 text-center shadow-card">
    <div class="text-3xl font-semibold text-primary">{{ title }}</div>
    <p class="mt-3 text-sm text-text-secondary">{{ description }}</p>
    <div v-if="$slots.default" class="mt-6"><slot /></div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  title: string
  description: string
}>()
</script>
