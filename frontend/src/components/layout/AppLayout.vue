<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'

const authStore = useAuthStore()
const router = useRouter()
const sidebarOpen = ref(false)

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}
</script>

<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Sidebar -->
    <Sidebar :open="sidebarOpen" @close="sidebarOpen = false" />

    <!-- Main Content -->
    <div class="lg:pl-64 flex flex-col flex-1">
      <!-- Navbar -->
      <Navbar @toggle-sidebar="toggleSidebar" @logout="handleLogout" />

      <!-- Page Content -->
      <main class="flex-1">
        <div class="py-6">
          <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <router-view />
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
