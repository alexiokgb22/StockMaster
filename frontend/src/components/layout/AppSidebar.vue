<template>
  <aside class="w-72 min-h-screen border-r border-border bg-white px-4 py-6 hidden lg:flex lg:flex-col">
    <!-- Logo -->
    <div class="mb-6 px-2">
      <div class="text-2xl font-semibold text-primary">StockMaster</div>
      <p class="mt-1 text-sm text-text-secondary">ERP WMS foundation</p>
    </div>

    <!-- Badge entrepôt — visible uniquement pour le gestionnaire -->
    <div
      v-if="currentUser?.warehouseName && currentUser.role === 'Gestionnaire d\'Entrepôt'"
      class="mb-6 mx-2 rounded-lg border border-primary/20 bg-primary/5 px-4 py-3"
    >
      <div class="text-xs font-medium uppercase tracking-wide text-primary/70">Mon entrepôt</div>
      <div class="mt-1 text-sm font-semibold text-primary">{{ currentUser.warehouseName }}</div>
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
const managerLinks = computed(() =>
  filterLinks([
    { title: 'Magasiniers', to: { name: 'Storekeepers' }, permission: 'user.create_storekeeper' },
  ]),
)

// Logistique : liste des entrepôts — admin seulement (warehouse.create)
// Le gestionnaire n'a pas besoin de cette vue, son entrepôt est affiché dans le badge ci-dessus.
const logisticsLinks = computed(() =>
  filterLinks([
    { title: 'Entrepôts', to: { name: 'Warehouses' }, permission: 'warehouse.create' },
  ]),
)

const inventoryLinks = computed(() => [
  { title: 'Produits',     to: { name: 'NotFound' }, comingSoon: true },
  { title: 'Stocks',       to: { name: 'NotFound' }, comingSoon: true },
  { title: 'Inventaires',  to: { name: 'NotFound' }, comingSoon: true },
])

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
