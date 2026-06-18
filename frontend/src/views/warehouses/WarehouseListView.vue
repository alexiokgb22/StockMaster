<template>
  <div class="min-h-screen bg-gray-50 px-6 py-8">
    <PageHeader
      title="Entrepôts"
      description="Gérez vos entrepôts et leur capacité de stockage"
    />

    <!-- Barre d'action -->
    <div class="mt-6 flex gap-4">
      <div class="flex-1">
        <Input
          v-model="search"
          placeholder="Rechercher par nom ou ville..."
          @update:model-value="handleSearch"
        />
      </div>
      <Button
        v-if="canCreate"
        @click="showCreateModal = true"
        class="px-4 py-2"
      >
        + Nouvel entrepôt
      </Button>
    </div>

    <!-- Filtres -->
    <div class="mt-4 flex gap-2">
      <button
        @click="filterActive = null"
        :class="[
          'px-3 py-1 rounded text-sm font-medium',
          filterActive === null
            ? 'bg-blue-600 text-white'
            : 'bg-gray-200 text-gray-700'
        ]"
      >
        Tous
      </button>
      <button
        @click="filterActive = true"
        :class="[
          'px-3 py-1 rounded text-sm font-medium',
          filterActive === true
            ? 'bg-blue-600 text-white'
            : 'bg-gray-200 text-gray-700'
        ]"
      >
        Actifs
      </button>
      <button
        @click="filterActive = false"
        :class="[
          'px-3 py-1 rounded text-sm font-medium',
          filterActive === false
            ? 'bg-blue-600 text-white'
            : 'bg-gray-200 text-gray-700'
        ]"
      >
        Inactifs
      </button>
    </div>

    <!-- Erreur -->
    <div v-if="error" class="mt-4 rounded bg-red-50 p-4 text-red-700">
      {{ error }}
    </div>

    <!-- Tableau -->
    <div class="mt-6 overflow-hidden rounded-lg border border-gray-200 bg-white">
      <div v-if="warehouseStore.loading && warehouseStore.warehouses.length === 0" class="p-6">
        <SkeletonLoader :count="5" />
      </div>
      <div v-else-if="warehouseStore.warehouses.length === 0" class="p-6">
        <EmptyState
          title="Aucun entrepôt"
          description="Commencez par créer un nouvel entrepôt"
        />
      </div>
      <table v-else class="w-full">
        <thead class="bg-gray-50 border-b">
          <tr>
            <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">
              Nom
            </th>
            <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">
              Ville
            </th>
            <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">
              Gestionnaire
            </th>
            <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">
              Capacité
            </th>
            <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">
              État
            </th>
            <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">
              Actions
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="warehouse in warehouseStore.warehouses"
            :key="warehouse.id"
            class="border-b hover:bg-gray-50"
          >
            <td class="px-6 py-4 text-sm text-gray-900">
              <router-link
                :to="`/warehouses/${warehouse.id}`"
                class="font-medium text-blue-600 hover:underline"
              >
                {{ warehouse.name }}
              </router-link>
            </td>
            <td class="px-6 py-4 text-sm text-gray-700">
              {{ warehouse.city }}
            </td>
            <td class="px-6 py-4 text-sm text-gray-700">
              {{ warehouse.managerName || '-' }}
            </td>
            <td class="px-6 py-4 text-sm text-gray-700">
              <div class="flex items-center gap-2">
                <div class="flex-1 bg-gray-200 rounded-full h-2">
                  <div
                    class="bg-blue-600 h-2 rounded-full"
                    :style="{ width: capacityPercent(warehouse) + '%' }"
                  />
                </div>
                <span class="text-xs text-gray-600">
                  {{ (capacityPercent(warehouse) || 0).toFixed(0) }}%
                </span>
              </div>
            </td>
            <td class="px-6 py-4 text-sm">
              <Badge
                :variant="warehouse.isActive ? 'success' : 'secondary'"
                :text="warehouse.isActive ? 'Actif' : 'Inactif'"
              />
            </td>
            <td class="px-6 py-4 text-sm">
              <div class="flex gap-2">
                <Button
                  v-if="canUpdate"
                  @click="editWarehouse(warehouse)"
                  class="px-3 py-1 text-xs"
                  variant="secondary"
                >
                  Éditer
                </Button>
                <Button
                  v-if="canDisable"
                  @click="openToggleConfirmation(warehouse)"
                  :variant="warehouse.isActive ? 'danger' : 'secondary'"
                  class="px-3 py-1 text-xs"
                >
                  {{ warehouse.isActive ? 'Désactiver' : 'Activer' }}
                </Button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modales -->
    <WarehouseFormModal
      v-if="showCreateModal"
      :warehouse="null"
      @create="handleCreate"
      @close="showCreateModal = false"
    />

    <WarehouseFormModal
      v-if="editingWarehouse"
      :warehouse="editingWarehouse"
      @update="handleUpdate"
      @close="editingWarehouse = null"
    />

    <ConfirmationDialog
      v-if="toggleWarehouseId"
      title="Confirmation"
      :message="`Êtes-vous sûr de vouloir ${toggleWarehouse?.isActive ? 'désactiver' : 'activer'} cet entrepôt ?`"
      confirm-button="Confirmer"
      cancel-button="Annuler"
      @confirm="handleToggle"
      @cancel="toggleWarehouseId = null"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useWarehouseStore } from '@/stores/warehouse'
import { usePermissions } from '@/composables/usePermissions'
import PageHeader from '@/components/ui/PageHeader.vue'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import SkeletonLoader from '@/components/ui/SkeletonLoader.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ConfirmationDialog from '@/components/ui/ConfirmationDialog.vue'
import WarehouseFormModal from '@/components/warehouse/WarehouseFormModal.vue'
import type { WarehouseResponse } from '@/types/warehouse.types'

const warehouseStore = useWarehouseStore()
const { hasPermission } = usePermissions()

const search = ref('')
const filterActive = ref<boolean | null>(null)
const showCreateModal = ref(false)
const editingWarehouse = ref<WarehouseResponse | null>(null)
const toggleWarehouseId = ref<number | null>(null)
const toggleWarehouse = ref<WarehouseResponse | null>(null)

const canCreate = computed(() => hasPermission('warehouse.create'))
const canUpdate = computed(() => hasPermission('warehouse.update'))
const canDisable = computed(() => hasPermission('warehouse.disable'))

const error = computed(() => warehouseStore.error)

const handleSearch = () => {
  warehouseStore.fetchWarehouses({
    search: search.value || undefined,
    active: filterActive.value || undefined
  })
}

const editWarehouse = (warehouse: WarehouseResponse) => {
  editingWarehouse.value = warehouse
}

const openToggleConfirmation = (warehouse: WarehouseResponse) => {
  toggleWarehouse.value = warehouse
  toggleWarehouseId.value = warehouse.id
}

const handleToggle = async () => {
  if (toggleWarehouseId.value) {
    await warehouseStore.toggleWarehouse(toggleWarehouseId.value)
    toggleWarehouseId.value = null
  }
}

const handleCreate = async () => {
  showCreateModal.value = false
  await warehouseStore.fetchWarehouses()
}

const handleUpdate = async () => {
  editingWarehouse.value = null
  await warehouseStore.fetchWarehouses()
}

const capacityPercent = (warehouse: WarehouseResponse) => {
  if (warehouse.totalCapacity === 0) return 0
  return (warehouse.usedCapacity / warehouse.totalCapacity) * 100
}

watch([search, filterActive], () => {
  handleSearch()
})

onMounted(() => {
  warehouseStore.fetchWarehouses()
})
</script>
