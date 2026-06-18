<template>
  <aside class="w-72 min-h-screen border-r border-border bg-white px-4 py-6 hidden lg:flex lg:flex-col">
    <div class="mb-8 px-2">
      <div class="text-2xl font-semibold text-primary">StockMaster</div>
      <p class="mt-1 text-sm text-text-secondary">ERP WMS foundation</p>
    </div>

    <nav class="space-y-6 px-2">
      <SidebarSection label="Dashboard" :items="[{ title: 'Accueil', to: { name: 'Dashboard' } }]" />
      <SidebarSection label="Administration" :items="adminLinks" />
      <SidebarSection label="Logistique" :items="logisticsLinks" />
      <SidebarSection label="Inventaire" :items="inventoryLinks" />
      <SidebarSection label="Approvisionnement" :items="supplyLinks" />
      <SidebarSection label="Mouvements" :items="movementLinks" />
      <SidebarSection label="Rapports" :items="reportsLinks" />
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { usePermissions } from '@/composables/usePermissions'
import SidebarSection from '@/components/layout/SidebarSection.vue'

interface SidebarItem {
  title: string
  to: { name: string }
  permission?: string
  comingSoon?: boolean
}

const { can } = usePermissions()

const filterLinks = (items: SidebarItem[]) =>
  items.filter((item) => !item.permission || can(item.permission))

const adminLinks = computed(() =>
  filterLinks([
    { title: 'Utilisateurs', to: { name: 'Users' }, permission: 'user.read' },
    { title: 'Rôles', to: { name: 'Roles' }, permission: 'user.read' },
  ]),
)

const logisticsLinks = computed(() =>
  filterLinks([
    { title: 'Entrepôts', to: { name: 'Warehouses' }, permission: 'warehouse.read' },
  ]),
)

const inventoryLinks = computed(() => [
  { title: 'Produits', to: { name: 'NotFound' }, comingSoon: true },
  { title: 'Stocks', to: { name: 'NotFound' }, comingSoon: true },
  { title: 'Inventaires', to: { name: 'NotFound' }, comingSoon: true },
])

const supplyLinks = computed(() => [
  { title: 'Fournisseurs', to: { name: 'NotFound' }, comingSoon: true },
  { title: 'Commandes', to: { name: 'NotFound' }, comingSoon: true },
])

const movementLinks = computed(() => [
  { title: 'Réceptions', to: { name: 'NotFound' }, comingSoon: true },
  { title: 'Sorties', to: { name: 'NotFound' }, comingSoon: true },
  { title: 'Transferts', to: { name: 'NotFound' }, comingSoon: true },
])

const reportsLinks = computed(() => [
  { title: 'Rapports', to: { name: 'NotFound' }, comingSoon: true },
])
</script>
