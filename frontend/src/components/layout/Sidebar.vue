<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import {
  LayoutDashboard, Warehouse, Grid3X3, Package, Tag, Truck,
  BarChart3, ArrowLeftRight, ClipboardList, ShoppingCart,
  FileText, Users, Shield, ChevronRight, Boxes,
  AlertTriangle, Settings, LogOut, Layers
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
  <Transition name="sidebar-mobile">
    <aside
      v-show="open || true"
      :class="[
        'fixed inset-y-0 left-0 z-50 flex flex-col',
        'w-[260px]',
        'lg:translate-x-0 lg:block',
        open ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
      ]"
      style="background: var(--gradient-sidebar)"
    >
      <!-- Logo -->
      <div class="flex items-center gap-3 px-5 h-16 border-b border-white/10 shrink-0">
        <div class="w-8 h-8 rounded-lg flex items-center justify-center shrink-0" style="background: var(--gradient-primary)">
          <BarChart3 class="w-5 h-5 text-primary" />
        </div>
        <div>
          <span class="text-white font-extrabold text-base tracking-tight">StockMaster</span>
          <span class="block text-xs font-medium" style="color: rgba(255,255,255,0.45)">WMS Platform</span>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 overflow-y-auto py-4 px-3 space-y-5">
        <template v-for="group in navGroups" :key="group.label">
          <div>
            <p v-if="group.label" class="px-3 mb-1.5 text-[10px] font-bold uppercase tracking-widest" style="color: rgba(255,255,255,0.35)">
              {{ group.label }}
            </p>
            <ul class="space-y-0.5">
              <li v-for="item in group.items" :key="item.path">
                <router-link
                  :to="item.path"
                  @click="emit('close')"
                  :class="[
                    'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-fast group',
                    isActive(item.path)
                      ? 'text-accent shadow-inner'
                      : 'hover:bg-white/10'
                  ]"
                  :style="isActive(item.path) ? 'background: var(--sidebar-active)' : ''"
                >
                  <!-- Active bar -->
                  <span v-if="isActive(item.path)" class="absolute left-0 w-1 h-6 rounded-r-full bg-accent" />

                  <component
                    :is="item.icon"
                    :class="['w-4.5 h-4.5 shrink-0 transition-fast', isActive(item.path) ? 'text-accent' : 'text-white/60 group-hover:text-white']"
                  />
                  <span :class="[isActive(item.path) ? 'text-white font-semibold' : 'text-white/75 group-hover:text-white']">
                    {{ item.name }}
                  </span>

                  <!-- Badge -->
                  <span v-if="item.badge" class="ml-auto text-xs font-bold bg-error text-white px-1.5 py-0.5 rounded-full">
                    {{ item.badge }}
                  </span>

                  <ChevronRight v-else-if="isActive(item.path)" class="ml-auto w-3.5 h-3.5 text-accent/70" />
                </router-link>
              </li>
            </ul>
          </div>
        </template>
      </nav>

      <!-- Footer -->
      <div class="px-3 pb-4 pt-3 border-t border-white/10 shrink-0">
        <div class="flex items-center gap-3 px-3 py-2 rounded-lg">
          <div class="w-8 h-8 rounded-full bg-accent/20 border border-accent/30 flex items-center justify-center shrink-0">
            <span class="text-accent text-xs font-bold">{{ authStore.user?.username?.[0]?.toUpperCase() || 'U' }}</span>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-white text-sm font-semibold truncate">{{ authStore.user?.username }}</p>
            <p class="text-xs truncate" style="color: rgba(255,255,255,0.45)">{{ authStore.user?.role }}</p>
          </div>
        </div>
      </div>
    </aside>
  </Transition>
</template>

<style scoped>
.w-4\.5 { width: 1.125rem; }
.h-4\.5 { height: 1.125rem; }

.overlay-enter-active, .overlay-leave-active { transition: opacity 0.2s ease; }
.overlay-enter-from, .overlay-leave-to { opacity: 0; }

.sidebar-mobile-enter-active, .sidebar-mobile-leave-active { transition: transform 0.25s ease; }
.sidebar-mobile-enter-from, .sidebar-mobile-leave-to { transform: translateX(-100%); }

@media (min-width: 1024px) {
  .sidebar-mobile-enter-from, .sidebar-mobile-leave-to { transform: translateX(0); }
}
</style>
