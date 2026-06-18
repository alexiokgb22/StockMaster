<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { Warehouse, Plus, Search, SlidersHorizontal, MoreVertical, MapPin, Users, TrendingUp } from 'lucide-vue-next'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import Modal      from '@/components/ui/Modal.vue'
import FormField  from '@/components/ui/FormField.vue'
import Input      from '@/components/ui/Input.vue'
import Select     from '@/components/ui/Select.vue'
import Textarea   from '@/components/ui/Textarea.vue'

const auth = useAuthStore()
const canCreate  = computed(() => auth.hasPermission(PERMISSIONS.WAREHOUSE_CREATE))
const canUpdate  = computed(() => auth.hasPermission(PERMISSIONS.WAREHOUSE_UPDATE))
const canDisable = computed(() => auth.hasPermission(PERMISSIONS.WAREHOUSE_DISABLE))

const search  = ref('')
const filter  = ref('all')
const openMenu = ref<number | null>(null)
const showModal = ref(false)

// Form data
const form = ref({
  name: '',
  city: '',
  address: '',
  capacity: '',
  manager: '',
  description: ''
})

const warehouses = ref([
  { id: 1, name: 'Paris Nord',   city: 'Paris',     manager: 'Jean Martin',    capacity: 5000, used: 4350, zones: 8,  active: true  },
  { id: 2, name: 'Lyon Est',     city: 'Lyon',      manager: 'Sophie Durand',  capacity: 3000, used: 1860, zones: 5,  active: true  },
  { id: 3, name: 'Marseille',    city: 'Marseille', manager: 'Pierre Blanc',   capacity: 4000, used: 2960, zones: 7,  active: true  },
  { id: 4, name: 'Bordeaux',     city: 'Bordeaux',  manager: 'Claire Petit',   capacity: 2500, used: 1125, zones: 4,  active: true  },
  { id: 5, name: 'Nantes Ouest', city: 'Nantes',    manager: 'Marc Legrand',   capacity: 2000, used: 1680, zones: 3,  active: true  },
  { id: 6, name: 'Lille Nord',   city: 'Lille',     manager: 'Anne Moreau',    capacity: 1800, used: 0,    zones: 2,  active: false },
])

const cityOptions = [
  { value: 'paris', label: 'Paris' },
  { value: 'lyon', label: 'Lyon' },
  { value: 'marseille', label: 'Marseille' },
  { value: 'bordeaux', label: 'Bordeaux' },
  { value: 'nantes', label: 'Nantes' },
  { value: 'lille', label: 'Lille' }
]

const filtered = computed(() => warehouses.value
  .filter(w => filter.value === 'all' || (filter.value === 'active' ? w.active : !w.active))
  .filter(w => !search.value || w.name.toLowerCase().includes(search.value.toLowerCase()) || w.city.toLowerCase().includes(search.value.toLowerCase()))
)

function occupancyPct(w: typeof warehouses.value[0]) {
  return w.capacity ? Math.round((w.used / w.capacity) * 100) : 0
}
function occupancyColor(pct: number) {
  if (pct >= 85) return '#ef4444'
  if (pct >= 70) return '#f59e0b'
  return '#10b981'
}

function openCreateModal() {
  form.value = { name: '', city: '', address: '', capacity: '', manager: '', description: '' }
  showModal.value = true
}

