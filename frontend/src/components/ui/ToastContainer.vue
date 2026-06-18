<template>
  <div class="fixed bottom-6 right-6 z-50 flex flex-col gap-3">
    <div v-for="toast in messages" :key="toast.id" class="rounded-3xl border border-border bg-white p-4 shadow-card w-80">
      <div class="flex items-start justify-between gap-4">
        <div>
          <div class="text-sm font-semibold text-text-main">{{ toast.title }}</div>
          <p class="mt-1 text-sm text-text-secondary">{{ toast.message }}</p>
        </div>
        <button @click="remove(toast.id)" class="rounded-full px-2 py-1 text-xs text-text-secondary hover:text-primary">×</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useToastStore } from '@/stores/toast'

const toastStore = useToastStore()
const { messages } = storeToRefs(toastStore)

const remove = (id: string) => {
  toastStore.remove(id)
}
</script>
