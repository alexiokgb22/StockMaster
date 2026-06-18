<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { Layers, Search, Play, FileText, User, Clock } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'

const auth = useAuthStore()
const canStart        = computed(() => auth.hasPermission(PERMISSIONS.AUDIT_START))
const canCreateReport = computed(() => auth.hasPermission(PERMISSIONS.AUDIT_REPORT_CREATE))

const search = ref('')
const tab = ref<'sessions' | 'journal'>('sessions')

const sessions = ref([
  { id: 'AUD-2024-005', title: 'Audit Q1 2024',          auditor: 'audit.duval', warehouses: ['Paris Nord', 'Lyon Est'],  startedAt: '2024-01-10', completedAt: null,          status: 'IN_PROGRESS', reports: 1 },
  { id: 'AUD-2023-012', title: 'Audit Annuel 2023',       auditor: 'audit.duval', warehouses: ['Tous'],                    startedAt: '2023-12-01', completedAt: '2023-12-15',   status: 'COMPLETED',   reports: 4 },
  { id: 'AUD-2023-009', title: 'Audit surprise Oct',      auditor: 'audit.duval', warehouses: ['Marseille'],               startedAt: '2023-10-15', completedAt: '2023-10-16',   status: 'COMPLETED',   reports: 1 },
])

const journal = ref([
  { id: 1, user: 'g.martin',    action: 'Validation transfert',   module: 'Transferts',  detail: 'TRF-2024-089 validé',                 at: '2024-01-15 09:45' },
  { id: 2, user: 'admin',       action: 'Création utilisateur',   module: 'Utilisateurs', detail: 'Compte a.moreau créé',                at: '2024-01-15 09:10' },
  { id: 3, user: 's.dupont',    action: 'Réception stock',        module: 'Stocks',      detail: 'CMD-2024-0312 — 45 articles réceptionnés', at: '2024-01-15 08:50' },
  { id: 4, user: 'audit.duval', action: 'Rapport d\'audit',       module: 'Audit',       detail: 'AUD-2024-005 — Rapport rédigé',        at: '2024-01-15 08:20' },
  { id: 5, user: 'p.blanc',     action: 'Inventaire démarré',     module: 'Inventaires', detail: 'INV-2024-014 lancé — Zone B Marseille', at: '2024-01-15 07:55' },
  { id: 6, user: 'g.martin',    action: 'Ajustement stock',       module: 'Stocks',      detail: 'REF-0234 ajusté : 3 → 53 unités',      at: '2024-01-14 17:30' },
])

const filteredJournal = computed(() => journal.value.filter(j =>
  !search.value || j.user.toLowerCase().includes(search.value.toLowerCase()) || j.action.toLowerCase().includes(search.value.toLowerCase()) || j.module.toLowerCase().includes(search.value.toLowerCase())
))

