<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { Grid3X3, Plus, Search, Warehouse } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const auth = useAuthStore()
const canCreate = computed(() => auth.hasPermission(PERMISSIONS.ZONE_CREATE))
const canUpdate = computed(() => auth.hasPermission(PERMISSIONS.ZONE_UPDATE))

const search = ref('')
const zones = ref([
  { id: 1, name: 'Zone A — Réception',  warehouse: 'Paris Nord',   type: 'RECEPTION',  capacity: 800,  used: 650, active: true  },
  { id: 2, name: 'Zone B — Stockage',   warehouse: 'Paris Nord',   type: 'STORAGE',    capacity: 2000, used: 1980, active: true  },
  { id: 3, name: 'Zone C — Expédition', warehouse: 'Paris Nord',   type: 'EXPEDITION', capacity: 600,  used: 240, active: true  },
  { id: 4, name: 'Zone A — Réception',  warehouse: 'Lyon Est',     type: 'RECEPTION',  capacity: 500,  used: 310, active: true  },
  { id: 5, name: 'Zone B — Stockage',   warehouse: 'Lyon Est',     type: 'STORAGE',    capacity: 1500, used: 900, active: true  },
  { id: 6, name: 'Zone D — Quarantaine',warehouse: 'Marseille',    type: 'QUARANTINE', capacity: 200,  used: 45,  active: true  },
  { id: 7, name: 'Zone A — Réception',  warehouse: 'Bordeaux',     type: 'RECEPTION',  capacity: 400,  used: 0,   active: false },
])

const typeLabels: Record<string, string> = {
  RECEPTION:  'Réception',
  STORAGE:    'Stockage',
  EXPEDITION: 'Expédition',
  QUARANTINE: 'Quarantaine',
}
const typeBadge: Record<string, 'info' | 'success' | 'shipped' | 'warning'> = {
  RECEPTION:  'info',
  STORAGE:    'success',
  EXPEDITION: 'shipped',
  QUARANTINE: 'warning',
}

const filtered = computed(() => zones.value.filter(z =>
  !search.value || z.name.toLowerCase().includes(search.value.toLowerCase()) || z.warehouse.toLowerCase().includes(search.value.toLowerCase())
))

function pct(z: typeof zones.value[0]) { return z.capacity ? Math.round((z.used / z.capacity) * 100) : 0 }
function pctColor(p: number) { return p >= 90 ? '#ef4444' : p >= 70 ? '#f59e0b' : '#10b981' }
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Zones de Stockage"
      subtitle="Organisation spatiale des entrepôts"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Zones' }]"
    >
      <template #actions>
        <Button v-if="canCreate" variant="accent" size="sm">
          <Plus class="w-4 h-4" /> Nouvelle zone
        </Button>
      </template>
    </PageHeader>

    <BaseCard padding="sm">
      <div class="flex items-center gap-3">
        <div class="relative flex-1 min-w-48">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
          <input v-model="search" placeholder="Rechercher une zone…"
            class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
        </div>
      </div>
    </BaseCard>

    <BaseCard padding="none">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-border-light bg-background">
              <th class="text-left px-5 py-3 text-label">Zone</th>
              <th class="text-left px-5 py-3 text-label">Entrepôt</th>
              <th class="text-left px-5 py-3 text-label">Type</th>
              <th class="text-left px-5 py-3 text-label">Occupation</th>
              <th class="text-left px-5 py-3 text-label">Statut</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody class="divide-y divide-border-light">
            <tr v-for="z in filtered" :key="z.id" class="hover:bg-background transition-fast group">
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-lg bg-primary-100 flex items-center justify-center shrink-0">
                    <Grid3X3 class="w-4 h-4 text-primary" />
                  </div>
                  <span class="font-semibold text-text-main">{{ z.name }}</span>
                </div>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1.5 text-text-muted">
                  <Warehouse class="w-3.5 h-3.5" />{{ z.warehouse }}
                </div>
              </td>
              <td class="px-5 py-3.5">
                <Badge :variant="typeBadge[z.type]" size="sm">{{ typeLabels[z.type] }}</Badge>
              </td>
              <td class="px-5 py-3.5 min-w-40">
                <div class="flex items-center gap-2">
                  <div class="flex-1 h-1.5 bg-border-light rounded-full overflow-hidden">
                    <div class="h-full rounded-full" :style="{ width: `${pct(z)}%`, background: pctColor(pct(z)) }" />
                  </div>
                  <span class="text-xs font-bold w-8 text-right" :style="{ color: pctColor(pct(z)) }">{{ pct(z) }}%</span>
                </div>
                <p class="text-xs text-text-muted mt-0.5">{{ z.used }} / {{ z.capacity }} empl.</p>
              </td>
              <td class="px-5 py-3.5">
                <Badge :variant="z.active ? 'active' : 'inactive'" dot>{{ z.active ? 'Active' : 'Inactive' }}</Badge>
              </td>
              <td class="px-5 py-3.5">
                <button v-if="canUpdate" class="opacity-0 group-hover:opacity-100 transition-fast px-2.5 py-1 text-xs font-semibold text-primary bg-primary-100 rounded-lg hover:bg-primary-200">
                  Modifier
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-if="filtered.length === 0" :icon="Grid3X3" title="Aucune zone trouvée" />
      </div>
      <div class="px-5 py-3 border-t border-border-light text-caption">{{ filtered.length }} zone{{ filtered.length > 1 ? 's' : '' }}</div>
    </BaseCard>
  </div>
</template>
