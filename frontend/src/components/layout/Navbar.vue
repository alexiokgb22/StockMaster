<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Menu, Search, Bell, ChevronDown, LogOut, User, Settings, X } from '@lucide/vue'

const emit = defineEmits<{ toggleSidebar: []; logout: [] }>()

const route  = useRoute()
const auth   = useAuthStore()

const searchOpen     = ref(false)
const searchQuery    = ref('')
const profileOpen    = ref(false)
const notifOpen      = ref(false)

// Notifications mock — seront branchées sur l'API plus tard
const notifications = ref([
  { id: 1, type: 'warning', title: 'Stock critique', message: 'Produit REF-0234 sous le seuil minimum', time: 'Il y a 5 min', read: false },
  { id: 2, type: 'info',    title: 'Transfert reçu', message: 'TRF-2024-0089 confirmé à Lyon Est',       time: 'Il y a 20 min', read: false },
  { id: 3, type: 'success', title: 'Inventaire clos', message: 'INV-2024-012 validé par G. Martin',      time: 'Il y a 1h',    read: true },
])

const unread = computed(() => notifications.value.filter(n => !n.read).length)

const breadcrumbs = computed(() => {
  const map: Record<string, string> = {
    dashboard:       'Tableau de Bord',
    warehouses:      'Entrepôts',
    zones:           'Zones',
    products:        'Produits',
    categories:      'Catégories',
    suppliers:       'Fournisseurs',
    stocks:          'Stocks',
    transfers:       'Transferts',
    inventories:     'Inventaires',
    'purchase-orders': 'Commandes Fournisseurs',
    reports:         'Rapports',
    audit:           'Audit',
    users:           'Utilisateurs',
  }
  const segments = route.path.split('/').filter(Boolean)
  return segments.map((seg, i) => ({
    label: map[seg] || seg,
    path:  i < segments.length - 1 ? '/' + segments.slice(0, i + 1).join('/') : undefined
  }))
})

const notifIcons: Record<string, string> = {
  warning: 'bg-warning-light text-warning-dark',
  info:    'bg-info-light text-info-dark',
  success: 'bg-success-light text-success-dark',
  error:   'bg-error-light text-error-dark',
}

function markAllRead() {
  notifications.value.forEach(n => n.read = true)
}
</script>

