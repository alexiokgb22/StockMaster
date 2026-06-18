<template>
  <div class="min-h-screen bg-background text-text-main">

    <!-- ── Layout minimal : changement de mot de passe obligatoire ── -->
    <template v-if="mustChangePassword">
      <div class="flex min-h-screen items-center justify-center bg-background px-4">
        <div class="w-full max-w-md">
          <!-- Mini header contextuel -->
          <div class="mb-8 text-center">
            <div class="text-2xl font-semibold text-primary">StockMaster</div>
            <div class="mt-1 text-sm text-text-secondary">Connecté en tant que {{ currentUser?.username }}</div>
          </div>
          <router-view />
        </div>
      </div>
    </template>

    <!-- ── Layout normal ── -->
    <template v-else>
      <div class="flex min-h-screen">
        <AppSidebar />
        <div class="flex-1 flex flex-col">
          <AppHeader />
          <main class="flex-1 p-6 xl:p-8">
            <router-view />
          </main>
        </div>
      </div>
    </template>

    <ToastContainer />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'

const authStore = useAuthStore()
const currentUser = computed(() => authStore.currentUser)
const mustChangePassword = computed(() => authStore.currentUser?.mustChangePassword === true)
</script>
