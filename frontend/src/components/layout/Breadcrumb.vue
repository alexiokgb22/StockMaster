<template>
  <div class="mb-6 flex flex-wrap items-center gap-2 text-sm text-text-secondary">
    <RouterLink to="/" class="hover:text-primary">Accueil</RouterLink>
    <span>›</span>
    <span>{{ label }}</span>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'

const props = defineProps<{
  label: string
}>()
</script>
