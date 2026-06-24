<template>
  <div class="space-y-4">

    <div class="flex flex-wrap items-center justify-between gap-3">
      <div class="flex flex-wrap gap-3">
        <BaseInput v-model="search" placeholder="Rechercher un produit..." class="max-w-xs" @input="debouncedFetch" />
        <select
          v-model="filters.zoneId"
          @change="fetchStocks"
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option value="">Toutes les zones</option>
          <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.name }}</option>
        </select>
        <select
          v-model="filters.categoryId"
          @change="fetchStocks"
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option value="">Toutes les catégories</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
        <label class="flex cursor-pointer items-center gap-2 text-sm">
          <input type="checkbox" v-model="filters.belowMin" @change="fetchStocks" class="rounded" />
          Seuil bas uniquement
        </label>
      </div>
      <BaseButton @click="showCreateModal = true">+ Initialiser stock</BaseButton>
    </div>

    <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">Chargement…</div>
    <BaseTable v-else :columns="columns">
      <tr
        v-for="stock in stocks"
        :key="stock.id"
        class="border-t border-border hover:bg-primary-light/30"
      >
        <td class="px-4 py-3">
          <p class="font-medium text-text-main">{{ stock.productName }}</p>
          <p class="text-xs text-text-secondary">{{ stock.categoryName }} · {{ stock.zoneName }}</p>
        </td>
        <td class="px-4 py-3">
          <div class="flex items-center gap-2">
            <span :class="stock.isBelowMin ? 'font-semibold text-red-600' : 'text-text-main'">
              {{ stock.quantityAvailable }}
            </span>
            <StatusBadge v-if="stock.isBelowMin" label="Seuil bas" variant="danger" />
          </div>
        </td>
        <td class="px-4 py-3 text-xs text-text-secondary">
          Min : {{ stock.minStock ?? '—' }} / Max : {{ stock.maxStock ?? '—' }}
        </td>
        <td class="px-4 py-3">
          <BaseButton size="sm" variant="secondary" @click="openAdjust(stock)">Ajuster</BaseButton>
        </td>
      </tr>
    </BaseTable>

    <EmptyState
      v-if="!loading && stocks.length === 0"
      title="Aucune ligne de stock"
      description="Initialisez le stock d'un produit dans cet entrepôt."
    />

    <div class="flex items-center justify-between">
      <span class="text-sm text-text-secondary">{{ totalElements }} ligne(s)</span>
      <div class="flex gap-2">
        <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetchStocks()">Précédent</BaseButton>
        <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
        <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetchStocks()">Suivant</BaseButton>
      </div>
    </div>

    <StockCreateModal
      v-if="showCreateModal"
      :warehouse-id="warehouseId"
      :zones="zones"
      @close="showCreateModal = false"
      @saved="onSaved"
    />

    <StockAdjustModal
      v-if="selectedStock"
      :stock="selectedStock"
      :warehouse-id="warehouseId"
      @close="selectedStock = null"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { stockService } from '@/services/stock.service'
import { categoryService } from '@/services/category.service'
import { zoneService } from '@/services/zone.service'
import type { StockResponse } from '@/types/stock.types'
import type { CategoryResponse } from '@/types/category.types'
import type { ZoneResponse } from '@/types/zone.types'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StockCreateModal from '@/components/stock/StockCreateModal.vue'
import StockAdjustModal from '@/components/stock/StockAdjustModal.vue'

const props = defineProps<{ warehouseId: number }>()

const stocks        = ref<StockResponse[]>([])
const zones         = ref<ZoneResponse[]>([])
const categories    = ref<CategoryResponse[]>([])
const loading       = ref(false)
const page          = ref(0)
const totalPages    = ref(1)
const totalElements = ref(0)
const search        = ref('')
const filters       = ref({ zoneId: '' as number | '', categoryId: '' as number | '', belowMin: false })
const showCreateModal = ref(false)
const selectedStock   = ref<StockResponse | null>(null)

const columns = [
  { key: 'product',    label: 'Produit' },
  { key: 'qty',        label: 'Disponible' },
  { key: 'thresholds', label: 'Seuils' },
  { key: 'actions',    label: '' },
]

let debounceTimer: ReturnType<typeof setTimeout>
function debouncedFetch() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => { page.value = 0; fetchStocks() }, 300)
}

async function fetchStocks() {
  loading.value = true
  try {
    const res = await stockService.list(props.warehouseId, {
      search:     search.value || undefined,
      zoneId:     filters.value.zoneId     ? Number(filters.value.zoneId)     : undefined,
      categoryId: filters.value.categoryId ? Number(filters.value.categoryId) : undefined,
      belowMin:   filters.value.belowMin   || undefined,
      page:       page.value,
      size:       20,
    })
    stocks.value        = res.content
    totalPages.value    = res.totalPages
    totalElements.value = res.totalElements
  } finally {
    loading.value = false
  }
}

function openAdjust(stock: StockResponse) {
  selectedStock.value = stock
}

function onSaved() {
  showCreateModal.value = false
  selectedStock.value   = null
  fetchStocks()
}

onMounted(async () => {
  fetchStocks()
  try {
    const [zonesRes, catsRes] = await Promise.all([
      zoneService.list(props.warehouseId, { size: 100 }),
      categoryService.listByWarehouse(props.warehouseId, {}),
    ])
    zones.value      = zonesRes.content
    categories.value = catsRes.content
  } catch { /* silencieux */ }
})
</script>
