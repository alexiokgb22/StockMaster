<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
    <div class="w-full max-w-lg rounded-[32px] bg-white p-6 shadow-card">
      <div class="text-xl font-semibold text-primary">{{ title }}</div>
      <p class="mt-3 text-sm text-text-secondary">{{ message }}</p>
      <div class="mt-6 flex flex-wrap gap-3">
        <button @click="$emit('confirm')" class="rounded-2xl bg-primary px-5 py-3 text-sm font-semibold text-white hover:bg-[#0a2d55]">Confirmer</button>
        <button @click="$emit('cancel')" class="rounded-2xl border border-border bg-surface px-5 py-3 text-sm font-semibold text-text-secondary hover:border-primary hover:text-primary">Annuler</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  title: string
  message: string
  confirmButton?: string
  cancelButton?: string
}>()
</script>
