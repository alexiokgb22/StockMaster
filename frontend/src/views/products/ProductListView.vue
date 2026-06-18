<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { Package, Plus, Search, LayoutGrid, List, Tag, AlertTriangle } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const auth = useAuthStore()
const canCreate = computed(() => auth.hasPermission(PERMISSIONS.PRODUCT_CREATE))
const canUpdate = computed(() => auth.hasPermission(PERMISSIONS.PRODUCT_UPDATE))
const canDelete = computed(() => auth.hasPermission(PERMISSIONS.PRODUCT_DELETE_LOGIC))

const search  = ref('')
const viewMode = ref<'list' | 'grid'>('list')
const catFilter = ref('all')

const products = ref([
  { id: 1, ref: 'REF-0234', name: 'Câble USB-C 2m',      category: 'Informatique',    price: 12.99,  stock: 3,   minStock: 50,  active: true  },
  { id: 2, ref: 'REF-1102', name: 'Boîtier PC Tour ATX',  category: 'Informatique',    price: 89.90,  stock: 12,  minStock: 20,  active: true  },
  { id: 3, ref: 'REF-0891', name: 'Disque SSD 1To',       category: 'Informatique',    price: 74.99,  stock: 0,   minStock: 10,  active: true  },
  { id: 4, ref: 'REF-0445', name: 'RAM DDR5 16Go',        category: 'Informatique',    price: 59.00,  stock: 8,   minStock: 15,  active: true  },
  { id: 5, ref: 'REF-2201', name: 'Écran 27" 4K',         category: 'Informatique',    price: 349.00, stock: 45,  minStock: 5,   active: true  },
  { id: 6, ref: 'REF-3301', name: 'Pneu 205/55 R16',      category: 'Automobile',      price: 68.50,  stock: 120, minStock: 20,  active: true  },
  { id: 7, ref: 'REF-4401', name: 'Frigo 250L A++',       category: 'Électroménager',  price: 429.00, stock: 7,   minStock: 3,   active: true  },
  { id: 8, ref: 'REF-5501', name: 'Clavier mécanique',    category: 'Informatique',    price: 129.00, stock: 0,   minStock: 10,  active: false },
])

const categories = computed(() => ['all', ...new Set(products.value.map(p => p.category))])

const filtered = computed(() => products.value
  .filter(p => catFilter.value === 'all' || p.category === catFilter.value)
  .filter(p => !search.value || p.name.toLowerCase().includes(search.value.toLowerCase()) || p.ref.toLowerCase().includes(search.value.toLowerCase()))
)

