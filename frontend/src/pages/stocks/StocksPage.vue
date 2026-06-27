<template>
  <div class="min-h-screen bg-gray-50 px-6 py-8">
    <PageHeader
      title="Stocks"
      subtitle="Vue consolidée des stocks selon votre profil"
    />

    <div v-if="loading" class="mt-6 rounded-2xl border border-border bg-white px-6 py-8 text-center text-sm text-text-secondary">
      Chargement des stocks…
    </div>

    <div v-else-if="error" class="mt-6 rounded-2xl border border-red-200 bg-red-50 px-6 py-4 text-sm text-red-700">
      {{ error }}
    </div>

    <!-- Vue Admin : Liste des entrepôts -->
    <div v-else-if="isAdmin">
      <!-- Si aucun entrepôt sélectionné, afficher la liste -->
      <div v-if="!selectedWarehouseId">
        <div v-if="warehouses.length === 0" class="mt-10">
          <EmptyState
            title="Aucun entrepôt"
            description="Aucun entrepôt actif disponible."
          />
        </div>

        <div v-else>
          <p class="mt-4 text-sm text-text-secondary">
            {{ warehouses.length }} entrepôt{{ warehouses.length !== 1 ? 's' : '' }}
          </p>

          <div class="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            <WarehouseStockCard
              v-for="warehouse in warehouses"
              :key="warehouse.id"
              :warehouse="warehouse"
              @click="selectWarehouse(warehouse.id)"
            />
          </div>
        </div>
      </div>

      <!-- Si un entrepôt est sélectionné, afficher ses détails -->
      <div v-else>
        <div class="mt-6 flex items-center gap-4">
          <button
            @click="selectedWarehouseId = null"
            class="flex items-center gap-2 text-sm text-primary hover:underline"
          >
            ← Retour aux entrepôts
          </button>
        </div>

        <BaseCard v-if="currentWarehouseView" class="mt-4 space-y-4">
          <div class="flex flex-col gap-2 md:flex-row md:items-start md:justify-between">
            <div>
              <h2 class="text-lg font-semibold text-text-main">{{ currentWarehouseView.warehouse.name }}</h2>
              <p class="text-sm text-text-secondary">
                {{ currentWarehouseView.warehouse.address }} · {{ currentWarehouseView.warehouse.city }}
              </p>
            </div>
            <div class="text-sm text-text-secondary">
              <div>Capacité utilisée : {{ currentWarehouseView.warehouse.usedCapacity }}/{{ currentWarehouseView.warehouse.totalCapacity }}</div>
              <div>{{ currentWarehouseView.stocks.length }} ligne(s) de stock</div>
            </div>
          </div>

          <!-- Filtres -->
          <div class="flex flex-wrap items-center gap-3 border-t border-border pt-4">
            <BaseInput
              v-model="searchQuery"
              placeholder="Rechercher un produit…"
              class="w-full max-w-xs"
            />
            <button
              @click="toggleBelowMinFilter"
              :class="[
                'rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                filterBelowMin
                  ? 'bg-amber-100 text-amber-700'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              {{ filterBelowMin ? '✓ Sous seuil uniquement' : 'Tous les stocks' }}
            </button>
          </div>

          <!-- Tableau des stocks -->
          <div class="overflow-x-auto">
            <table class="min-w-full text-sm">
              <thead>
                <tr class="border-b border-border text-left text-text-secondary">
                  <th class="py-2 pr-4">Produit</th>
                  <th class="py-2 pr-4">Référence</th>
                  <th class="py-2 pr-4">Zone</th>
                  <th class="py-2 pr-4">Disponible</th>
                  <th class="py-2 pr-4">Seuil</th>
                  <th class="py-2 pr-4">État</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="stock in filteredStocks"
                  :key="stock.id"
                  class="border-b border-border/60 last:border-0"
                >
                  <td class="py-3 pr-4 font-medium text-text-main">{{ stock.productName }}</td>
                  <td class="py-3 pr-4 text-text-secondary">{{ stock.productReference }}</td>
                  <td class="py-3 pr-4 text-text-secondary">{{ stock.zoneName }}</td>
                  <td class="py-3 pr-4">{{ stock.quantityAvailable }}</td>
                  <td class="py-3 pr-4 text-text-secondary">
                    {{ stock.minStock != null ? `Min ${stock.minStock}` : '—' }}
                    <span v-if="stock.maxStock != null"> · Max {{ stock.maxStock }}</span>
                  </td>
                  <td class="py-3 pr-4">
                    <span
                      :class="[
                        'rounded-full px-2.5 py-1 text-xs font-medium',
                        stock.isBelowMin ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700',
                      ]"
                    >
                      {{ stock.isBelowMin ? 'Sous seuil' : 'OK' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="filteredStocks.length === 0" class="py-8 text-center text-sm text-text-secondary">
            Aucun stock trouvé avec ces filtres.
          </div>

          <!-- Historique des entrées -->
          <div class="border-t border-border pt-4">
            <h3 class="mb-3 text-sm font-semibold uppercase tracking-wide text-text-secondary">
              Historique des entrées
            </h3>
            <div v-if="currentWarehouseView.entries.length === 0" class="text-sm text-text-secondary">
              Aucune entrée enregistrée pour cet entrepôt.
            </div>
            <div v-else class="overflow-x-auto">
              <table class="min-w-full text-sm">
                <thead>
                  <tr class="border-b border-border text-left text-text-secondary">
                    <th class="py-2 pr-4">Produit</th>
                    <th class="py-2 pr-4">Quantité</th>
                    <th class="py-2 pr-4">Document</th>
                    <th class="py-2 pr-4">Note</th>
                    <th class="py-2 pr-4">Date</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="entry in currentWarehouseView.entries" :key="entry.id" class="border-b border-border/60 last:border-0">
                    <td class="py-3 pr-4 font-medium text-text-main">{{ entry.productName }}</td>
                    <td class="py-3 pr-4">{{ entry.quantity }}</td>
                    <td class="py-3 pr-4 text-text-secondary">{{ entry.referenceDoc || '—' }}</td>
                    <td class="py-3 pr-4 text-text-secondary">{{ entry.note || '—' }}</td>
                    <td class="py-3 pr-4 text-text-secondary">{{ formatDate(entry.createdAt) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </BaseCard>
      </div>
    </div>

    <!-- Vue Manager/Magasinier : Affichage direct de leur entrepôt -->
    <div v-else-if="isManager || isStorekeeper">
      <div v-if="!currentWarehouseView" class="mt-10">
        <EmptyState
          title="Aucun entrepôt"
          description="Vous n'êtes assigné à aucun entrepôt."
        />
      </div>

      <BaseCard v-else class="mt-6 space-y-4">
        <div class="flex flex-col gap-2 md:flex-row md:items-start md:justify-between">
          <div>
            <h2 class="text-lg font-semibold text-text-main">{{ currentWarehouseView.warehouse.name }}</h2>
            <p class="text-sm text-text-secondary">
              {{ currentWarehouseView.warehouse.address }} · {{ currentWarehouseView.warehouse.city }}
            </p>
          </div>
          <div class="text-sm text-text-secondary">
            <div v-if="!isStorekeeper">
              Capacité utilisée : {{ currentWarehouseView.warehouse.usedCapacity }}/{{ currentWarehouseView.warehouse.totalCapacity }}
            </div>
            <div>{{ currentWarehouseView.stocks.length }} ligne(s) de stock</div>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead>
              <tr class="border-b border-border text-left text-text-secondary">
                <th class="py-2 pr-4">Produit</th>
                <th class="py-2 pr-4">Référence</th>
                <th class="py-2 pr-4">Zone</th>
                <th class="py-2 pr-4">Disponible</th>
                <th v-if="showThresholdInfo" class="py-2 pr-4">Seuil</th>
                <th class="py-2 pr-4">État</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="stock in currentWarehouseView.stocks" :key="stock.id" class="border-b border-border/60 last:border-0">
                <td class="py-3 pr-4 font-medium text-text-main">{{ stock.productName }}</td>
                <td class="py-3 pr-4 text-text-secondary">{{ stock.productReference }}</td>
                <td class="py-3 pr-4 text-text-secondary">{{ stock.zoneName }}</td>
                <td class="py-3 pr-4">{{ stock.quantityAvailable }}</td>
                <td v-if="showThresholdInfo" class="py-3 pr-4 text-text-secondary">
                  {{ stock.minStock != null ? `Min ${stock.minStock}` : '—' }}
                  <span v-if="stock.maxStock != null"> · Max {{ stock.maxStock }}</span>
                </td>
                <td class="py-3 pr-4">
                  <span
                    :class="[
                      'rounded-full px-2.5 py-1 text-xs font-medium',
                      stock.isBelowMin ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700',
                    ]"
                  >
                    {{ stock.isBelowMin ? 'Sous seuil' : 'OK' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="showEntryHistory" class="border-t border-border pt-4">
          <h3 class="mb-3 text-sm font-semibold uppercase tracking-wide text-text-secondary">
            Historique des entrées
          </h3>
          <div v-if="currentWarehouseView.entries.length === 0" class="text-sm text-text-secondary">
            Aucune entrée enregistrée pour cet entrepôt.
          </div>
          <div v-else class="overflow-x-auto">
            <table class="min-w-full text-sm">
              <thead>
                <tr class="border-b border-border text-left text-text-secondary">
                  <th class="py-2 pr-4">Produit</th>
                  <th class="py-2 pr-4">Quantité</th>
                  <th class="py-2 pr-4">Document</th>
                  <th class="py-2 pr-4">Note</th>
                  <th class="py-2 pr-4">Date</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="entry in currentWarehouseView.entries" :key="entry.id" class="border-b border-border/60 last:border-0">
                  <td class="py-3 pr-4 font-medium text-text-main">{{ entry.productName }}</td>
                  <td class="py-3 pr-4">{{ entry.quantity }}</td>
                  <td class="py-3 pr-4 text-text-secondary">{{ entry.referenceDoc || '—' }}</td>
                  <td class="py-3 pr-4 text-text-secondary">{{ entry.note || '—' }}</td>
                  <td class="py-3 pr-4 text-text-secondary">{{ formatDate(entry.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </BaseCard>
    </div>

    <!-- Autres rôles -->
    <div v-else class="mt-10">
      <EmptyState
        title="Accès restreint"
        description="Vous n'avez pas les permissions nécessaires pour consulter les stocks."
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { stockService } from '@/services/stock.service'
import { warehouseService } from '@/services/warehouse.service'
import type { StockResponse, StockMovementResponse, MovementType } from '@/types/stock.types'
import type { WarehouseResponse } from '@/types/warehouse.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import WarehouseStockCard from '@/components/stock/WarehouseStockCard.vue'

interface WarehouseViewModel {
  warehouse: WarehouseResponse
  stocks: StockResponse[]
  entries: StockMovementResponse[]
}

const authStore = useAuthStore()
const currentUser = computed(() => authStore.currentUser)
const role = computed(() => currentUser.value?.role ?? '')
const isAdmin = computed(() => role.value === 'Administrateur')
const isManager = computed(() => role.value === "Gestionnaire d'entrepôt")
const isStorekeeper = computed(() => role.value === 'Magasinier')
const showThresholdInfo = computed(() => !isStorekeeper.value)
const showEntryHistory = computed(() => !isStorekeeper.value)

const loading = ref(false)
const error = ref('')
const warehouses = ref<WarehouseResponse[]>([])
const selectedWarehouseId = ref<number | null>(null)
const currentWarehouseView = ref<WarehouseViewModel | null>(null)
const searchQuery = ref('')
const filterBelowMin = ref(false)

const filteredStocks = computed(() => {
  if (!currentWarehouseView.value) return []
  
  let stocks = currentWarehouseView.value.stocks
  
  // Filtre par recherche
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    stocks = stocks.filter(
      (s) =>
        s.productName.toLowerCase().includes(query) ||
        s.productReference.toLowerCase().includes(query)
    )
  }
  
  // Filtre par seuil bas
  if (filterBelowMin.value) {
    stocks = stocks.filter((s) => s.isBelowMin)
  }
  
  return stocks
})

function formatDate(value: string) {
  return new Date(value).toLocaleString('fr-FR')
}

function toggleBelowMinFilter() {
  filterBelowMin.value = !filterBelowMin.value
}

async function fetchStocksForWarehouse(warehouse: WarehouseResponse) {
  const [stocksRes, entriesRes] = await Promise.all([
    stockService.list(warehouse.id, { size: 200 }),
    stockService.listMovements(warehouse.id, { movementType: 'ENTRY' as MovementType, size: 100 }),
  ])

  return {
    warehouse,
    stocks: stocksRes.content,
    entries: entriesRes.content,
  } satisfies WarehouseViewModel
}

async function selectWarehouse(warehouseId: number) {
  loading.value = true
  error.value = ''
  try {
    const warehouse = warehouses.value.find((item) => item.id === warehouseId)
    if (!warehouse) {
      return
    }

    selectedWarehouseId.value = warehouse.id
    currentWarehouseView.value = await fetchStocksForWarehouse(warehouse)
    
    // Réinitialiser les filtres
    searchQuery.value = ''
    filterBelowMin.value = false
  } catch (err) {
    error.value = 'Impossible de charger les stocks de cet entrepôt.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

async function loadAdminView() {
  const res = await warehouseService.list({ active: true, size: 100 })
  warehouses.value = res.content.filter((warehouse) => warehouse.isActive)
}

async function loadUserWarehouseView() {
  const warehouseId = currentUser.value?.warehouseId
  if (!warehouseId) {
    currentWarehouseView.value = null
    return
  }

  if (isStorekeeper.value) {
    const warehouse: WarehouseResponse = {
      id: warehouseId,
      name: currentUser.value?.warehouseName ?? 'Mon entrepôt',
      address: '',
      city: '',
      totalCapacity: 0,
      usedCapacity: 0,
      isActive: true,
      createdAt: '',
      updatedAt: '',
    }
    currentWarehouseView.value = await fetchStocksForWarehouse(warehouse)
    return
  }

  const warehouse = await warehouseService.get(warehouseId)
  currentWarehouseView.value = await fetchStocksForWarehouse(warehouse)
}

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    if (isAdmin.value) {
      await loadAdminView()
    } else if (isManager.value || isStorekeeper.value) {
      await loadUserWarehouseView()
    }
  } catch (err) {
    error.value = 'Impossible de charger les stocks.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>
