<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()

onMounted(async () => {
  // Tenter de récupérer l'utilisateur si un cookie existe
  await authStore.fetchUser()
})
</script>

<template>
  <router-view />
</template>
