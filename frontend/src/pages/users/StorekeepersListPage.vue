<template>
  <div>
    <PageHeader
      title="Mes magasiniers"
      subtitle="Gestion"
      :description="currentUser?.warehouseName ? `Entrepôt : ${currentUser.warehouseName}` : ''"
    />

    <div class="mb-4 flex items-center justify-between gap-4">
      <BaseInput v-model="search" placeholder="Nom ou email…" class="max-w-xs" />
      <RouterLink :to="{ name: 'StorekeeperCreate' }">
        <BaseButton>Créer un magasinier</BaseButton>
      </RouterLink>
    </div>

    <BaseCard>
      <BaseTable :columns="columns">
        <tr
          v-for="user in storekeepers"
          :key="user.id"
          :class="[
            'border-t border-border',
            user.isCurrentlyInWarehouse 
              ? 'hover:bg-primary-light/30' 
              : 'bg-gray-50 opacity-75'
          ]"
        >
          <td class="px-4 py-4">
            <div>
              <span class="font-medium">{{ user.username }}</span>
              <span 
                v-if="!user.isCurrentlyInWarehouse" 
                class="ml-2 inline-flex items-center rounded-full bg-orange-100 px-2 py-0.5 text-xs font-medium text-orange-800"
              >
                Réaffecté
              </span>
            </div>
            <div v-if="user.createdByUsername" class="mt-1 text-xs text-text-secondary">
              Créé par: <span class="font-medium">{{ user.createdByUsername }}</span> 
              <span class="text-text-tertiary">({{ user.createdByRole }})</span>
            </div>
          </td>
          <td class="px-4 py-4">{{ user.email }}</td>
          <td class="px-4 py-4">
            <StatusBadge
              v-if="user.isCurrentlyInWarehouse"
              :label="user.isActive ? 'Actif' : 'Inactif'"
              :variant="user.isActive ? 'success' : 'danger'"
            />
            <StatusBadge
              v-else
              label="Réaffecté"
              variant="warning"
            />
            <div v-if="!user.isCurrentlyInWarehouse && user.warehouseName" class="mt-1 text-xs text-text-secondary">
              Maintenant dans: <span class="font-medium">{{ user.warehouseName }}</span>
            </div>
          </td>
          <td class="px-4 py-4 space-x-2">
            <BaseButton
              v-if="user.isCurrentlyInWarehouse"
              type="button"
              :variant="user.isActive ? 'secondary' : 'primary'"
              @click="toggleStorekeeper(user.id, user.isActive)"
              :disabled="loading"
            >
              {{ user.isActive ? 'Désactiver' : 'Activer' }}
            </BaseButton>
            <span v-else class="text-sm text-text-secondary italic">
              N'est plus dans cet entrepôt
            </span>
          </td>
        </tr>
      </BaseTable>
      <EmptyState
        v-if="!storekeepers.length && !loading"
        title="Aucun magasinier"
        description="Créez votre premier magasinier pour votre entrepôt."
      />
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { userService } from '@/services/user.service'
import { useAuth } from '@/composables/useAuth'
import type { UserResponse } from '@/types/user.types'

const { currentUser } = useAuth()

const search = ref('')
const loading = ref(false)
const storekeepers = ref<UserResponse[]>([])

const columns = [
  { key: 'username', label: 'Nom d\'utilisateur' },
  { key: 'email', label: 'Email' },
  { key: 'status', label: 'Statut' },
  { key: 'actions', label: 'Actions' },
]

// ── Chargement ─────────────────────────────────────────────────────

const loadStorekeepers = async () => {
  const warehouseId = currentUser.value?.warehouseId
  if (!warehouseId) {
    console.error('Aucun entrepôt assigné au gestionnaire')
    return
  }
  loading.value = true
  try {
    const response = await userService.storekeeperList(warehouseId, {
      search: search.value,
      page: 0,
      size: 50,
    })
    storekeepers.value = response.content
  } finally {
    loading.value = false
  }
}

// ── Actions ────────────────────────────────────────────────────────

const toggleStorekeeper = async (id: number, currentActive: boolean) => {
  loading.value = true
  try {
    await userService.toggleStorekeeper(id)
    // Mettre à jour localement sans recharger
    const idx = storekeepers.value.findIndex((u) => u.id === id)
    if (idx !== -1) {
      storekeepers.value[idx] = { 
        ...storekeepers.value[idx], 
        isActive: !currentActive 
      }
    }
  } finally {
    loading.value = false
  }
}

// ── Init ───────────────────────────────────────────────────────────

onMounted(loadStorekeepers)
watch(search, loadStorekeepers)
</script>
