<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'

const authStore = useAuthStore()
const canCreate = computed(() => authStore.hasPermission(PERMISSIONS.SUPPLIER_CREATE))
</script>

<template>
  <div>
    <div class="sm:flex sm:items-center mb-6">
      <div class="sm:flex-auto">
        <h1 class="text-2xl font-semibold text-gray-900">Fournisseurs</h1>
      </div>
      <button v-if="canCreate" class="btn-primary">Ajouter un fournisseur</button>
    </div>
    <div class="bg-white shadow rounded-lg p-6">
      <p class="text-gray-500 text-center py-12">Module en développement</p>
    </div>
  </div>
</template>
