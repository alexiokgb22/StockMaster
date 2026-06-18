<script setup lang="ts">
import { ref, computed } from 'vue'
import { ShoppingCart, Plus, Search, ChevronRight } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

type POStatus = 'draft' | 'validated' | 'shipped' | 'received' | 'cancelled'
const search = ref('')
const statusFilter = ref('all')

const orders = ref([
  { id: 'CMD-2024-0312', supplier: 'TechPro Distribution', warehouse: 'Paris Nord',   amount: 4580.00, orderDate: '2024-01-15', expectedDate: '2024-01-20', status: 'validated' as POStatus, lines: 8  },
  { id: 'CMD-2024-0298', supplier: 'ElectoSupply SAS',      warehouse: 'Lyon Est',    amount: 12340.00, orderDate: '2024-01-12', expectedDate: '2024-01-18', status: 'received'  as POStatus, lines: 15 },
  { id: 'CMD-2024-0285', supplier: 'AutoPieces Pro',        warehouse: 'Bordeaux',    amount: 2890.50, orderDate: '2024-01-10', expectedDate: '2024-01-16', status: 'shipped'   as POStatus, lines: 6  },
  { id: 'CMD-2024-0271', supplier: 'FoodLogist',            warehouse: 'Nantes',      amount: 780.00,  orderDate: '2024-01-08', expectedDate: '2024-01-14', status: 'received'  as POStatus, lines: 4  },
  { id: 'CMD-2024-0260', supplier: 'TechPro Distribution',  warehouse: 'Marseille',   amount: 9120.00, orderDate: '2024-01-05', expectedDate: '2024-01-22', status: 'draft'     as POStatus, lines: 12 },
  { id: 'CMD-2024-0244', supplier: 'OldTech SARL',          warehouse: 'Paris Nord',  amount: 340.00,  orderDate: '2024-01-02', expectedDate: '2024-01-10', status: 'cancelled' as POStatus, lines: 2  },
])

const statusOpts = ['all', 'draft', 'validated', 'shipped', 'received', 'cancelled']

const filtered = computed(() => orders.value
  .filter(o => statusFilter.value === 'all' || o.status === statusFilter.value)
  .filter(o => !search.value || o.id.toLowerCase().includes(search.value.toLowerCase()) || o.supplier.toLowerCase().includes(search.value.toLowerCase()))
)
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Commandes Fournisseurs"
      subtitle="Suivi des commandes d'approvisionnement"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Commandes' }]"
    >
      <template #actions>
        <Button variant="accent" size="sm"><Plus class="w-4 h-4" /> Nouvelle commande</Button>
      </template>
    </PageHeader>

    <!-- KPIs -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div v-for="kpi in [
        { l: 'Total',      v: orders.length,                                 c: 'text-primary' },
        { l: 'En attente', v: orders.filter(o=>o.status==='validated').length, c: 'text-warning' },
        { l: 'En livraison',v: orders.filter(o=>o.status==='shipped').length,  c: 'text-info'    },
        { l: 'Livrées',    v: orders.filter(o=>o.status==='received').length,  c: 'text-success' },
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
          <input v-model="search" placeholder="N° commande, fournisseur…"
            class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
        </div>
        <div class="flex gap-1 bg-background rounded-lg p-1 border border-border flex-wrap">
          <button v-for="s in statusOpts" :key="s" @click="statusFilter = s"
            :class="['px-3 py-1.5 rounded-md text-xs font-semibold transition-fast capitalize', statusFilter === s ? 'bg-surface shadow-sm text-primary border border-border-light' : 'text-text-muted hover:text-text-main']">
            {{ s === 'all' ? 'Tous' : s }}
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
              <th class="text-left px-5 py-3 text-label">Fournisseur</th>
              <th class="text-left px-5 py-3 text-label">Entrepôt</th>
              <th class="text-left px-5 py-3 text-label">Montant</th>
              <th class="text-left px-5 py-3 text-label">Date cde</th>
              <th class="text-left px-5 py-3 text-label">Livraison prévue</th>
              <th class="text-left px-5 py-3 text-label">Lignes</th>
              <th class="text-left px-5 py-3 text-label">Statut</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody class="divide-y divide-border-light">
            <tr v-for="o in filtered" :key="o.id" class="hover:bg-background transition-fast group">
              <td class="px-5 py-3.5 font-mono text-xs font-bold text-primary">{{ o.id }}</td>
              <td class="px-5 py-3.5 font-semibold text-text-main">{{ o.supplier }}</td>
              <td class="px-5 py-3.5 text-text-muted">{{ o.warehouse }}</td>
              <td class="px-5 py-3.5 font-bold text-text-main">{{ o.amount.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' }) }}</td>
              <td class="px-5 py-3.5 text-text-muted text-xs">{{ o.orderDate }}</td>
              <td class="px-5 py-3.5 text-text-muted text-xs">{{ o.expectedDate }}</td>
              <td class="px-5 py-3.5 font-semibold text-text-main">{{ o.lines }}</td>
              <td class="px-5 py-3.5"><Badge :variant="o.status" size="sm" dot>{{ o.status }}</Badge></td>
              <td class="px-5 py-3.5">
                <div class="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-fast">
                  <button v-if="o.status === 'draft'" class="px-2.5 py-1 text-xs font-semibold text-success bg-success-light rounded-lg">Valider</button>
                  <button v-if="o.status === 'shipped'" class="px-2.5 py-1 text-xs font-semibold text-primary bg-primary-100 rounded-lg">Réceptionner</button>
                  <button class="px-2.5 py-1 text-xs font-semibold text-text-muted bg-background rounded-lg border border-border">Détail</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-if="filtered.length === 0" :icon="ShoppingCart" title="Aucune commande" />
      </div>
      <div class="px-5 py-3 border-t border-border-light text-caption">{{ filtered.length }} commande{{ filtered.length > 1 ? 's' : '' }}</div>
    </BaseCard>
  </div>
</template>
