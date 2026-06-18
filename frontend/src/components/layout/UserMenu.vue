<template>
  <div class="relative">
    <button @click="toggle" class="inline-flex items-center gap-2 rounded-full border border-border bg-surface px-4 py-2 text-sm text-text-secondary transition hover:border-primary hover:text-primary">
      <span>{{ currentUser?.username ?? 'Utilisateur' }}</span>
      <span class="inline-flex h-2.5 w-2.5 rounded-full bg-accent"></span>
    </button>

    <div v-if="open" class="absolute right-0 z-20 mt-2 w-48 rounded-3xl border border-border bg-white p-3 shadow-card">
      <button @click="handleLogout" class="w-full rounded-2xl px-3 py-2 text-left text-sm text-text-secondary transition hover:bg-primary-light hover:text-primary">
        Se déconnecter
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { currentUser, logout } = useAuth()
const open = ref(false)

const toggle = () => {
  open.value = !open.value
}

const handleLogout = async () => {
  await logout()
  router.push({ name: 'Login' })
}
</script>
