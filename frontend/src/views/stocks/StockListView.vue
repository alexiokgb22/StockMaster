<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'

const authStore = useAuthStore()
const canUpdate = computed(() => authStore.hasPermission(PERMISSIONS.STOCK_UPDATE))
const canViewHistory = computed(() => authStore.hasPermission(PERMISSIONS.STOCK_VIEW_HISTORY))
</script>

<template>
  <div>
    <div class="sm:flex sm:items-center mb-6">
      <div class="sm:flex-auto">
        <h1 class="text-2xl font-semibold text-gray-900">Gestion des Stocks</h1>
        <p class="mt-2 text-sm text-gray-700">Visualiser et ajuster les stocks</p>
      </div>
    </div>
    <div class="bg-white shadow rounded-lg p-6">
      <p class="text-gray-500 text-center py-12">Module en développement</p>
      <p class="text-sm text-gray-400 text-center">
        Ajustement: {{ canUpdate }} / Historique: {{ canViewHistory }}
      </p>
    </div>
  </div>
</template>
