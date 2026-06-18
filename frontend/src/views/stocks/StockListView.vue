<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { Boxes, Search, AlertTriangle, History, Warehouse } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const auth = useAuthStore()
const canUpdate     = computed(() => auth.hasPermission(PERMISSIONS.STOCK_UPDATE))
const canViewHistory = computed(() => auth.hasPermission(PERMISSIONS.STOCK_VIEW_HISTORY))

const search  = ref('')
const whFilter = ref('all')

const stocks = ref([
  { id: 1, product: 'Câble USB-C 2m',      ref: 'REF-0234', warehouse: 'Paris Nord',   zone: 'Zone B',  available: 3,   reserved: 0,  transit: 0,   min: 50,  max: 200  },
  { id: 2, product: 'Boîtier PC Tour ATX',  ref: 'REF-1102', warehouse: 'Paris Nord',   zone: 'Zone B',  available: 12,  reserved: 2,  transit: 0,   min: 20,  max: 80   },
  { id: 3, product: 'Disque SSD 1To',       ref: 'REF-0891', warehouse: 'Lyon Est',     zone: 'Zone A',  available: 0,   reserved: 0,  transit: 15,  min: 10,  max: 50   },
  { id: 4, product: 'RAM DDR5 16Go',        ref: 'REF-0445', warehouse: 'Marseille',    zone: 'Zone B',  available: 8,   reserved: 4,  transit: 0,   min: 15,  max: 60   },
  { id: 5, product: 'Écran 27" 4K',         ref: 'REF-2201', warehouse: 'Paris Nord',   zone: 'Zone C',  available: 45,  reserved: 5,  transit: 0,   min: 5,   max: 30   },
  { id: 6, product: 'Pneu 205/55 R16',      ref: 'REF-3301', warehouse: 'Bordeaux',     zone: 'Zone A',  available: 120, reserved: 20, transit: 0,   min: 20,  max: 300  },
  { id: 7, product: 'Frigo 250L A++',       ref: 'REF-4401', warehouse: 'Lyon Est',     zone: 'Zone B',  available: 7,   reserved: 1,  transit: 3,   min: 3,   max: 20   },
])

const warehouses = computed(() => ['all', ...new Set(stocks.value.map(s => s.warehouse))])

const filtered = computed(() => stocks.value
  .filter(s => whFilter.value === 'all' || s.warehouse === whFilter.value)
  .filter(s => !search.value || s.product.toLowerCase().includes(search.value.toLowerCase()) || s.ref.toLowerCase().includes(search.value.toLowerCase()))
)

