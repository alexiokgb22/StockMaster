<template>
  <div class="min-h-[70vh] flex flex-col items-center justify-center px-6">
    <div class="rounded-[32px] border border-border bg-white p-12 text-center shadow-card">
      <h1 class="text-4xl font-semibold text-primary">404</h1>
      <p class="mt-4 text-sm text-text-secondary">La page demandée est introuvable.</p>
      <RouterLink to="/" class="mt-6 inline-flex rounded-2xl bg-primary px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#0a2d55]">Retour à l’accueil</RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>
