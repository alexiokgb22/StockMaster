<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { Tag, Plus, Search, Pencil } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard   from '@/components/ui/BaseCard.vue'
import Badge      from '@/components/ui/Badge.vue'
import Button     from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const auth = useAuthStore()
const canCreate = computed(() => auth.hasPermission(PERMISSIONS.CATEGORY_CREATE))
const canUpdate = computed(() => auth.hasPermission(PERMISSIONS.CATEGORY_UPDATE))

const search = ref('')
const categories = ref([
  { id: 1, name: 'Informatique',   description: 'Matériel et périphériques informatiques',  products: 142, active: true  },
  { id: 2, name: 'Électroménager', description: 'Appareils électroménagers grand et petit',  products: 58,  active: true  },
  { id: 3, name: 'Automobile',     description: 'Pièces et accessoires automobiles',          products: 213, active: true  },
  { id: 4, name: 'Alimentaire',    description: 'Produits alimentaires secs et conserves',    products: 87,  active: true  },
  { id: 5, name: 'Mobilier',       description: 'Meubles et équipements de bureau',           products: 34,  active: false },
])

const filtered = computed(() => categories.value.filter(c =>
  !search.value || c.name.toLowerCase().includes(search.value.toLowerCase())
))
</script>

<template>
  <div class="space-y-5 animate-fade-in">
    <PageHeader
      title="Catégories"
      subtitle="Classification des produits du catalogue"
      :breadcrumbs="[{ label: 'Accueil', to: '/dashboard' }, { label: 'Catégories' }]"
    >
      <template #actions>
        <Button v-if="canCreate" variant="accent" size="sm"><Plus class="w-4 h-4" /> Nouvelle catégorie</Button>
      </template>
    </PageHeader>

    <BaseCard padding="sm">
      <div class="relative max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
        <input v-model="search" placeholder="Rechercher…"
          class="w-full pl-9 pr-4 py-2 text-sm border border-border rounded-lg bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-base" />
      </div>
    </BaseCard>

    <BaseCard padding="none">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-border-light bg-background">
            <th class="text-left px-5 py-3 text-label">Catégorie</th>
            <th class="text-left px-5 py-3 text-label">Description</th>
            <th class="text-left px-5 py-3 text-label">Produits</th>
            <th class="text-left px-5 py-3 text-label">Statut</th>
            <th class="px-5 py-3" />
          </tr>
        </thead>
        <tbody class="divide-y divide-border-light">
          <tr v-for="c in filtered" :key="c.id" class="hover:bg-background transition-fast group">
            <td class="px-5 py-3.5">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-lg bg-accent-light flex items-center justify-center shrink-0">
                  <Tag class="w-4 h-4 text-accent-dark" />
                </div>
                <span class="font-semibold text-text-main">{{ c.name }}</span>
              </div>
            </td>
            <td class="px-5 py-3.5 text-text-muted max-w-xs truncate">{{ c.description }}</td>
            <td class="px-5 py-3.5 font-bold text-text-main">{{ c.products }}</td>
            <td class="px-5 py-3.5"><Badge :variant="c.active ? 'active' : 'inactive'" dot>{{ c.active ? 'Active' : 'Inactive' }}</Badge></td>
            <td class="px-5 py-3.5">
              <button v-if="canUpdate" class="opacity-0 group-hover:opacity-100 transition-fast px-2.5 py-1 text-xs font-semibold text-primary bg-primary-100 rounded-lg hover:bg-primary-200 flex items-center gap-1">
                <Pencil class="w-3 h-3" /> Modifier
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <EmptyState v-if="filtered.length === 0" :icon="Tag" title="Aucune catégorie" />
      <div class="px-5 py-3 border-t border-border-light text-caption">{{ filtered.length }} catégorie{{ filtered.length > 1 ? 's' : '' }}</div>
    </BaseCard>
  </div>
</template>