function stockStatus(p: typeof products.value[0]) {
  if (p.stock === 0)              return { variant: 'critical' as const, label: 'Rupture' }
  if (p.stock < p.minStock)       return { variant: 'warning'  as const, label: 'Stock bas' }
  return                                 { variant: 'success'  as const, label: 'OK' }
}
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Produits"
      subtitle="Catalogue complet des produits"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Produits' }]"
    >
      <template #actions>
        <Button v-if="canCreate" variant="accent" size="sm">
          <Plus class="w-4 h-4" /> Nouveau produit
        </Button>
      </template>
    </PageHeader>

    <!-- KPIs rapides -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div v-for="kpi in [
        { label: 'Total produits', value: products.length, color: 'text-primary' },
        { label: 'En rupture',     value: products.filter(p=>p.stock===0).length, color: 'text-error' },
        { label: 'Stock bas',      value: products.filter(p=>p.stock>0&&p.stock<p.minStock).length, color: 'text-warning' },
        { label: 'Inactifs',       value: products.filter(p=>!p.active).length, color: 'text-text-muted' },
      ]" :key="kpi.label" class="bg-surface rounded-xl border border-border-light p-4 shadow-sm">
        <p class="text-label">{{ kpi.label }}</p>
        <p :class="['text-2xl font-extrabold mt-1', kpi.color]">{{ kpi.value }}</p>
      </div>
    </div>

    <!-- Filtres + toggle vue -->
    <BaseCard padding="sm">
      <div class="flex flex-wrap items-center gap-3">
        <div class="relative flex-1 min-w-48">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
          <input v-model="search" placeholder="Référence, nom de produit…"
            class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
        </div>
        <select v-model="catFilter" class="px-3 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary transition-base text-text-main">
          <option v-for="c in categories" :key="c" :value="c">{{ c === 'all' ? 'Toutes catégories' : c }}</option>
        </select>
        <div class="flex gap-1 bg-background rounded-lg p-1 border border-border ml-auto">
          <button @click="viewMode='list'" :class="['p-1.5 rounded transition-fast', viewMode==='list' ? 'bg-surface shadow-sm text-primary' : 'text-text-muted']"><List class="w-4 h-4" /></button>
          <button @click="viewMode='grid'" :class="['p-1.5 rounded transition-fast', viewMode==='grid' ? 'bg-surface shadow-sm text-primary' : 'text-text-muted']"><LayoutGrid class="w-4 h-4" /></button>
        </div>
      </div>
    </BaseCard>

    <!-- Vue liste -->
    <BaseCard v-if="viewMode === 'list'" padding="none">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-border-light bg-background">
              <th class="text-left px-5 py-3 text-label">Produit</th>
              <th class="text-left px-5 py-3 text-label">Référence</th>
              <th class="text-left px-5 py-3 text-label">Catégorie</th>
              <th class="text-left px-5 py-3 text-label">Prix achat</th>
              <th class="text-left px-5 py-3 text-label">Stock</th>
              <th class="text-left px-5 py-3 text-label">État</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody class="divide-y divide-border-light">
            <tr v-for="p in filtered" :key="p.id" class="hover:bg-background transition-fast group">
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-lg bg-primary-100 flex items-center justify-center shrink-0">
                    <Package class="w-4 h-4 text-primary" />
                  </div>
                  <span class="font-semibold text-text-main">{{ p.name }}</span>
                </div>
              </td>
              <td class="px-5 py-3.5 font-mono text-xs text-text-muted">{{ p.ref }}</td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1.5">
                  <Tag class="w-3.5 h-3.5 text-text-muted" />
                  <span class="text-text-secondary">{{ p.category }}</span>
                </div>
              </td>
              <td class="px-5 py-3.5 font-semibold text-text-main">{{ p.price.toFixed(2) }} €</td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-2">
                  <span :class="['font-bold', p.stock === 0 ? 'text-error' : p.stock < p.minStock ? 'text-warning' : 'text-success']">
                    {{ p.stock }}
                  </span>
                  <span class="text-text-muted text-xs">/ min {{ p.minStock }}</span>
                </div>
              </td>
              <td class="px-5 py-3.5">
                <Badge :variant="stockStatus(p).variant" size="sm" dot>{{ stockStatus(p).label }}</Badge>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-fast">
                  <button v-if="canUpdate" class="px-2.5 py-1 text-xs font-semibold text-primary bg-primary-100 rounded-lg hover:bg-primary-200 transition-fast">Modifier</button>
                  <button v-if="canDelete && p.active" class="px-2.5 py-1 text-xs font-semibold text-error bg-error-light rounded-lg hover:bg-red-100 transition-fast">Désactiver</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-if="filtered.length === 0" :icon="Package" title="Aucun produit trouvé" />
      </div>
      <div class="px-5 py-3 border-t border-border-light flex items-center justify-between text-caption">
        <span>{{ filtered.length }} produit{{ filtered.length > 1 ? 's' : '' }}</span>
        <div class="flex gap-1">
          <button class="px-3 py-1 rounded border border-border text-xs hover:bg-background transition-fast">Préc.</button>
          <button class="px-3 py-1 rounded bg-primary text-white text-xs">1</button>
          <button class="px-3 py-1 rounded border border-border text-xs hover:bg-background transition-fast">Suiv.</button>
        </div>
      </div>
    </BaseCard>

    <!-- Vue grille -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <div v-for="p in filtered" :key="p.id"
        class="bg-surface rounded-xl border border-border-light shadow-sm p-4 hover:shadow-md hover:-translate-y-0.5 transition-base group">
        <div class="flex items-start justify-between mb-3">
          <div class="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center">
            <Package class="w-5 h-5 text-primary" />
          </div>
          <Badge :variant="stockStatus(p).variant" size="sm" dot>{{ stockStatus(p).label }}</Badge>
        </div>
        <p class="font-semibold text-text-main text-sm mb-0.5">{{ p.name }}</p>
        <p class="font-mono text-xs text-text-muted mb-3">{{ p.ref }}</p>
        <div class="flex items-center justify-between text-xs border-t border-border-light pt-3">
          <span class="text-text-muted">{{ p.category }}</span>
          <span class="font-bold text-text-main">{{ p.price.toFixed(2) }} €</span>
        </div>
        <div class="mt-2 flex items-center gap-2">
          <div class="flex-1 h-1.5 bg-border-light rounded-full overflow-hidden">
            <div class="h-full rounded-full" :style="{ width: `${Math.min((p.stock/p.minStock)*100,100)}%`, background: p.stock===0?'#ef4444':p.stock<p.minStock?'#f59e0b':'#10b981' }" />
          </div>
          <span class="text-xs font-bold w-8 text-right" :style="{ color: p.stock===0?'#ef4444':p.stock<p.minStock?'#f59e0b':'#10b981' }">{{ p.stock }}</span>
        </div>
      </div>
      <EmptyState v-if="filtered.length === 0" :icon="Package" title="Aucun produit trouvé" class="col-span-full" />
    </div>
  </div>
</template>
