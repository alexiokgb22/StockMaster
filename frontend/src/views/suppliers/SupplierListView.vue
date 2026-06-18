<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { Truck, Plus, Search, Mail, Phone } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const auth = useAuthStore()
const canCreate = computed(() => auth.hasPermission(PERMISSIONS.SUPPLIER_CREATE))
const canUpdate = computed(() => auth.hasPermission(PERMISSIONS.SUPPLIER_UPDATE))

const search = ref('')
const suppliers = ref([
  { id: 1, name: 'TechPro Distribution',  city: 'Paris',    contact: 'Marc Duval',    email: 'marc@techpro.fr',     phone: '01 23 45 67 89', orders: 34, active: true  },
  { id: 2, name: 'ElectoSupply SAS',       city: 'Lyon',     contact: 'Julie Bernard', email: 'j.bernard@electo.fr', phone: '04 56 78 90 12', orders: 21, active: true  },
  { id: 3, name: 'AutoPieces Pro',         city: 'Bordeaux', contact: 'Paul Renaud',   email: 'p.renaud@autopro.fr', phone: '05 34 56 78 90', orders: 18, active: true  },
  { id: 4, name: 'FoodLogist',             city: 'Nantes',   contact: 'Sara Martin',   email: 's.martin@foodlog.fr', phone: '02 12 34 56 78', orders: 12, active: true  },
  { id: 5, name: 'OldTech SARL',           city: 'Toulouse', contact: 'Jean Vieux',    email: 'j.vieux@oldtech.fr',  phone: '05 61 00 00 00', orders: 2,  active: false },
])

const filtered = computed(() => suppliers.value.filter(s =>
  !search.value || s.name.toLowerCase().includes(search.value.toLowerCase()) || s.city.toLowerCase().includes(search.value.toLowerCase())
))
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Fournisseurs"
      subtitle="Gestion du panel fournisseurs"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Fournisseurs' }]"
    >
      <template #actions>
        <Button v-if="canCreate" variant="accent" size="sm"><Plus class="w-4 h-4" /> Nouveau fournisseur</Button>
      </template>
    </PageHeader>

    <BaseCard padding="sm">
      <div class="relative max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
        <input v-model="search" placeholder="Nom, ville…"
          class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
      </div>
    </BaseCard>

    <BaseCard padding="none">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-border-light bg-background">
              <th class="text-left px-5 py-3 text-label">Fournisseur</th>
              <th class="text-left px-5 py-3 text-label">Contact</th>
              <th class="text-left px-5 py-3 text-label">Téléphone</th>
              <th class="text-left px-5 py-3 text-label">Commandes</th>
              <th class="text-left px-5 py-3 text-label">Statut</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody class="divide-y divide-border-light">
            <tr v-for="s in filtered" :key="s.id" class="hover:bg-background transition-fast group">
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 rounded-xl bg-primary-100 flex items-center justify-center shrink-0 text-sm font-bold text-primary">
                    {{ s.name[0] }}
                  </div>
                  <div>
                    <p class="font-semibold text-text-main">{{ s.name }}</p>
                    <p class="text-xs text-text-muted">{{ s.city }}</p>
                  </div>
                </div>
              </td>
              <td class="px-5 py-3.5">
                <p class="text-text-secondary font-medium">{{ s.contact }}</p>
                <div class="flex items-center gap-1 text-xs text-text-muted mt-0.5">
                  <Mail class="w-3 h-3" />{{ s.email }}
                </div>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1.5 text-text-secondary">
                  <Phone class="w-3.5 h-3.5 text-text-muted" />{{ s.phone }}
                </div>
              </td>
              <td class="px-5 py-3.5 font-bold text-text-main">{{ s.orders }}</td>
              <td class="px-5 py-3.5"><Badge :variant="s.active ? 'active' : 'inactive'" dot>{{ s.active ? 'Actif' : 'Inactif' }}</Badge></td>
              <td class="px-5 py-3.5">
                <button v-if="canUpdate" class="opacity-0 group-hover:opacity-100 transition-fast px-2.5 py-1 text-xs font-semibold text-primary bg-primary-100 rounded-lg hover:bg-primary-200">Modifier</button>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-if="filtered.length === 0" :icon="Truck" title="Aucun fournisseur" />
      </div>
      <div class="px-5 py-3 border-t border-border-light text-caption">{{ filtered.length }} fournisseur{{ filtered.length > 1 ? 's' : '' }}</div>
    </BaseCard>
  </div>
</template>