function handleSubmit() {
  console.log('Form submitted:', form.value)
  showModal.value = false
}
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Entrepôts"
      subtitle="Gestion et supervision de tous les entrepôts"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Entrepôts' }]"
    >
      <template #actions>
        <Button v-if="canCreate" variant="accent" size="sm" @click="openCreateModal">
          <Plus class="w-4 h-4" /> Nouvel entrepôt
        </Button>
      </template>
    </PageHeader>

    <!-- Filtres -->
    <BaseCard padding="sm">
      <div class="flex flex-wrap items-center gap-3">
        <div class="relative flex-1 min-w-48">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
          <input v-model="search" placeholder="Rechercher un entrepôt…"
            class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
        </div>
        <div class="flex gap-1 bg-background rounded-lg p-1 border border-border">
          <button v-for="opt in [{ v: 'all', l: 'Tous' }, { v: 'active', l: 'Actifs' }, { v: 'inactive', l: 'Inactifs' }]" :key="opt.v"
            @click="filter = opt.v"
            :class="['px-3 py-1.5 rounded-md text-xs font-semibold transition-fast', filter === opt.v ? 'bg-surface shadow-sm text-primary border border-border-light' : 'text-text-muted hover:text-text-main']">
            {{ opt.l }}
          </button>
        </div>
        <Button variant="secondary" size="sm"><SlidersHorizontal class="w-4 h-4" /> Filtres</Button>
      </div>
    </BaseCard>

    <!-- Table -->
    <BaseCard padding="none">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-border-light bg-background">
              <th class="text-left px-5 py-3 text-label">Entrepôt</th>
              <th class="text-left px-5 py-3 text-label">Ville</th>
              <th class="text-left px-5 py-3 text-label">Responsable</th>
              <th class="text-left px-5 py-3 text-label">Occupation</th>
              <th class="text-left px-5 py-3 text-label">Zones</th>
              <th class="text-left px-5 py-3 text-label">Statut</th>
              <th class="px-5 py-3" />
            </tr>
          </thead>
          <tbody class="divide-y divide-border-light">
          <tr v-for="w in filtered" :key="w.id" class="hover:bg-background transition-fast group">
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 rounded-lg bg-primary-100 flex items-center justify-center shrink-0">
                    <Warehouse class="icon-md text-primary" />
                  </div>
                  <span class="font-semibold text-text-main">{{ w.name }}</span>
                </div>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1.5 text-text-muted">
                  <MapPin class="w-3.5 h-3.5" />{{ w.city }}
                </div>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-1.5 text-text-secondary">
                  <Users class="w-3.5 h-3.5 text-text-muted" />{{ w.manager }}
                </div>
              </td>
              <td class="px-5 py-4 min-w-40">
                <div class="flex items-center gap-2">
                  <div class="flex-1 h-1.5 bg-border-light rounded-full overflow-hidden">
                    <div class="h-full rounded-full transition-slow"
                      :style="{ width: `${occupancyPct(w)}%`, background: occupancyColor(occupancyPct(w)) }" />
                  </div>
                  <span class="text-xs font-bold w-8 text-right" :style="{ color: occupancyColor(occupancyPct(w)) }">
                    {{ occupancyPct(w) }}%
                  </span>
                </div>
                <p class="text-xs text-text-muted mt-0.5">{{ w.used.toLocaleString() }} / {{ w.capacity.toLocaleString() }}</p>
              </td>
              <td class="px-5 py-4">
                <span class="font-semibold text-text-main">{{ w.zones }}</span>
                <span class="text-text-muted text-xs ml-1">zones</span>
              </td>
              <td class="px-5 py-4">
                <Badge :variant="w.active ? 'active' : 'inactive'" dot>{{ w.active ? 'Actif' : 'Inactif' }}</Badge>
              </td>
              <td class="px-5 py-4">
                <div class="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-fast">
                  <button v-if="canUpdate" class="px-2.5 py-1 text-xs font-semibold text-primary bg-primary-100 rounded-lg hover:bg-primary-200 transition-fast">
                    Modifier
                  </button>
                  <button v-if="canDisable" class="px-2.5 py-1 text-xs font-semibold text-error bg-error-light rounded-lg hover:bg-red-100 transition-fast">
                    {{ w.active ? 'Désactiver' : 'Activer' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <EmptyState v-if="filtered.length === 0" :icon="Warehouse" title="Aucun entrepôt trouvé" description="Modifiez vos critères de recherche." />
      </div>
      <!-- Footer table -->
      <div class="px-5 py-3 border-t border-border-light flex items-center justify-between text-caption">
        <span>{{ filtered.length }} entrepôt{{ filtered.length > 1 ? 's' : '' }}</span>
        <div class="flex items-center gap-1">
          <button class="px-3 py-1 rounded border border-border text-xs hover:bg-background transition-fast">Préc.</button>
          <button class="px-3 py-1 rounded bg-primary text-white text-xs">1</button>
          <button class="px-3 py-1 rounded border border-border text-xs hover:bg-background transition-fast">Suiv.</button>
        </div>
      </div>
    </BaseCard>

    <!-- Modal Create/Edit -->
    <Modal :show="showModal" title="Nouvel entrepôt" size="lg" @close="showModal = false">
      <form @submit.prevent="handleSubmit" class="space-y-5">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <FormField label="Nom de l'entrepôt" required>
            <Input v-model="form.name" placeholder="Ex: Paris Nord" />
          </FormField>

          <FormField label="Ville" required>
            <Select v-model="form.city" :options="cityOptions" placeholder="Sélectionnez une ville" />
          </FormField>
        </div>

        <FormField label="Adresse complète" required>
          <Input v-model="form.address" placeholder="Ex: 123 Avenue de la Logistique" />
        </FormField>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <FormField label="Capacité (m²)" required>
            <Input v-model="form.capacity" type="number" placeholder="Ex: 5000" />
          </FormField>

          <FormField label="Responsable">
            <Input v-model="form.manager" placeholder="Ex: Jean Martin" />
          </FormField>
        </div>

        <FormField label="Description" help-text="Description détaillée de l'entrepôt">
          <Textarea v-model="form.description" placeholder="Décrivez l'entrepôt..." :rows="4" />
        </FormField>
      </form>

      <template #footer>
        <div class="flex items-center justify-end gap-3">
          <Button variant="secondary" @click="showModal = false">Annuler</Button>
          <Button variant="primary" @click="handleSubmit">Créer l'entrepôt</Button>
        </div>
      </template>
    </Modal>
  </div>
</template>

<style scoped>
/* Styles locaux supprimés — icônes centralisées dans design-system.css */
</style>
