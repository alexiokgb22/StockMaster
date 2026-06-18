<template>
  <div>
    <div class="text-xs uppercase tracking-[0.2em] text-text-secondary mb-3">{{ label }}</div>
    <div class="space-y-1">
      <RouterLink
        v-for="item in items"
        :key="item.title"
        :to="item.to"
        class="group flex items-center justify-between rounded-2xl px-3 py-2 text-sm font-medium text-text-secondary transition hover:bg-primary-light hover:text-primary"
      >
        <span>{{ item.title }}</span>
        <span v-if="item.comingSoon" class="text-[11px] uppercase tracking-[0.2em] text-accent">À venir</span>
      </RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'

interface SidebarItem {
  title: string
  to: { name: string }
  comingSoon?: boolean
}

const props = defineProps<{
  label: string
  items: SidebarItem[]
}>()
</script>