const sessionStatusBadge: Record<string, 'validated' | 'received'> = { IN_PROGRESS: 'validated', COMPLETED: 'received' }
const moduleBadge: Record<string, 'info' | 'success' | 'warning' | 'shipped' | 'default'> = {
  Transferts: 'info', Utilisateurs: 'shipped', Stocks: 'success', Audit: 'warning', Inventaires: 'default',
}
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Audit & Traçabilité"
      subtitle="Journal des actions et sessions d'audit"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Audit' }]"
    >
      <template #actions>
        <Button v-if="canStart" variant="accent" size="sm"><Play class="w-4 h-4" /> Lancer un audit</Button>
      </template>
    </PageHeader>

    <!-- Onglets -->
    <div class="flex gap-1 bg-background rounded-xl p-1 border border-border w-fit">
      <button v-for="t in [{ v: 'sessions', l: 'Sessions d\'audit' }, { v: 'journal', l: 'Journal des actions' }]"
        :key="t.v" @click="tab = t.v as any"
        :class="['px-4 py-2 rounded-lg text-sm font-semibold transition-fast', tab === t.v ? 'bg-surface shadow-sm text-primary border border-border-light' : 'text-text-muted hover:text-text-main']">
        {{ t.l }}
      </button>
    </div>

    <!-- Sessions -->
    <template v-if="tab === 'sessions'">
      <BaseCard padding="none">
        <div class="px-5 py-4 border-b border-border-light"><h2 class="heading-4">Sessions d'Audit</h2></div>
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-border-light bg-background">
              <th class="text-left px-5 py-3 text-label">Session</th>
              <th class="text-left px-5 py-3 text-label">Auditeur</th>
              <th class="text-left px-5 py-3 text-label">Entrepôts</th>
              <th class="text-left px-5 py-3 text-label">Démarré</th>
              <th class="text-left px-5 py-3 text-label">Clos</th>
              <th class="text-left px-5 py-3 text-label">Rapports</th>
              <th class="text-left px-5 py-3 text-label">Statut</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody class="divide-y divide-border-light">
            <tr v-for="s in sessions" :key="s.id" class="hover:bg-background transition-fast group">
              <td class="px-5 py-3.5">
                <p class="font-mono text-xs font-bold text-primary">{{ s.id }}</p>
                <p class="font-semibold text-text-main text-sm mt-0.5">{{ s.title }}</p>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1.5 text-text-secondary">
                  <User class="w-3.5 h-3.5 text-text-muted" />{{ s.auditor }}
                </div>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex flex-wrap gap-1">
                  <span v-for="w in s.warehouses" :key="w" class="text-xs bg-primary-100 text-primary px-2 py-0.5 rounded-full font-medium">{{ w }}</span>
                </div>
              </td>
              <td class="px-5 py-3.5 text-text-muted text-xs">{{ s.startedAt }}</td>
              <td class="px-5 py-3.5 text-text-muted text-xs">{{ s.completedAt || '—' }}</td>
              <td class="px-5 py-3.5 font-bold text-text-main">{{ s.reports }}</td>
              <td class="px-5 py-3.5"><Badge :variant="sessionStatusBadge[s.status]" size="sm" dot>{{ s.status }}</Badge></td>
              <td class="px-5 py-3.5">
                <button v-if="canCreateReport && s.status === 'IN_PROGRESS'" class="opacity-0 group-hover:opacity-100 transition-fast flex items-center gap-1 px-2.5 py-1 text-xs font-semibold text-primary bg-primary-100 rounded-lg">
                  <FileText class="w-3.5 h-3.5" /> Rédiger
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </BaseCard>
    </template>

    <!-- Journal -->
    <template v-else>
      <BaseCard padding="sm">
        <div class="relative max-w-sm">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
          <input v-model="search" placeholder="Utilisateur, action, module…"
            class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
        </div>
      </BaseCard>

      <BaseCard padding="none">
        <div class="px-5 py-4 border-b border-border-light"><h2 class="heading-4">Journal des Actions</h2></div>
        <ul class="divide-y divide-border-light">
          <li v-for="j in filteredJournal" :key="j.id" class="flex items-center gap-4 px-5 py-3.5 hover:bg-background transition-fast">
            <div class="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center shrink-0 text-xs font-bold text-primary">
              {{ j.user[0].toUpperCase() }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="font-semibold text-text-main text-sm">{{ j.action }}</span>
                <Badge :variant="moduleBadge[j.module] ?? 'default'" size="sm">{{ j.module }}</Badge>
              </div>
              <p class="text-xs text-text-muted mt-0.5 truncate">{{ j.detail }}</p>
            </div>
            <div class="text-right shrink-0">
              <p class="text-xs font-medium text-text-secondary">{{ j.user }}</p>
              <div class="flex items-center gap-1 text-xs text-text-muted mt-0.5">
                <Clock class="w-3 h-3" />{{ j.at }}
              </div>
            </div>
          </li>
        </ul>
      </BaseCard>
    </template>
  </div>
</template>
