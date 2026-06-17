<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'

defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const authStore = useAuthStore()

interface MenuItem {
  name: string
  path: string
  icon: string
  permission?: string
  children?: MenuItem[]
}

const menuItems = computed<MenuItem[]>(() => {
  const items: MenuItem[] = []

  // Dashboard (accessible à tous)
  items.push({
    name: 'Tableau de Bord',
    path: '/dashboard',
    icon: 'dashboard'
  })

  // Entrepôts
  if (authStore.hasPermission(PERMISSIONS.WAREHOUSE_READ)) {
    items.push({
      name: 'Entrepôts',
      path: '/warehouses',
      icon: 'warehouse',
      permission: PERMISSIONS.WAREHOUSE_READ
    })
  }

  // Zones
  if (authStore.hasPermission(PERMISSIONS.ZONE_READ)) {
    items.push({
      name: 'Zones',
      path: '/zones',
      icon: 'zone',
      permission: PERMISSIONS.ZONE_READ
    })
  }

  // Produits
  if (authStore.hasPermission(PERMISSIONS.PRODUCT_READ)) {
    items.push({
      name: 'Produits',
      path: '/products',
      icon: 'product',
      permission: PERMISSIONS.PRODUCT_READ
    })
  }

  // Catégories
  if (authStore.hasPermission(PERMISSIONS.CATEGORY_READ)) {
    items.push({
      name: 'Catégories',
      path: '/categories',
      icon: 'category',
      permission: PERMISSIONS.CATEGORY_READ
    })
  }

  // Fournisseurs
  if (authStore.hasPermission(PERMISSIONS.SUPPLIER_READ)) {
    items.push({
      name: 'Fournisseurs',
      path: '/suppliers',
      icon: 'supplier',
      permission: PERMISSIONS.SUPPLIER_READ
    })
  }

  // Stocks
  if (authStore.hasPermission(PERMISSIONS.STOCK_READ)) {
    items.push({
      name: 'Stocks',
      path: '/stocks',
      icon: 'stock',
      permission: PERMISSIONS.STOCK_READ
    })
  }

  // Transferts
  if (authStore.hasAnyPermission([PERMISSIONS.TRANSFER_CREATE, PERMISSIONS.TRANSFER_VALIDATE])) {
    items.push({
      name: 'Transferts',
      path: '/transfers',
      icon: 'transfer',
      permission: PERMISSIONS.TRANSFER_CREATE
    })
  }

  // Inventaires
  if (authStore.hasAnyPermission([PERMISSIONS.INVENTORY_CREATE, PERMISSIONS.INVENTORY_START])) {
    items.push({
      name: 'Inventaires',
      path: '/inventories',
      icon: 'inventory',
      permission: PERMISSIONS.INVENTORY_CREATE
    })
  }

  // Commandes
  if (authStore.hasPermission(PERMISSIONS.RECEIPT_CREATE)) {
    items.push({
      name: 'Commandes',
      path: '/purchase-orders',
      icon: 'order',
      permission: PERMISSIONS.RECEIPT_CREATE
    })
  }

  // Rapports (gestionnaire uniquement)
  if (authStore.hasPermission(PERMISSIONS.REPORT_VIEW)) {
    items.push({
      name: 'Rapports',
      path: '/reports',
      icon: 'report',
      permission: PERMISSIONS.REPORT_VIEW
    })
  }

  // Utilisateurs (admin et gestionnaire)
  if (authStore.hasAnyPermission([PERMISSIONS.USER_READ, PERMISSIONS.USER_CREATE_STOREKEEPER])) {
    items.push({
      name: 'Utilisateurs',
      path: '/users',
      icon: 'users',
      permission: PERMISSIONS.USER_READ
    })
  }

  // Audit (auditeur et admin)
  if (authStore.hasPermission(PERMISSIONS.AUDIT_VIEW)) {
    items.push({
      name: 'Audit',
      path: '/audit',
      icon: 'audit',
      permission: PERMISSIONS.AUDIT_VIEW
    })
  }

  return items
})
</script>

<template>
  <!-- Mobile sidebar overlay -->
  <div v-if="open" class="relative z-50 lg:hidden">
    <div class="fixed inset-0 bg-gray-900/80" @click="emit('close')"></div>

    <div class="fixed inset-0 flex">
      <div class="relative mr-16 flex w-full max-w-xs flex-1">
        <div class="flex grow flex-col gap-y-5 overflow-y-auto bg-white px-6 pb-4">
          <div class="flex h-16 shrink-0 items-center">
            <h1 class="text-xl font-bold text-blue-600">StockMaster</h1>
          </div>
          <nav class="flex flex-1 flex-col">
            <ul role="list" class="flex flex-1 flex-col gap-y-7">
              <li>
                <ul role="list" class="-mx-2 space-y-1">
                  <li v-for="item in menuItems" :key="item.path">
                    <router-link
                      :to="item.path"
                      class="text-gray-700 hover:text-blue-600 hover:bg-gray-50 group flex gap-x-3 rounded-md p-2 text-sm leading-6 font-semibold"
                      active-class="bg-gray-50 text-blue-600"
                      @click="emit('close')"
                    >
                      {{ item.name }}
                    </router-link>
                  </li>
                </ul>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </div>

  <!-- Desktop sidebar -->
  <div class="hidden lg:fixed lg:inset-y-0 lg:z-50 lg:flex lg:w-64 lg:flex-col">
    <div class="flex grow flex-col gap-y-5 overflow-y-auto border-r border-gray-200 bg-white px-6 pb-4">
      <div class="flex h-16 shrink-0 items-center">
        <h1 class="text-xl font-bold text-blue-600">StockMaster</h1>
      </div>
      <nav class="flex flex-1 flex-col">
        <ul role="list" class="flex flex-1 flex-col gap-y-7">
          <li>
            <ul role="list" class="-mx-2 space-y-1">
              <li v-for="item in menuItems" :key="item.path">
                <router-link
                  :to="item.path"
                  class="text-gray-700 hover:text-blue-600 hover:bg-gray-50 group flex gap-x-3 rounded-md p-2 text-sm leading-6 font-semibold"
                  active-class="bg-gray-50 text-blue-600"
                >
                  {{ item.name }}
                </router-link>
              </li>
            </ul>
          </li>
        </ul>
      </nav>
    </div>
  </div>
</template>
