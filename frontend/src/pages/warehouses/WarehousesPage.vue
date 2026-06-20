<template>
  <div class="min-h-screen bg-gray-50 px-6 py-8">
    <PageHeader
      title="Entrepôts"
      description="Gérez vos entrepôts et leur capacité de stockage"
    />

    <!-- Barre d'action -->
    <div class="mt-6 flex flex-wrap items-center gap-3">
      <BaseInput
        v-model="search"
        placeholder="Rechercher par nom ou ville…"
        class="w-full max-w-xs"
      />

      <!-- Filtres statut -->
      <div class="flex gap-1.5 rounded-lg border border-border bg-white p-1">
        <button
          v-for="f in filters"
          :key="String(f.value)"
          @click="filterActive = f.value"
          class="rounded-md px-3 py-1 text-sm font-medium transition-colors"
          :class="filterActive === f.value
            ? 'bg-primary text-white shadow-sm'
            : 'text-text-secondary hover:text-text-main'"
        >
          {{ f.label }}
        </button>
      </div>

      <div class="ml-auto">
        <BaseButton v-if="canCreate" @click="showCreateModal = true">
          + Nouvel entrepôt
        </BaseButton>
      </div>
    </div>

    <!-- Compteur -->
    <p class="mt-4 text-sm text-text-secondary">
      <template v-if="!warehouseStore.loading">
        {{ warehouseStore.warehouses.length }} entrepôt{{ warehouseStore.warehouses.length !== 1 ? 's' : '' }}
      </template>
    </p>

    <!-- Erreur -->
    <div v-if="error" class="mt-4 rounded-lg bg-red-50 p-4 text-sm text-red-700">
      {{ error }}
    </div>

    <!-- Skeleton -->
    <div v-if="warehouseStore.loading && warehouseStore.warehouses.length === 0" class="mt-6">
      <SkeletonLoader :count="6" />
    </div>

    <!-- État vide -->
    <div v-else-if="warehouseStore.warehouses.length === 0" class="mt-10">
      <EmptyState
        title="Aucun entrepôt"
        description="Commencez par créer un nouvel entrepôt."
      />
    </div>

    <!-- Grille de cartes -->
    <div
      v-else
      class="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-3"
    >
      <WarehouseCard
        v-for="warehouse in warehouseStore.warehouses"
        :key="warehouse.id"
        :warehouse="warehouse"
        :can-update="canUpdate"
        :can-disable="canDisable"
        @edit="editWarehouse"
        @toggle="openToggleConfirmation"
      />
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
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import SkeletonLoader from '@/components/ui/SkeletonLoader.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ConfirmationDialog from '@/components/ui/ConfirmationDialog.vue'
import WarehouseFormModal from '@/components/warehouse/WarehouseFormModal.vue'
import WarehouseCard from '@/components/warehouse/WarehouseCard.vue'
import type { WarehouseResponse } from '@/types/warehouse.types'

const warehouseStore = useWarehouseStore()
const { hasPermission } = usePermissions()

const search           = ref('')
const filterActive     = ref<boolean | null>(null)
const showCreateModal  = ref(false)
const editingWarehouse = ref<WarehouseResponse | null>(null)
const toggleWarehouseId = ref<number | null>(null)
const toggleWarehouse   = ref<WarehouseResponse | null>(null)

const canCreate  = computed(() => hasPermission('warehouse.create'))
const canUpdate  = computed(() => hasPermission('warehouse.update'))
const canDisable = computed(() => hasPermission('warehouse.disable'))
const error      = computed(() => warehouseStore.error)

const filters = [
  { label: 'Tous',    value: null  },
  { label: 'Actifs',  value: true  },
  { label: 'Inactifs', value: false },
] as const

const load = () => {
  warehouseStore.fetchWarehouses({
    search: search.value || undefined,
    active: filterActive.value ?? undefined,
  })
}

const editWarehouse = (warehouse: WarehouseResponse) => {
  editingWarehouse.value = warehouse
}

const openToggleConfirmation = (warehouse: WarehouseResponse) => {
  toggleWarehouse.value   = warehouse
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
  await load()
}

const handleUpdate = async () => {
  editingWarehouse.value = null
  await load()
}

watch([search, filterActive], load)
onMounted(load)
</script>
