<template>
  <div class="space-y-6">
    <PageHeader title="Inventaires" subtitle="Comptage physique et ajustement du stock">
      <BaseButton
        v-if="isManager && !hasActiveInventory"
        @click="showCreateModal = true"
      >
        + Nouvel inventaire
      </BaseButton>
      <span
        v-if="isManager && hasActiveInventory"
        class="text-sm text-amber-600 font-medium"
      >
        Un inventaire est en cours
      </span>
    </PageHeader>

    <!-- Inventaire actif (en cours) — affiché en priorité -->
    <template v-if="activeInventory">
      <InventoryDetail
        :inventory="activeInventory"
        :warehouse-id="warehouseId"
        :is-manager="isManager"
        :is-storekeeper="isStorekeeper"
        @refreshed="onInventoryUpdated"
      />
    </template>

    <!-- Historique -->
    <BaseCard>
      <div class="mb-4 flex items-center justify-between">
        <h2 class="text-sm font-semibold text-text-main">Historique des inventaires</h2>
        <select
          v-model="statusFilter"
          @change="fetchInventories"
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option value="">Tous</option>
          <option value="COMPLETED">Clôturés</option>
          <option value="CANCELLED">Annulés</option>
        </select>
      </div>

      <div v-if="loading" class="py-6 text-center text-sm text-text-secondary">Chargement…</div>

      <EmptyState
        v-else-if="pastInventories.length === 0"
        title="Aucun inventaire"
        description="Les inventaires clôturés ou annulés apparaîtront ici."
      />

      <div v-else class="space-y-2">
        <div
          v-for="inv in pastInventories"
          :key="inv.id"
          class="flex items-center justify-between rounded-xl border border-border bg-surface px-4 py-3 cursor-pointer hover:bg-primary/5 transition"
          @click="selectedInventory = inv"
        >
          <div class="flex items-center gap-3">
            <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">
              {{ inv.inventoryNumber }}
            </code>
            <StatusBadge :label="statusLabel(inv.inventoryStatus)" :variant="statusVariant(inv.inventoryStatus)" />
            <span class="text-xs text-text-secondary">
              {{ inv.inventoryType === 'FULL' ? 'Complet' : 'Partiel' }}
            </span>
          </div>
          <div class="flex items-center gap-4 text-sm text-text-secondary">
            <span>{{ inv.totalLines }} lignes</span>
            <span
              v-if="inv.gapLines > 0"
              class="text-amber-600 font-medium"
            >
              {{ inv.gapLines }} écart(s)
            </span>
            <span>{{ new Date(inv.createdAt).toLocaleDateString('fr-FR') }}</span>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="mt-4 flex items-center justify-between">
        <span class="text-sm text-text-secondary">{{ totalElements }} inventaire(s)</span>
        <div class="flex gap-2">
          <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetchInventories()">
            Précédent
          </BaseButton>
          <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
          <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetchInventories()">
            Suivant
          </BaseButton>
        </div>
      </div>
    </BaseCard>

    <!-- Modales -->
    <InventoryCreateModal
      v-if="showCreateModal"
      :warehouse-id="warehouseId"
      @close="showCreateModal = false"
      @saved="onCreated"
    />

    <!-- Détail inventaire passé (lecture seule) -->
    <InventoryDetailModal
      v-if="selectedInventory"
      :inventory="selectedInventory"
      @close="selectedInventory = null"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { inventoryService } from '@/services/inventory.service'
import { useToastStore } from '@/stores/toast'
import type { InventoryResponse, InventoryStatus } from '@/types/inventory.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import InventoryCreateModal from '@/components/inventory/InventoryCreateModal.vue'
import InventoryDetail from '@/components/inventory/InventoryDetail.vue'
import InventoryDetailModal from '@/components/inventory/InventoryDetailModal.vue'

const authStore = useAuthStore()
const toast = useToastStore()

const warehouseId = computed(() => authStore.currentUser?.warehouseId!)
const isManager   = computed(() => authStore.currentUser?.role === "Gestionnaire d'entrepôt")
const isStorekeeper = computed(() => authStore.currentUser?.role === 'Magasinier')

// Inventaire en cours (affiché séparément en haut)
const activeInventory = ref<InventoryResponse | null>(null)
const hasActiveInventory = computed(() => activeInventory.value !== null)

// Historique
const pastInventories = ref<InventoryResponse[]>([])
const loading = ref(false)
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const statusFilter = ref<InventoryStatus | ''>('')

const showCreateModal = ref(false)
const selectedInventory = ref<InventoryResponse | null>(null)

const statusLabel = (s: InventoryStatus) =>
  ({ IN_PROGRESS: 'En cours', COMPLETED: 'Clôturé', CANCELLED: 'Annulé' }[s] ?? s)

const statusVariant = (s: InventoryStatus) =>
  ({ IN_PROGRESS: 'warning', COMPLETED: 'success', CANCELLED: 'secondary' }[s] ?? 'secondary') as any

async function fetchActiveInventory() {
  try {
    const res = await inventoryService.list(warehouseId.value, {
      status: 'IN_PROGRESS',
      page: 0,
      size: 1,
    })
    activeInventory.value = res.content[0] ?? null
    // Si IN_PROGRESS trouvé, charger le détail complet avec les lignes
    if (activeInventory.value) {
      activeInventory.value = await inventoryService.getById(
        warehouseId.value,
        activeInventory.value.id,
      )
    }
  } catch { /* silencieux */ }
}

async function fetchInventories() {
  loading.value = true
  try {
    const effectiveStatus = statusFilter.value || undefined
    // On exclut IN_PROGRESS car affiché séparément
    const res = await inventoryService.list(warehouseId.value, {
      status: effectiveStatus as InventoryStatus | undefined,
      page: page.value,
      size: 20,
    })
    // Filtrer les IN_PROGRESS de la liste historique
    pastInventories.value = res.content.filter(
      (i) => i.inventoryStatus !== 'IN_PROGRESS',
    )
    totalPages.value = res.totalPages
    totalElements.value = res.totalElements
  } finally {
    loading.value = false
  }
}

async function onCreated() {
  showCreateModal.value = false
  toast.success('Inventaire démarré')
  await fetchActiveInventory()
  await fetchInventories()
}

async function onInventoryUpdated(updated: InventoryResponse) {
  if (updated.inventoryStatus === 'IN_PROGRESS') {
    activeInventory.value = updated
  } else {
    // Clôturé ou annulé → retirer de l'actif
    activeInventory.value = null
    toast.success(
      updated.inventoryStatus === 'COMPLETED'
        ? `Inventaire clôturé — ${updated.gapLines} écart(s) appliqué(s)`
        : 'Inventaire annulé',
    )
    await fetchInventories()
  }
}

onMounted(async () => {
  await fetchActiveInventory()
  fetchInventories()
})
</script>
