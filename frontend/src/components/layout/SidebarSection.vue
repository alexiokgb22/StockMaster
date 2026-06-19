<template>
  <div>
    <div class="text-xs uppercase tracking-[0.2em] text-white/50 mb-3 font-semibold">{{ label }}</div>
    <div class="space-y-1">
      <RouterLink
        v-for="item in items"
        :key="item.title"
        :to="item.to"
        class="group flex items-center justify-between rounded-xl px-3 py-2.5 text-sm font-medium text-white/70 transition-all hover:bg-white/10 hover:text-white"
        active-class="!bg-accent !text-primary font-semibold shadow-lg"
      >
        <span>{{ item.title }}</span>
        <span v-if="item.comingSoon" class="text-[10px] uppercase tracking-[0.2em] text-accent/80 font-bold">À venir</span>
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
