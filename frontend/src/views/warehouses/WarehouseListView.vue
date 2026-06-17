<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'

const authStore = useAuthStore()

const canCreate = computed(() => authStore.hasPermission(PERMISSIONS.WAREHOUSE_CREATE))
const canUpdate = computed(() => authStore.hasPermission(PERMISSIONS.WAREHOUSE_UPDATE))
const canDisable = computed(() => authStore.hasPermission(PERMISSIONS.WAREHOUSE_DISABLE))
</script>

<template>
  <div>
    <div class="sm:flex sm:items-center mb-6">
      <div class="sm:flex-auto">
        <h1 class="text-2xl font-semibold text-gray-900">Entrepôts</h1>
        <p class="mt-2 text-sm text-gray-700">Liste de tous les entrepôts du système</p>
      </div>
      <div class="mt-4 sm:ml-16 sm:mt-0 sm:flex-none">
        <button
          v-if="canCreate"
          type="button"
          class="block rounded-md bg-blue-600 px-3 py-2 text-center text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
        >
          Créer un entrepôt
        </button>
      </div>
    </div>

    <div class="bg-white shadow rounded-lg p-6">
      <div class="text-center py-12">
        <svg
          class="mx-auto h-12 w-12 text-gray-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"
          />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">Aucun entrepôt</h3>
        <p class="mt-1 text-sm text-gray-500">La fonctionnalité sera implémentée prochainement</p>
        <div class="mt-6">
          <p class="text-sm text-gray-600">
            <strong>Permissions disponibles :</strong>
          </p>
          <ul class="mt-2 text-sm text-gray-500">
            <li v-if="canCreate">✓ Créer un entrepôt</li>
            <li v-if="canUpdate">✓ Modifier un entrepôt</li>
            <li v-if="canDisable">✓ Désactiver un entrepôt</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
