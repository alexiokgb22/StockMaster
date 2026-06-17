<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const emit = defineEmits<{
  toggleSidebar: []
  logout: []
}>()

const authStore = useAuthStore()

const userName = computed(() => authStore.user?.username || '')
const userRole = computed(() => authStore.user?.role || '')
</script>

<template>
  <div class="sticky top-0 z-40 flex h-16 shrink-0 items-center gap-x-4 border-b border-gray-200 bg-white px-4 shadow-sm sm:gap-x-6 sm:px-6 lg:px-8">
    <!-- Mobile menu button -->
    <button
      type="button"
      class="-m-2.5 p-2.5 text-gray-700 lg:hidden"
      @click="emit('toggleSidebar')"
    >
      <span class="sr-only">Ouvrir le menu</span>
      <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
      </svg>
    </button>

    <div class="flex flex-1 gap-x-4 self-stretch lg:gap-x-6">
      <div class="flex flex-1"></div>

      <!-- User menu -->
      <div class="flex items-center gap-x-4 lg:gap-x-6">
        <div class="hidden lg:block lg:h-6 lg:w-px lg:bg-gray-200" aria-hidden="true" />

        <div class="flex items-center gap-x-4">
          <div class="text-sm">
            <div class="font-medium text-gray-700">{{ userName }}</div>
            <div class="text-gray-500">{{ userRole }}</div>
          </div>

          <button
            @click="emit('logout')"
            class="rounded-md bg-red-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-red-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600"
          >
            Déconnexion
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
