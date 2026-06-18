<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { ArrowLeftRight, Plus, Search, ChevronRight } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const auth = useAuthStore()
const canCreate   = computed(() => auth.hasPermission(PERMISSIONS.TRANSFER_CREATE))
const canValidate = computed(() => auth.hasPermission(PERMISSIONS.TRANSFER_VALIDATE))

type TStatus = 'draft' | 'validated' | 'shipped' | 'received' | 'cancelled'

const search     = ref('')
const statusFilter = ref('all')

const transfers = ref([
  { id: 'TRF-2024-089', product: 'RAM DDR5 16Go',     from: 'Paris Nord', to: 'Lyon Est',   qty: 120, date: '2024-01-15', status: 'shipped'   as TStatus, progress: 65 },
  { id: 'TRF-2024-087', product: 'Disque SSD 1To',    from: 'Lyon Est',   to: 'Marseille',  qty: 80,  date: '2024-01-14', status: 'shipped'   as TStatus, progress: 30 },
  { id: 'TRF-2024-085', product: 'Écran 27" 4K',      from: 'Paris Nord', to: 'Bordeaux',   qty: 15,  date: '2024-01-14', status: 'received'  as TStatus, progress: 100 },
  { id: 'TRF-2024-083', product: 'Câble USB-C 2m',    from: 'Marseille',  to: 'Paris Nord', qty: 200, date: '2024-01-13', status: 'validated' as TStatus, progress: 0 },
  { id: 'TRF-2024-081', product: 'Boîtier PC Tour',   from: 'Bordeaux',   to: 'Lyon Est',   qty: 30,  date: '2024-01-12', status: 'draft'     as TStatus, progress: 0 },
  { id: 'TRF-2024-078', product: 'Frigo 250L A++',    from: 'Lyon Est',   to: 'Nantes',     qty: 5,   date: '2024-01-10', status: 'cancelled' as TStatus, progress: 0 },
])

const statusOpts = [
  { v: 'all',       l: 'Tous' },
  { v: 'draft',     l: 'Brouillon' },
  { v: 'validated', l: 'Validés' },
  { v: 'shipped',   l: 'Expédiés' },
  { v: 'received',  l: 'Reçus' },
  { v: 'cancelled', l: 'Annulés' },
]

const filtered = computed(() => transfers.value
  .filter(t => statusFilter.value === 'all' || t.status === statusFilter.value)
  .filter(t => !search.value || t.id.toLowerCase().includes(search.value.toLowerCase()) || t.product.toLowerCase().includes(search.value.toLowerCase()))
)
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Transferts"
      subtitle="Mouvements inter-entrepôts"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Transferts' }]"
    >
      <template #actions>
        <Button v-if="canCreate" variant="accent" size="sm"><Plus class="w-4 h-4" /> Nouveau transfert</Button>
      </template>
    </PageHeader>

    <!-- KPIs -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div v-for="kpi in [
        { l: 'Total',      v: transfers.length,                               c: 'text-primary' },
        { l: 'En transit', v: transfers.filter(t=>t.status==='shipped').length,  c: 'text-info'    },
        { l: 'En attente', v: transfers.filter(t=>t.status==='validated').length, c: 'text-warning' },
        { l: 'Reçus',      v: transfers.filter(t=>t.status==='received').length, c: 'text-success' },
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
          <input v-model="search" placeholder="N° transfert, produit…"
            class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
        </div>
        <div class="flex gap-1 bg-background rounded-lg p-1 border border-border flex-wrap">
          <button v-for="opt in statusOpts" :key="opt.v" @click="statusFilter = opt.v"
            :class="['px-3 py-1.5 rounded-md text-xs font-semibold transition-fast', statusFilter === opt.v ? 'bg-surface shadow-sm text-primary border border-border-light' : 'text-text-muted hover:text-text-main']">
            {{ opt.l }}
          </button>
        </div>
      </div>
    </BaseCard>

    <BaseCard padding="none">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-border-light bg-background">
              <th class="text-left px-5 py-3 text-label">Référence</th>
              <th class="text-left px-5 py-3 text-label">Produit</th>
              <th class="text-left px-5 py-3 text-label">Trajet</th>
              <th class="text-left px-5 py-3 text-label">Quantité</th>
              <th class="text-left px-5 py-3 text-label">Progression</th>
              <th class="text-left px-5 py-3 text-label">Date</th>
              <th class="text-left px-5 py-3 text-label">Statut</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody class="divide-y divide-border-light">
            <tr v-for="t in filtered" :key="t.id" class="hover:bg-background transition-fast group">
              <td class="px-5 py-3.5 font-mono text-xs font-bold text-primary">{{ t.id }}</td>
              <td class="px-5 py-3.5 font-semibold text-text-main">{{ t.product }}</td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1.5 text-sm">
                  <span class="font-medium text-text-secondary">{{ t.from }}</span>
                  <ChevronRight class="w-3.5 h-3.5 text-text-muted shrink-0" />
                  <span class="font-medium text-text-secondary">{{ t.to }}</span>
                </div>
              </td>
              <td class="px-5 py-3.5 font-bold text-text-main">{{ t.qty }}</td>
              <td class="px-5 py-3.5 min-w-32">
                <div v-if="t.status === 'shipped'" class="flex items-center gap-2">
                  <div class="flex-1 h-1.5 bg-border-light rounded-full overflow-hidden">
                    <div class="h-full rounded-full bg-primary transition-slow" :style="{ width: `${t.progress}%` }" />
                  </div>
                  <span class="text-xs font-semibold text-primary">{{ t.progress }}%</span>
                </div>
                <span v-else class="text-text-subtle text-xs">—</span>
              </td>
              <td class="px-5 py-3.5 text-text-muted text-xs">{{ t.date }}</td>
              <td class="px-5 py-3.5"><Badge :variant="t.status" size="sm" dot>{{ t.status }}</Badge></td>
              <td class="px-5 py-3.5">
                <div class="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-fast">
                  <button v-if="canValidate && t.status === 'draft'" class="px-2.5 py-1 text-xs font-semibold text-success bg-success-light rounded-lg hover:bg-green-100 transition-fast">Valider</button>
                  <button v-if="t.status === 'shipped'" class="px-2.5 py-1 text-xs font-semibold text-info bg-info-light rounded-lg hover:bg-blue-100 transition-fast">Réceptionner</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-if="filtered.length === 0" :icon="ArrowLeftRight" title="Aucun transfert trouvé" />
      </div>
      <div class="px-5 py-3 border-t border-border-light text-caption">{{ filtered.length }} transfert{{ filtered.length > 1 ? 's' : '' }}</div>
    </BaseCard>
  </div>
</template>
