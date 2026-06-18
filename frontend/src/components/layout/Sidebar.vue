<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import {
  LayoutDashboard, Warehouse, Grid3X3, Package, Tag, Truck,
  BarChart3, ArrowLeftRight, ClipboardList, ShoppingCart,
  FileText, Users, Boxes,
  Layers
} from '@lucide/vue'

defineProps<{ open: boolean }>()
const emit = defineEmits<{ close: [] }>()

const route = useRoute()
const authStore = useAuthStore()

interface NavItem {
  name: string
  path: string
  icon: unknown
  permission?: string | string[]
  badge?: string | number
}
interface NavGroup {
  label: string
  items: NavItem[]
}

const navGroups = computed<NavGroup[]>(() => {
  const groups: NavGroup[] = []

  // ── Tableau de bord ──
  groups.push({
    label: '',
    items: [{ name: 'Tableau de Bord', path: '/dashboard', icon: LayoutDashboard }]
  })

  // ── Référentiel ──
  const refItems: NavItem[] = []
  if (authStore.hasPermission(PERMISSIONS.WAREHOUSE_READ))
    refItems.push({ name: 'Entrepôts',   path: '/warehouses', icon: Warehouse })
  if (authStore.hasPermission(PERMISSIONS.ZONE_READ))
    refItems.push({ name: 'Zones',       path: '/zones',      icon: Grid3X3 })
  if (authStore.hasPermission(PERMISSIONS.PRODUCT_READ))
    refItems.push({ name: 'Produits',    path: '/products',   icon: Package })
  if (authStore.hasPermission(PERMISSIONS.CATEGORY_READ))
    refItems.push({ name: 'Catégories', path: '/categories', icon: Tag })
  if (authStore.hasPermission(PERMISSIONS.SUPPLIER_READ))
    refItems.push({ name: 'Fournisseurs', path: '/suppliers', icon: Truck })
  if (refItems.length) groups.push({ label: 'Référentiel', items: refItems })

  // ── Opérations ──
  const opsItems: NavItem[] = []
  if (authStore.hasPermission(PERMISSIONS.STOCK_READ))
    opsItems.push({ name: 'Stocks',      path: '/stocks',          icon: Boxes })
  if (authStore.hasAnyPermission([PERMISSIONS.TRANSFER_CREATE, PERMISSIONS.TRANSFER_VALIDATE]))
    opsItems.push({ name: 'Transferts',  path: '/transfers',       icon: ArrowLeftRight })
  if (authStore.hasAnyPermission([PERMISSIONS.INVENTORY_CREATE, PERMISSIONS.INVENTORY_START]))
    opsItems.push({ name: 'Inventaires', path: '/inventories',     icon: ClipboardList })
  if (authStore.hasPermission(PERMISSIONS.RECEIPT_CREATE))
    opsItems.push({ name: 'Commandes',   path: '/purchase-orders', icon: ShoppingCart })
  if (opsItems.length) groups.push({ label: 'Opérations', items: opsItems })

  // ── Analyse ──
  const anaItems: NavItem[] = []
  if (authStore.hasPermission(PERMISSIONS.REPORT_VIEW))
    anaItems.push({ name: 'Rapports',   path: '/reports', icon: FileText })
  if (authStore.hasPermission(PERMISSIONS.AUDIT_VIEW))
    anaItems.push({ name: 'Audit',      path: '/audit',   icon: Layers })
  if (anaItems.length) groups.push({ label: 'Analyse', items: anaItems })

  // ── Administration ──
  const adminItems: NavItem[] = []
  if (authStore.hasAnyPermission([PERMISSIONS.USER_READ, PERMISSIONS.USER_CREATE_STOREKEEPER]))
    adminItems.push({ name: 'Utilisateurs', path: '/users', icon: Users })
  if (adminItems.length) groups.push({ label: 'Administration', items: adminItems })

  return groups
})

function isActive(path: string) {
  return route.path === path || (path !== '/dashboard' && route.path.startsWith(path))
}
</script>

<template>
  <!-- ── Mobile overlay ── -->
  <Transition name="overlay">
    <div v-if="open" class="fixed inset-0 z-40 lg:hidden" @click="emit('close')">
      <div class="absolute inset-0 bg-primary/60 backdrop-blur-sm" />
    </div>
  </Transition>

  <!-- ── Sidebar panel ── -->
  <aside
    :class="[
      'fixed inset-y-0 left-0 z-50 flex flex-col',
      'w-[256px] transition-transform duration-250 ease-in-out',
      open ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
    ]"
    style="background: var(--gradient-sidebar)"
  >
    <!-- Logo -->
    <div class="flex items-center gap-3 px-5 h-[60px] border-b border-white/10 shrink-0">
      <div class="w-8 h-8 rounded-lg flex items-center justify-center shrink-0" style="background: var(--gradient-primary)">
        <BarChart3 class="w-4.5 h-4.5 text-primary" />
      </div>
      <div>
        <span class="text-white font-bold text-[15px] tracking-tight">StockMaster</span>
        <span class="block text-[11px] font-medium" style="color: rgba(255,255,255,0.40)">WMS Platform</span>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 overflow-y-auto py-5 px-3 space-y-6">
      <template v-for="group in navGroups" :key="group.label">
        <div>
          <p v-if="group.label" class="px-3 mb-2 text-[10px] font-bold uppercase tracking-widest" style="color: rgba(255,255,255,0.30)">
            {{ group.label }}
          </p>
          <ul class="space-y-0.5">
            <li v-for="item in group.items" :key="item.path">
              <router-link
                :to="item.path"
                @click="emit('close')"
                :class="[
                  'relative flex items-center gap-3 px-3 py-2 rounded-lg text-[13px] font-medium transition-fast group overflow-hidden',
                  isActive(item.path)
                    ? 'text-white'
                    : 'hover:bg-white/8 text-white/65 hover:text-white'
                ]"
                :style="isActive(item.path) ? 'background: rgba(248,216,48,0.13)' : ''"
              >
                <!-- Active indicator -->
                <span
                  v-if="isActive(item.path)"
                  class="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-5 rounded-r-full"
                  style="background: #F8D830"
                />

                <component
                  :is="item.icon"
                  class="w-[18px] h-[18px] shrink-0 transition-fast"
                  :style="isActive(item.path) ? 'color: #F8D830' : ''"
                />
                <span :class="isActive(item.path) ? 'font-semibold text-white' : ''">
                  {{ item.name }}
                </span>

                <!-- Badge -->
                <span v-if="item.badge" class="ml-auto text-[10px] font-bold bg-error text-white px-1.5 py-0.5 rounded-full">
                  {{ item.badge }}
                </span>
              </router-link>
            </li>
          </ul>
        </div>
      </template>
    </nav>

    <!-- Footer utilisateur -->
    <div class="px-3 pb-4 pt-3 border-t border-white/10 shrink-0">
      <div class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-white/8 transition-fast cursor-pointer">
        <div class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 text-[13px] font-bold" style="background: rgba(248,216,48,0.2); border: 1px solid rgba(248,216,48,0.3); color: #F8D830">
          {{ authStore.user?.username?.[0]?.toUpperCase() || 'U' }}
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-white text-[13px] font-semibold truncate leading-tight">{{ authStore.user?.username }}</p>
          <p class="text-[11px] truncate leading-tight" style="color: rgba(255,255,255,0.40)">{{ authStore.user?.role }}</p>
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.sidebar-mobile-enter-active, .sidebar-mobile-leave-active { transition: opacity 0.2s ease; }
.sidebar-mobile-enter-from, .sidebar-mobile-leave-to { opacity: 0; }
</style>
