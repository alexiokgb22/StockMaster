<template>
  <aside class="w-72 min-h-screen bg-primary px-4 py-6 hidden lg:flex lg:flex-col shadow-xl">
    <!-- Logo -->
    <div class="mb-8 px-2">
      <div class="text-2xl font-bold text-white">StockMaster</div>
      <p class="mt-1 text-sm text-white/60">ERP WMS foundation</p>
    </div>

    <nav class="space-y-6 px-2 flex-1">
      <SidebarSection label="Dashboard" :items="[{ title: 'Accueil', to: { name: 'Dashboard' } }]" />

      <!-- Administration — admin uniquement -->
      <SidebarSection
        v-if="adminLinks.length"
        label="Administration"
        :items="adminLinks"
      />

      <!-- Gestion — gestionnaire d'entrepôt uniquement -->
      <SidebarSection
        v-if="managerLinks.length"
        label="Gestion"
        :items="managerLinks"
      />

      <!-- Logistique — entrepôts visible uniquement pour l'admin (warehouse.create) -->
      <SidebarSection
        v-if="logisticsLinks.length"
        label="Logistique"
        :items="logisticsLinks"
      />

      <SidebarSection label="Inventaire"       :items="inventoryLinks" />
      <SidebarSection label="Approvisionnement" :items="supplyLinks" />
      <SidebarSection label="Mouvements"        :items="movementLinks" />
      <SidebarSection label="Rapports"          :items="reportsLinks" />
    </nav>

    <!-- Carte entrepôt — visible pour gestionnaire et magasinier -->
    <div
      v-if="currentUser?.warehouseName && (currentUser.role === 'Gestionnaire d\'entrepôt' || currentUser.role === 'Magasinier')"
      class="mt-6 mx-2 rounded-xl bg-white/10 backdrop-blur-sm border border-white/20 px-4 py-4"
    >
      <div class="flex items-start gap-3">
        <div class="flex-shrink-0 w-10 h-10 rounded-lg bg-accent flex items-center justify-center">
          <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
          </svg>
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-xs font-medium uppercase tracking-wide text-accent mb-1">
            {{ currentUser.role === 'Gestionnaire d\'entrepôt' ? 'Mon entrepôt' : 'Affecté à' }}
          </div>
          <div class="text-sm font-semibold text-white truncate">{{ currentUser.warehouseName }}</div>
          <div v-if="currentUser.warehouseCity" class="text-xs text-white/60 mt-0.5">
            {{ currentUser.warehouseCity }}
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { usePermissions } from '@/composables/usePermissions'
import { useAuthStore } from '@/stores/auth'
import SidebarSection from '@/components/layout/SidebarSection.vue'

interface SidebarItem {
  title: string
  to: { name: string }
  permission?: string
  comingSoon?: boolean
}

const { can } = usePermissions()
const authStore = useAuthStore()
const currentUser = computed(() => authStore.currentUser)

const filterLinks = (items: SidebarItem[]) =>
  items.filter((item) => !item.permission || can(item.permission))

// Administration : utilisateurs et rôles (admin)
const adminLinks = computed(() =>
  filterLinks([
    { title: 'Utilisateurs', to: { name: 'Users' },  permission: 'user.read' },
    { title: 'Rôles',        to: { name: 'Roles' },  permission: 'user.read' },
  ]),
)

// Gestion : magasiniers (gestionnaire d'entrepôt uniquement)
// On vérifie le rôle ET la permission pour plus de sécurité
const managerLinks = computed(() => {
  if (currentUser.value?.role !== "Gestionnaire d'entrepôt") {
    return []
  }
  return filterLinks([
    { title: 'Magasiniers', to: { name: 'Storekeepers' }, permission: 'user.create_storekeeper' },
  ])
})

// Logistique : liste des entrepôts — admin seulement (warehouse.create)
// Le gestionnaire n'a pas besoin de cette vue, son entrepôt est affiché dans le badge ci-dessus.
const logisticsLinks = computed(() =>
  filterLinks([
    { title: 'Entrepôts', to: { name: 'Warehouses' }, permission: 'warehouse.create' },
  ]),
)

const inventoryLinks = computed(() =>
  filterLinks([
    { title: 'Catégories',  to: { name: 'Categories' }, permission: 'category.read' },
    { title: 'Produits',    to: { name: 'NotFound' },   comingSoon: true },
    { title: 'Stocks',      to: { name: 'NotFound' },   comingSoon: true },
    { title: 'Inventaires', to: { name: 'NotFound' },   comingSoon: true },
  ]),
)

const supplyLinks = computed(() => [
  { title: 'Fournisseurs', to: { name: 'NotFound' }, comingSoon: true },
  { title: 'Commandes',    to: { name: 'NotFound' }, comingSoon: true },
])

const movementLinks = computed(() => [
  { title: 'Réceptions', to: { name: 'NotFound' }, comingSoon: true },
  { title: 'Sorties',    to: { name: 'NotFound' }, comingSoon: true },
  { title: 'Transferts', to: { name: 'NotFound' }, comingSoon: true },
])

const reportsLinks = computed(() => [
  { title: 'Rapports', to: { name: 'NotFound' }, comingSoon: true },
])
</script>
