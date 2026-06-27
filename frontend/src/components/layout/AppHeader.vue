<template>
  <header class="flex items-center justify-between gap-4 border-b border-border bg-white px-6 py-4 shadow-sm sticky top-0 z-10">
    <div>
      <div class="text-sm text-text-secondary">Bienvenue, {{ currentUser?.username }}</div>
      <div class="text-lg font-semibold text-primary">
        Tableau de bord StockMaster
      </div>
    </div>

    <div class="flex items-center gap-4">
      <!-- Badge alertes -->
      <RouterLink
        v-if="can('alert.view')"
        :to="{ name: 'Alerts' }"
        class="relative rounded-full border border-border bg-surface px-4 py-2 text-sm text-text-secondary transition hover:border-primary hover:text-primary"
      >
        🔔 Alertes
        <span
          v-if="unreadCount > 0"
          class="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white"
        >
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </RouterLink>

      <UserMenu />
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePermissions } from '@/composables/usePermissions'
import { alertService } from '@/services/alert.service'
import UserMenu from '@/components/layout/UserMenu.vue'

const authStore = useAuthStore()
const { can } = usePermissions()
const currentUser = computed(() => authStore.currentUser)
const isAdmin = computed(() => authStore.currentUser?.role === 'Administrateur')
const warehouseId = computed(() => authStore.currentUser?.warehouseId ?? undefined)

const unreadCount = ref(0)
let intervalId: ReturnType<typeof setInterval> | null = null

async function fetchUnreadCount() {
  if (!can('alert.view')) return
  try {
    const res = await alertService.countUnread({
      warehouseId: isAdmin.value ? undefined : warehouseId.value,
    })
    unreadCount.value = res.count
  } catch { /* silencieux */ }
}

onMounted(() => {
  fetchUnreadCount()
  // Rafraîchir toutes les 2 minutes
  intervalId = setInterval(fetchUnreadCount, 120_000)
})

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
</script>
