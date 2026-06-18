<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { Users, Plus, Search, Shield, UserCheck, UserX } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const auth = useAuthStore()
const canCreate          = computed(() => auth.hasPermission(PERMISSIONS.USER_CREATE))
const canCreateStorekeeper = computed(() => auth.hasPermission(PERMISSIONS.USER_CREATE_STOREKEEPER))
const canToggle          = computed(() => auth.hasPermission(PERMISSIONS.USER_TOGGLE) || auth.hasPermission(PERMISSIONS.USER_TOGGLE_STOREKEEPER))
const canAssignRole      = computed(() => auth.hasPermission(PERMISSIONS.USER_ASSIGN_ROLE))
const showCreate = computed(() => canCreate.value || canCreateStorekeeper.value)

const search = ref('')
const roleFilter = ref('all')

const users = ref([
  { id: 1, username: 'admin',        email: 'admin@stockmaster.com',     role: 'Administrateur',         warehouse: null,          active: true,  lastLogin: '2024-01-15 08:32' },
  { id: 2, username: 'g.martin',     email: 'g.martin@stockmaster.com',  role: 'Gestionnaire d\'entrepôt', warehouse: 'Paris Nord',  active: true,  lastLogin: '2024-01-15 09:10' },
  { id: 3, username: 's.dupont',     email: 's.dupont@stockmaster.com',  role: 'Magasinier',             warehouse: 'Paris Nord',  active: true,  lastLogin: '2024-01-15 07:45' },
  { id: 4, username: 'p.blanc',      email: 'p.blanc@stockmaster.com',   role: 'Gestionnaire d\'entrepôt', warehouse: 'Marseille',   active: true,  lastLogin: '2024-01-14 16:20' },
  { id: 5, username: 'a.moreau',     email: 'a.moreau@stockmaster.com',  role: 'Magasinier',             warehouse: 'Lyon Est',    active: true,  lastLogin: '2024-01-15 08:00' },
  { id: 6, username: 'audit.duval',  email: 'audit.duval@stockmaster.com', role: 'Auditeur',             warehouse: null,          active: true,  lastLogin: '2024-01-13 11:00' },
  { id: 7, username: 'j.vieux',      email: 'j.vieux@stockmaster.com',   role: 'Magasinier',             warehouse: 'Bordeaux',    active: false, lastLogin: '2023-12-01 10:00' },
])

const roles = computed(() => ['all', ...new Set(users.value.map(u => u.role))])

const filtered = computed(() => users.value
  .filter(u => roleFilter.value === 'all' || u.role === roleFilter.value)
  .filter(u => !search.value || u.username.toLowerCase().includes(search.value.toLowerCase()) || u.email.toLowerCase().includes(search.value.toLowerCase()))
)

const roleBadge: Record<string, 'info' | 'success' | 'warning' | 'shipped'> = {
  'Administrateur':         'info',
  "Gestionnaire d'entrepôt": 'success',
  'Magasinier':             'warning',
  'Auditeur':               'shipped',
}
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Utilisateurs"
      subtitle="Gestion des comptes et des rôles"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Utilisateurs' }]"
    >
      <template #actions>
        <Button v-if="showCreate" variant="accent" size="sm"><Plus class="w-4 h-4" /> Nouvel utilisateur</Button>
      </template>
    </PageHeader>

    <!-- KPIs -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div v-for="kpi in [
        { l: 'Total',        v: users.length,                              c: 'text-primary' },
        { l: 'Actifs',       v: users.filter(u=>u.active).length,          c: 'text-success' },
        { l: 'Inactifs',     v: users.filter(u=>!u.active).length,         c: 'text-error'   },
        { l: 'Gestionnaires',v: users.filter(u=>u.role.includes('Gestion')).length, c: 'text-info' },
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
          <input v-model="search" placeholder="Nom, email…"
            class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
        </div>
        <select v-model="roleFilter" class="px-3 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary transition-base text-text-main">
          <option v-for="r in roles" :key="r" :value="r">{{ r === 'all' ? 'Tous les rôles' : r }}</option>
        </select>
      </div>
    </BaseCard>

    <BaseCard padding="none">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-border-light bg-background">
              <th class="text-left px-5 py-3 text-label">Utilisateur</th>
              <th class="text-left px-5 py-3 text-label">Rôle</th>
              <th class="text-left px-5 py-3 text-label">Entrepôt</th>
              <th class="text-left px-5 py-3 text-label">Dernière connexion</th>
              <th class="text-left px-5 py-3 text-label">Statut</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody class="divide-y divide-border-light">
            <tr v-for="u in filtered" :key="u.id" class="hover:bg-background transition-fast group">
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 rounded-full flex items-center justify-center shrink-0 text-sm font-bold text-white" style="background: var(--gradient-dark)">
                    {{ u.username[0].toUpperCase() }}
                  </div>
                  <div>
                    <p class="font-semibold text-text-main">{{ u.username }}</p>
                    <p class="text-xs text-text-muted">{{ u.email }}</p>
                  </div>
                </div>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1.5">
                  <Shield class="w-3.5 h-3.5 text-text-muted" />
                  <Badge :variant="roleBadge[u.role] ?? 'default'" size="sm">{{ u.role }}</Badge>
                </div>
              </td>
              <td class="px-5 py-3.5 text-text-muted">{{ u.warehouse || '—' }}</td>
              <td class="px-5 py-3.5 text-text-muted text-xs">{{ u.lastLogin }}</td>
              <td class="px-5 py-3.5"><Badge :variant="u.active ? 'active' : 'inactive'" dot>{{ u.active ? 'Actif' : 'Inactif' }}</Badge></td>
              <td class="px-5 py-3.5">
                <div class="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-fast">
                  <button v-if="canAssignRole" class="p-1.5 rounded-lg text-text-muted hover:text-primary hover:bg-primary-100 transition-fast" title="Gérer rôle">
                    <Shield class="w-4 h-4" />
                  </button>
                  <button v-if="canToggle" class="p-1.5 rounded-lg transition-fast" :class="u.active ? 'text-text-muted hover:text-error hover:bg-error-light' : 'text-text-muted hover:text-success hover:bg-success-light'" :title="u.active ? 'Désactiver' : 'Activer'">
                    <component :is="u.active ? UserX : UserCheck" class="w-4 h-4" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-if="filtered.length === 0" :icon="Users" title="Aucun utilisateur trouvé" />
      </div>
      <div class="px-5 py-3 border-t border-border-light text-caption">{{ filtered.length }} utilisateur{{ filtered.length > 1 ? 's' : '' }}</div>
    </BaseCard>
  </div>
</template>