function status(s: typeof stocks.value[0]) {
  if (s.available === 0 && s.transit === 0) return { v: 'critical' as const, l: 'Rupture' }
  if (s.available === 0)                    return { v: 'warning'  as const, l: 'En transit' }
  if (s.available < s.min)                  return { v: 'warning'  as const, l: 'Stock bas' }
  return                                           { v: 'success'  as const, l: 'OK' }
}
function pct(s: typeof stocks.value[0]) { return s.max ? Math.min(Math.round((s.available / s.max) * 100), 100) : 0 }
function pctColor(p: number, s: typeof stocks.value[0]) {
  if (s.available < s.min) return '#ef4444'
  if (p < 30) return '#f59e0b'
  return '#10b981'
}
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Gestion des Stocks"
      subtitle="Niveaux de stock par produit et entrepôt"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Stocks' }]"
    />

    <!-- KPIs -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div v-for="kpi in [
        { l: 'Références suivies', v: stocks.length,                                         c: 'text-primary' },
        { l: 'En rupture',         v: stocks.filter(s=>s.available===0&&s.transit===0).length, c: 'text-error'   },
        { l: 'Stock bas',          v: stocks.filter(s=>s.available>0&&s.available<s.min).length, c: 'text-warning' },
        { l: 'En transit',         v: stocks.filter(s=>s.transit>0).length,                   c: 'text-info'    },
      ]" :key="kpi.l" class="bg-surface rounded-xl border border-border-light p-4 shadow-sm">
        <p class="text-label">{{ kpi.l }}</p>
        <p :class="['text-2xl font-extrabold mt-1', kpi.c]">{{ kpi.v }}</p>
      </div>
    </div>

    <!-- Filtres -->
    <BaseCard padding="sm">
      <div class="flex flex-wrap items-center gap-3">
        <div class="relative flex-1 min-w-48">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
          <input v-model="search" placeholder="Produit, référence…"
            class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
        </div>
        <select v-model="whFilter" class="px-3 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary transition-base text-text-main">
          <option v-for="w in warehouses" :key="w" :value="w">{{ w === 'all' ? 'Tous les entrepôts' : w }}</option>
        </select>
      </div>
    </BaseCard>

    <BaseCard padding="none">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-border-light bg-background">
              <th class="text-left px-5 py-3 text-label">Produit</th>
              <th class="text-left px-5 py-3 text-label">Entrepôt / Zone</th>
              <th class="text-left px-5 py-3 text-label">Disponible</th>
              <th class="text-left px-5 py-3 text-label">Réservé</th>
              <th class="text-left px-5 py-3 text-label">Transit</th>
              <th class="text-left px-5 py-3 text-label">Niveau</th>
              <th class="text-left px-5 py-3 text-label">État</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody class="divide-y divide-border-light">
            <tr v-for="s in filtered" :key="s.id" class="hover:bg-background transition-fast group">
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-lg bg-primary-100 flex items-center justify-center shrink-0">
                    <Boxes class="w-4 h-4 text-primary" />
                  </div>
                  <div>
                    <p class="font-semibold text-text-main">{{ s.product }}</p>
                    <p class="font-mono text-xs text-text-muted">{{ s.ref }}</p>
                  </div>
                </div>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1.5 text-text-secondary font-medium">
                  <Warehouse class="w-3.5 h-3.5 text-text-muted" />{{ s.warehouse }}
                </div>
                <p class="text-xs text-text-muted mt-0.5">{{ s.zone }}</p>
              </td>
              <td class="px-5 py-3.5 font-bold" :class="s.available === 0 ? 'text-error' : s.available < s.min ? 'text-warning' : 'text-success'">
                {{ s.available }}
              </td>
              <td class="px-5 py-3.5 text-text-secondary font-medium">{{ s.reserved }}</td>
              <td class="px-5 py-3.5">
                <span v-if="s.transit > 0" class="font-medium text-info">{{ s.transit }}</span>
                <span v-else class="text-text-subtle">—</span>
              </td>
              <td class="px-5 py-3.5 min-w-36">
                <div class="flex items-center gap-2">
                  <div class="flex-1 h-2 bg-border-light rounded-full overflow-hidden">
                    <div class="h-full rounded-full transition-slow"
                      :style="{ width: `${pct(s)}%`, background: pctColor(pct(s), s) }" />
                  </div>
                  <span class="text-xs text-text-muted w-10 text-right">{{ s.available }}/{{ s.max }}</span>
                </div>
              </td>
              <td class="px-5 py-3.5"><Badge :variant="status(s).v" size="sm" dot>{{ status(s).l }}</Badge></td>
              <td class="px-5 py-3.5">
                <div class="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-fast">
                  <button v-if="canViewHistory" class="p-1.5 rounded-lg text-text-muted hover:text-primary hover:bg-primary-100 transition-fast" title="Historique">
                    <History class="w-4 h-4" />
                  </button>
                  <button v-if="canUpdate" class="px-2.5 py-1 text-xs font-semibold text-primary bg-primary-100 rounded-lg hover:bg-primary-200 transition-fast">Ajuster</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-if="filtered.length === 0" :icon="Boxes" title="Aucun stock trouvé" />
      </div>
      <div class="px-5 py-3 border-t border-border-light text-caption">{{ filtered.length }} référence{{ filtered.length > 1 ? 's' : '' }}</div>
    </BaseCard>
  </div>
</template>
