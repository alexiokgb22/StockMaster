<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between flex-wrap gap-3">
      <div class="flex gap-3">
        <BaseInput
          v-model="search"
          placeholder="Rechercher un produit..."
          class="max-w-xs"
          @input="debouncedFetch"
        />
        <select
          v-model="selectedCategory"
          @change="fetchProducts"
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option value="">Toutes les catégories</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
      </div>
      <BaseButton @click="openCreate">+ Nouveau produit</BaseButton>
    </div>

    <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">Chargement…</div>
    <BaseTable v-else :columns="columns">
      <tr
        v-for="product in products"
        :key="product.id"
        class="border-t border-border hover:bg-primary-light/30"
      >
        <td class="px-4 py-3">
          <div class="flex items-center gap-2">
            <span class="font-medium text-text-main">{{ product.name }}</span>
            <StatusBadge v-if="product.isAdminDefined" label="Admin" variant="info" />
          </div>
        </td>
        <td class="px-4 py-3 text-text-secondary">{{ product.categoryName }}</td>
        <td class="px-4 py-3">
          <code class="rounded bg-gray-100 px-1.5 py-0.5 text-xs">{{ product.reference }}</code>
        </td>
        <td class="px-4 py-3 space-x-2">
          <!-- Boutons masqués si produit Admin et utilisateur gestionnaire -->
          <template v-if="!product.isAdminDefined">
            <BaseButton size="sm" variant="secondary" @click="openEdit(product)">Modifier</BaseButton>
            <BaseButton
              size="sm"
              :variant="product.isActive ? 'danger' : 'secondary'"
              @click="handleToggle(product)"
            >{{ product.isActive ? 'Désactiver' : 'Activer' }}</BaseButton>
          </template>
          <span v-else class="text-xs italic text-text-secondary">Lecture seule</span>
        </td>
      </tr>
    </BaseTable>

    <EmptyState
      v-if="!loading && products.length === 0"
      title="Aucun produit"
      description="Aucun produit disponible dans cet entrepôt."
    />

    <div class="flex items-center justify-between">
      <span class="text-sm text-text-secondary">{{ totalElements }} produit(s)</span>
      <div class="flex gap-2">
        <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetchProducts()">Précédent</BaseButton>
        <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
        <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetchProducts()">Suivant</BaseButton>
      </div>
    </div>

    <ProductFormModal
      v-if="showModal"
      :product="selectedProduct"
      :categories="categories"
      :warehouse-id="warehouseId"
      @close="showModal = false"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { productService } from '@/services/product.service'
import { categoryService } from '@/services/category.service'
import type { ProductResponse } from '@/types/product.types'
import type { CategoryResponse } from '@/types/category.types'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ProductFormModal from '@/components/product/ProductFormModal.vue'
import { useToastStore } from '@/stores/toast'

const props = defineProps<{ warehouseId: number }>()
const toast = useToastStore()

const products       = ref<ProductResponse[]>([])
const categories     = ref<CategoryResponse[]>([])
const loading        = ref(false)
const page           = ref(0)
const totalPages     = ref(1)
const totalElements  = ref(0)
const search         = ref('')
const selectedCategory = ref<number | ''>('')
const showModal        = ref(false)
const selectedProduct  = ref<ProductResponse | null>(null)

const columns = [
  { key: 'name',      label: 'Nom' },
  { key: 'category',  label: 'Catégorie' },
  { key: 'reference', label: 'Référence' },
  { key: 'actions',   label: '' },
]

let debounceTimer: ReturnType<typeof setTimeout>
function debouncedFetch() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => { page.value = 0; fetchProducts() }, 300)
}

async function fetchProducts() {
  loading.value = true
  try {
    const res = await productService.listByWarehouse(props.warehouseId, {
      search:     search.value || undefined,
      categoryId: selectedCategory.value ? Number(selectedCategory.value) : undefined,
      page:       page.value,
      size:       20,
    })
    products.value      = res.content
    totalPages.value    = res.totalPages
    totalElements.value = res.totalElements
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res = await categoryService.listByWarehouse(props.warehouseId, {})
    categories.value = res.content
  } catch { /* silencieux */ }
}

function openCreate() {
  selectedProduct.value = null
  showModal.value = true
}

function openEdit(product: ProductResponse) {
  selectedProduct.value = product
  showModal.value = true
}

async function handleToggle(product: ProductResponse) {
  try {
    await productService.toggle(product.id)
    toast.success(product.isActive ? 'Produit désactivé' : 'Produit activé')
    fetchProducts()
  } catch (e: any) {
    toast.error(e.response?.data?.message ?? 'Erreur')
  }
}

function onSaved() {
  showModal.value = false
  fetchProducts()
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>
