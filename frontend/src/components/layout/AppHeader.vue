<template>
  <header class="flex items-center justify-between gap-4 border-b border-border bg-white px-6 py-4 shadow-sm sticky top-0 z-10">
    <div>
      <div class="text-sm text-text-secondary">Bienvenue</div>
      <div class="text-lg font-semibold text-primary">Tableau de bord StockMaster</div>
    </div>

    <div class="flex items-center gap-4">
      <button class="rounded-full border border-border bg-surface px-4 py-2 text-sm text-text-secondary transition hover:border-primary hover:text-primary">
        Notifier
      </button>
      <UserMenu />
    </div>
  </header>
</template>

<script setup lang="ts">
import UserMenu from '@/components/layout/UserMenu.vue'
</script>
