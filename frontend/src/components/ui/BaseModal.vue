<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
    <div class="w-full max-w-2xl overflow-hidden rounded-[32px] bg-white p-6 shadow-card">
      <div class="flex items-center justify-between gap-4 pb-4">
        <div class="text-lg font-semibold text-primary">{{ title }}</div>
        <button @click="$emit('close')" class="rounded-full border border-border px-3 py-2 text-sm text-text-secondary hover:text-primary">Fermer</button>
      </div>
      <div class="space-y-4">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  title: string
  open: boolean
}>()
</script>
