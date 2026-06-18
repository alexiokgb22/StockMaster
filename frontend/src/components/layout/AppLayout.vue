<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'

const authStore = useAuthStore()
const router    = useRouter()
const sidebarOpen = ref(false)

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <ToastContainer />

    <!-- Sidebar fixe gauche -->
    <Sidebar :open="sidebarOpen" @close="sidebarOpen = false" />

    <!-- Zone principale avec padding left pour desktop -->
    <div class="lg:pl-[260px] flex flex-col min-h-screen">
      <!-- Navbar sticky en haut -->
      <Navbar @toggle-sidebar="sidebarOpen = !sidebarOpen" @logout="handleLogout" />

      <!-- Contenu principal avec bon spacing -->
      <main class="flex-1 p-6 lg:p-8">
        <RouterView />
      </main>
    </div>
  </div>
</template>
