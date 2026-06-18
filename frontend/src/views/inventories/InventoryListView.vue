<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { ClipboardList, Plus, Search, Play, CheckCircle } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const auth = useAuthStore()
const canCreate   = computed(() => auth.hasPermission(PERMISSIONS.INVENTORY_CREATE))
const canStart    = computed(() => auth.hasPermission(PERMISSIONS.INVENTORY_START))
const canComplete = computed(() => auth.hasPermission(PERMISSIONS.INVENTORY_COMPLETE))

type IStatus = 'draft' | 'validated' | 'shipped' | 'received'
const statusMap: Record<string, IStatus> = {
  PLANNED:    'draft',
  IN_PROGRESS:'validated',
  COMPLETED:  'received',
}

const search = ref('')
const inventories = ref([
  { id: 'INV-2024-014', warehouse: 'Marseille',   type: 'Partiel',  zone: 'Zone B',  startedAt: '2024-01-15 09:00', completedAt: null,            items: 0,   gaps: 0,  status: 'IN_PROGRESS', createdBy: 'P. Blanc'   },
  { id: 'INV-2024-013', warehouse: 'Paris Nord',  type: 'Complet',  zone: 'Toutes',  startedAt: '2024-01-10 08:00', completedAt: '2024-01-10 18:00', items: 340, gaps: 7,  status: 'COMPLETED',   createdBy: 'J. Martin'  },
  { id: 'INV-2024-012', warehouse: 'Lyon Est',    type: 'Partiel',  zone: 'Zone A',  startedAt: '2024-01-08 10:00', completedAt: '2024-01-08 14:00', items: 112, gaps: 2,  status: 'COMPLETED',   createdBy: 'S. Durand'  },
  { id: 'INV-2024-011', warehouse: 'Bordeaux',    type: 'Complet',  zone: 'Toutes',  startedAt: null,               completedAt: null,            items: 0,   gaps: 0,  status: 'PLANNED',     createdBy: 'C. Petit'   },
])

const filtered = computed(() => inventories.value.filter(i =>
  !search.value || i.id.toLowerCase().includes(search.value.toLowerCase()) || i.warehouse.toLowerCase().includes(search.value.toLowerCase())
))
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Inventaires"
      subtitle="Sessions de comptage et rapprochement théorie/réalité"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Inventaires' }]"
    >
      <template #actions>
        <Button v-if="canCreate" variant="accent" size="sm"><Plus class="w-4 h-4" /> Nouvel inventaire</Button>
      </template>
    </PageHeader>

    <BaseCard padding="sm">
      <div class="relative max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
        <input v-model="search" placeholder="N° inventaire, entrepôt…"
          class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
      </div>
    </BaseCard>

    <BaseCard padding="none">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-border-light bg-background">
            <th class="text-left px-5 py-3 text-label">Référence</th>
            <th class="text-left px-5 py-3 text-label">Entrepôt / Zone</th>
            <th class="text-left px-5 py-3 text-label">Type</th>
            <th class="text-left px-5 py-3 text-label">Démarré le</th>
            <th class="text-left px-5 py-3 text-label">Articles</th>
            <th class="text-left px-5 py-3 text-label">Écarts</th>
            <th class="text-left px-5 py-3 text-label">Statut</th>
            <th class="px-5 py-3" />
          </tr>
        </thead>
        <tbody class="divide-y divide-border-light">
          <tr v-for="i in filtered" :key="i.id" class="hover:bg-background transition-fast group">
            <td class="px-5 py-3.5 font-mono text-xs font-bold text-primary">{{ i.id }}</td>
            <td class="px-5 py-3.5">
              <p class="font-semibold text-text-main">{{ i.warehouse }}</p>
              <p class="text-xs text-text-muted">{{ i.zone }}</p>
            </td>
            <td class="px-5 py-3.5"><Badge variant="info" size="sm">{{ i.type }}</Badge></td>
            <td class="px-5 py-3.5 text-text-muted text-xs">{{ i.startedAt || '—' }}</td>
            <td class="px-5 py-3.5 font-bold text-text-main">{{ i.items || '—' }}</td>
            <td class="px-5 py-3.5">
              <span v-if="i.gaps > 0" class="font-bold text-error">{{ i.gaps }}</span>
              <span v-else-if="i.status === 'COMPLETED'" class="font-bold text-success">0</span>
              <span v-else class="text-text-subtle">—</span>
            </td>
            <td class="px-5 py-3.5"><Badge :variant="statusMap[i.status]" size="sm" dot>{{ i.status }}</Badge></td>
            <td class="px-5 py-3.5">
              <div class="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-fast">
                <button v-if="canStart && i.status === 'PLANNED'" class="px-2.5 py-1 text-xs font-semibold text-success bg-success-light rounded-lg flex items-center gap-1">
                  <Play class="w-3 h-3" /> Démarrer
                </button>
                <button v-if="canComplete && i.status === 'IN_PROGRESS'" class="px-2.5 py-1 text-xs font-semibold text-primary bg-primary-100 rounded-lg flex items-center gap-1">
                  <CheckCircle class="w-3 h-3" /> Clore
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <EmptyState v-if="filtered.length === 0" :icon="ClipboardList" title="Aucun inventaire" />
      <div class="px-5 py-3 border-t border-border-light text-caption">{{ filtered.length }} inventaire{{ filtered.length > 1 ? 's' : '' }}</div>
    </BaseCard>
  </div>
</template>