<template>
  <header class="sticky top-0 z-30 h-16 bg-surface border-b border-border-light flex items-center px-4 gap-3 shadow-sm">

    <!-- Burger (mobile) -->
    <button @click="emit('toggleSidebar')" class="lg:hidden p-2 rounded-lg text-text-muted hover:bg-background hover:text-primary transition-fast">
      <Menu class="w-5 h-5" />
    </button>

    <!-- Breadcrumb -->
    <nav class="hidden md:flex items-center gap-1.5 text-sm flex-1 min-w-0">
      <template v-for="(crumb, i) in breadcrumbs" :key="i">
        <router-link v-if="crumb.path" :to="crumb.path" class="text-text-muted hover:text-primary transition-fast truncate">
          {{ crumb.label }}
        </router-link>
        <span v-else class="font-semibold text-text-main truncate">{{ crumb.label }}</span>
        <span v-if="i < breadcrumbs.length - 1" class="text-text-subtle shrink-0">/</span>
      </template>
    </nav>
    <div class="flex-1 md:hidden" />

    <!-- Barre de recherche -->
    <div :class="['transition-base', searchOpen ? 'flex-1 max-w-md' : 'w-auto']">
      <template v-if="searchOpen">
        <div class="flex items-center gap-2 bg-background rounded-lg px-3 py-2 border border-border">
          <Search class="w-4 h-4 text-text-muted shrink-0" />
          <input v-model="searchQuery" autofocus placeholder="Rechercher un produit, entrepôt..." class="flex-1 bg-transparent text-sm outline-none text-text-main placeholder:text-text-muted" />
          <button @click="searchOpen = false; searchQuery = ''" class="text-text-muted hover:text-primary transition-fast">
            <X class="w-4 h-4" />
          </button>
        </div>
      </template>
      <template v-else>
        <button @click="searchOpen = true" class="p-2 rounded-lg text-text-muted hover:bg-background hover:text-primary transition-fast">
          <Search class="w-5 h-5" />
        </button>
      </template>
    </div>

    <!-- Notifications -->
    <div class="relative">
      <button @click="notifOpen = !notifOpen; profileOpen = false"
        class="relative p-2 rounded-lg text-text-muted hover:bg-background hover:text-primary transition-fast">
        <Bell class="w-5 h-5" />
        <span v-if="unread > 0"
          class="absolute top-1.5 right-1.5 w-2 h-2 bg-error rounded-full ring-2 ring-surface" />
      </button>

      <!-- Dropdown notifications -->
      <Transition name="dropdown">
        <div v-if="notifOpen" class="absolute right-0 top-full mt-2 w-80 bg-surface rounded-xl shadow-xl border border-border-light z-50 overflow-hidden">
          <div class="flex items-center justify-between px-4 py-3 border-b border-border-light">
            <h3 class="font-semibold text-text-main text-sm">Notifications</h3>
            <button @click="markAllRead" class="text-xs text-primary hover:underline font-medium">
              Tout lire
            </button>
          </div>
          <ul class="max-h-80 overflow-y-auto">
            <li v-for="notif in notifications" :key="notif.id"
              :class="['flex items-start gap-3 px-4 py-3 border-b border-border-light last:border-0 transition-fast hover:bg-background cursor-pointer', !notif.read ? 'bg-primary-50' : '']">
              <span :class="['w-2 h-2 rounded-full mt-2 shrink-0', !notif.read ? 'bg-primary' : 'bg-transparent']" />
              <div class="flex-1 min-w-0">
                <p class="text-sm font-semibold text-text-main">{{ notif.title }}</p>
                <p class="text-xs text-text-muted truncate">{{ notif.message }}</p>
                <p class="text-xs text-text-subtle mt-0.5">{{ notif.time }}</p>
              </div>
            </li>
          </ul>
          <div class="px-4 py-2.5 border-t border-border-light">
            <button class="text-xs text-primary font-semibold hover:underline w-full text-center">
              Voir toutes les notifications
            </button>
          </div>
        </div>
      </Transition>
    </div>

    <!-- Séparateur -->
    <div class="hidden lg:block w-px h-6 bg-border-light" />

    <!-- Profil -->
    <div class="relative">
      <button @click="profileOpen = !profileOpen; notifOpen = false"
        class="flex items-center gap-2.5 pl-1 pr-2 py-1.5 rounded-lg hover:bg-background transition-fast">
        <div class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 text-sm font-bold text-white" style="background: var(--gradient-dark)">
          {{ auth.user?.username?.[0]?.toUpperCase() || 'U' }}
        </div>
        <div class="hidden lg:block text-left">
          <p class="text-sm font-semibold text-text-main leading-tight">{{ auth.user?.username }}</p>
          <p class="text-xs text-text-muted leading-tight">{{ auth.user?.role }}</p>
        </div>
        <ChevronDown :class="['w-4 h-4 text-text-muted transition-fast hidden lg:block', profileOpen ? 'rotate-180' : '']" />
      </button>

      <!-- Dropdown profil -->
      <Transition name="dropdown">
        <div v-if="profileOpen" class="absolute right-0 top-full mt-2 w-52 bg-surface rounded-xl shadow-xl border border-border-light z-50 overflow-hidden">
          <div class="px-4 py-3 border-b border-border-light">
            <p class="text-sm font-semibold text-text-main">{{ auth.user?.username }}</p>
            <p class="text-xs text-text-muted">{{ auth.user?.email }}</p>
          </div>
          <ul class="py-1">
            <li>
              <button class="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-text-secondary hover:bg-background transition-fast">
                <User class="w-4 h-4 text-text-muted" /> Mon profil
              </button>
            </li>
            <li>
              <button class="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-text-secondary hover:bg-background transition-fast">
                <Settings class="w-4 h-4 text-text-muted" /> Paramètres
              </button>
            </li>
          </ul>
          <div class="py-1 border-t border-border-light">
            <button @click="emit('logout')"
              class="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-error hover:bg-error-light transition-fast font-medium">
              <LogOut class="w-4 h-4" /> Déconnexion
            </button>
          </div>
        </div>
      </Transition>
    </div>
  </header>

  <!-- Backdrop dropdowns -->
  <div v-if="profileOpen || notifOpen" class="fixed inset-0 z-20" @click="profileOpen = false; notifOpen = false" />
</template>

<style scoped>
.dropdown-enter-active, .dropdown-leave-active { transition: all 0.15s ease; }
.dropdown-enter-from, .dropdown-leave-to { opacity: 0; transform: translateY(-6px) scale(0.97); }
</style>
