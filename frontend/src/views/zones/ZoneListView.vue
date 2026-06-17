<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'

const authStore = useAuthStore()

const canCreate = computed(() => authStore.hasPermission(PERMISSIONS.ZONE_CREATE))
const canUpdate = computed(() => authStore.hasPermission(PERMISSIONS.ZONE_UPDATE))
</script>

<template>
  <div>
    <div class="sm:flex sm:items-center mb-6">
      <div class="sm:flex-auto">
        <h1 class="text-2xl font-semibold text-gray-900">Zones de Stockage</h1>
        <p class="mt-2 text-sm text-gray-700">Gérer les zones des entrepôts</p>
      </div>
      <div class="mt-4 sm:ml-16 sm:mt-0 sm:flex-none">
        <button
          v-if="canCreate"
          type="button"
          class="block rounded-md bg-blue-600 px-3 py-2 text-center text-sm font-semibold text-white shadow-sm hover:bg-blue-500"
        >
          Créer une zone
        </button>
      </div>
    </div>

    <div class="bg-white shadow rounded-lg p-6">
      <div class="text-center py-12">
        <p class="text-gray-500">Module en développement</p>
        <p class="text-sm text-gray-400 mt-2">Permissions : Créer ({{ canCreate }}) / Modifier ({{ canUpdate }})</p>
      </div>
    </div>
  </div>
</template>
