<script setup lang="ts">
import type { Component } from 'vue'
interface Props {
  icon?: Component
  title: string
  description?: string
  actionText?: string
}
defineProps<Props>()
const emit = defineEmits<{ action: [] }>()
</script>

<template>
  <div class="flex flex-col items-center justify-center py-16 px-4 text-center">
    <div class="w-16 h-16 rounded-2xl bg-primary-100 flex items-center justify-center mb-4">
      <component v-if="icon" :is="icon" class="w-8 h-8 text-primary" />
      <svg v-else class="w-8 h-8 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
      </svg>
    </div>
    <h3 class="heading-4 mb-1">{{ title }}</h3>
    <p v-if="description" class="text-sm text-text-muted max-w-xs mb-6">{{ description }}</p>
    <slot name="action">
      <button v-if="actionText" @click="emit('action')"
        class="inline-flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white text-sm font-semibold rounded-lg transition-base">
        {{ actionText }}
      </button>
    </slot>
  </div>
</template>
