<template>
  <div>
    <!-- ══════════════════════════════════════════════════════════
         VUE ADMIN — liste plate + arbre hiérarchique
    ═══════════════════════════════════════════════════════════════ -->
    <template v-if="isAdmin">
      <PageHeader
        title="Utilisateurs"
        subtitle="Administration"
        description="Gestion des comptes, activation et rôles."
      />

      <!-- Onglets Flat / Arbre -->
      <div class="mt-4 flex gap-2 border-b border-border">
        <button
          v-for="tab in adminTabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'px-4 py-2 text-sm font-medium border-b-2 transition',
            activeTab === tab.key
              ? 'border-primary text-primary'
              : 'border-transparent text-text-secondary hover:text-text-main',
          ]"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- ── Onglet Liste ── -->
      <div v-if="activeTab === 'list'" class="mt-6">
        <div class="mb-4 flex items-center justify-between gap-4">
          <BaseInput v-model="search" placeholder="Nom ou email…" class="max-w-xs" />
          <RouterLink :to="{ name: 'UserCreate' }">
            <BaseButton>Créer un utilisateur</BaseButton>
          </RouterLink>
        </div>

        <BaseCard>
          <BaseTable :columns="columns">
            <tr
              v-for="user in users"
              :key="user.id"
              class="border-t border-border hover:bg-primary-light/30"
            >
              <td class="px-4 py-4">{{ user.username }}</td>
              <td class="px-4 py-4">{{ user.email }}</td>
              <td class="px-4 py-4">{{ user.roleName }}</td>
              <td class="px-4 py-4">{{ user.warehouseName ?? '-' }}</td>
              <td class="px-4 py-4">
                <StatusBadge
                  :label="user.isActive ? 'Actif' : 'Inactif'"
                  :variant="user.isActive ? 'success' : 'danger'"
                />
              </td>
              <td class="px-4 py-4 space-x-2">
                <RouterLink
                  :to="{ name: 'UserDetail', params: { id: user.id } }"
                  class="text-primary hover:underline text-sm"
                >
                  Voir
                </RouterLink>
                <BaseButton
                  v-if="user.id !== currentUser?.id"
                  type="button"
                  variant="danger"
                  @click="openDeleteConfirmation(user.id, user.username)"
                >
                  Supprimer
                </BaseButton>
              </td>
            </tr>
          </BaseTable>
          <EmptyState
            v-if="!users.length && !loading"
            title="Aucun utilisateur trouvé"
            description="Essayez une autre recherche ou créez un nouvel utilisateur."
          />
        </BaseCard>
      </div>

      <!-- ── Onglet Arbre ── -->
      <div v-else class="mt-6 space-y-4">
        <div v-if="loadingTree" class="py-8 text-center text-text-secondary text-sm">
          Chargement de l'arbre…
        </div>
        <template v-else>
          <div
            v-for="node in tree"
            :key="node.warehouseId"
            class="rounded-xl border border-border bg-white overflow-hidden"
          >
            <!-- En-tête entrepôt -->
            <div class="flex items-center justify-between px-5 py-4 bg-surface border-b border-border">
              <div>
                <span class="font-semibold text-text-main">{{ node.warehouseName }}</span>
                <span class="ml-2 text-sm text-text-secondary">{{ node.warehouseCity }}</span>
              </div>
              <StatusBadge
                :label="node.warehouseActive ? 'Actif' : 'Inactif'"
                :variant="node.warehouseActive ? 'success' : 'secondary'"
              />
            </div>

            <div class="px-5 py-4 space-y-4">
              <!-- Gestionnaire -->
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-sm flex-shrink-0">
                  G
                </div>
                <div v-if="node.manager">
                  <RouterLink
                    :to="{ name: 'UserDetail', params: { id: node.manager.id } }"
                    class="text-sm font-medium text-text-main hover:text-primary"
                  >
                    {{ node.manager.username }}
                  </RouterLink>
                  <span class="ml-2 text-xs text-text-secondary">Gestionnaire</span>
                  <StatusBadge
                    class="ml-2"
                    :label="node.manager.isActive ? 'Actif' : 'Inactif'"
                    :variant="node.manager.isActive ? 'success' : 'danger'"
                  />
                </div>
                <span v-else class="text-sm text-text-secondary italic">Aucun gestionnaire assigné</span>
              </div>

              <!-- Magasiniers -->
              <div v-if="node.storekeepers.length" class="ml-6 border-l-2 border-border pl-4 space-y-2">
                <div
                  v-for="sk in node.storekeepers"
                  :key="sk.id"
                  class="flex items-center gap-3"
                >
                  <div class="w-6 h-6 rounded-full bg-gray-100 flex items-center justify-center text-gray-500 font-bold text-xs flex-shrink-0">
                    M
                  </div>
                  <RouterLink
                    :to="{ name: 'UserDetail', params: { id: sk.id } }"
                    class="text-sm text-text-main hover:text-primary"
                  >
                    {{ sk.username }}
                  </RouterLink>
                  <StatusBadge
                    :label="sk.isActive ? 'Actif' : 'Inactif'"
                    :variant="sk.isActive ? 'success' : 'danger'"
                  />
                </div>
              </div>
              <div v-else class="ml-6 text-sm text-text-secondary italic">
                Aucun magasinier
              </div>
            </div>
          </div>

          <EmptyState
            v-if="!tree.length"
            title="Aucun entrepôt"
            description="Créez d'abord des entrepôts pour voir la hiérarchie."
          />
        </template>
      </div>
    </template>

    <!-- ══════════════════════════════════════════════════════════
         VUE GESTIONNAIRE — ses magasiniers uniquement
    ═══════════════════════════════════════════════════════════════ -->
    <template v-else>
      <PageHeader
        title="Mes magasiniers"
        subtitle="Gestion"
        :description="currentUser?.warehouseName ? `Entrepôt : ${currentUser.warehouseName}` : ''"
      />

      <div class="mb-4 flex items-center justify-between gap-4">
        <BaseInput v-model="search" placeholder="Nom…" class="max-w-xs" />
        <RouterLink :to="{ name: 'StorekeeperCreate' }">
          <BaseButton>Créer un magasinier</BaseButton>
        </RouterLink>
      </div>

      <BaseCard>
        <BaseTable :columns="storekeeperColumns">
          <tr
            v-for="user in storekeepers"
            :key="user.id"
            class="border-t border-border hover:bg-primary-light/30"
          >
            <td class="px-4 py-4">{{ user.username }}</td>
            <td class="px-4 py-4">{{ user.email }}</td>
            <td class="px-4 py-4">
              <StatusBadge
                :label="user.isActive ? 'Actif' : 'Inactif'"
                :variant="user.isActive ? 'success' : 'danger'"
              />
            </td>
            <td class="px-4 py-4">
              <BaseButton
                type="button"
                variant="secondary"
                @click="toggleStorekeeper(user.id, user.isActive)"
                :disabled="loading"
              >
                {{ user.isActive ? 'Désactiver' : 'Activer' }}
              </BaseButton>
            </td>
          </tr>
        </BaseTable>
        <EmptyState
          v-if="!storekeepers.length && !loading"
          title="Aucun magasinier"
          description="Créez votre premier magasinier."
        />
      </BaseCard>
    </template>

    <!-- Confirmation suppression (admin) -->
    <ConfirmationDialog
      v-if="showDeleteConfirmation"
      :title="`Supprimer ${deleteUserName}`"
      message="Êtes-vous sûr de vouloir supprimer cet utilisateur ? Cette action est irréversible."
      confirmButton="Supprimer"
      cancelButton="Annuler"
      @confirm="handleDelete"
      @cancel="cancelDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ConfirmationDialog from '@/components/ui/ConfirmationDialog.vue'
import { userService } from '@/services/user.service'
import { useAuth } from '@/composables/useAuth'
import type { UserResponse, WarehouseTreeNode } from '@/types/user.types'

const { currentUser } = useAuth()

// ── Rôle courant ───────────────────────────────────────────────────
const isAdmin      = computed(() => currentUser.value?.role === 'Administrateur')
const isManager    = computed(() => currentUser.value?.role === "Gestionnaire d'Entrepôt")

// ── État commun ────────────────────────────────────────────────────
const search  = ref('')
const loading = ref(false)

// ── Admin : onglets ────────────────────────────────────────────────
const adminTabs = [
  { key: 'list', label: 'Liste' },
  { key: 'tree', label: 'Arbre hiérarchique' },
]
const activeTab = ref<'list' | 'tree'>('list')

// Admin : liste plate
const users  = ref<UserResponse[]>([])
const columns = [
  { key: 'username',  label: 'Utilisateur' },
  { key: 'email',     label: 'Email' },
  { key: 'role',      label: 'Rôle' },
  { key: 'warehouse', label: 'Entrepôt' },
  { key: 'status',    label: 'Statut' },
  { key: 'actions',   label: 'Actions' },
]

// Admin : arbre
const tree        = ref<WarehouseTreeNode[]>([])
const loadingTree = ref(false)

// Suppression
const deleteUserId           = ref<number | null>(null)
const deleteUserName         = ref('')
const showDeleteConfirmation = ref(false)

// ── Gestionnaire : ses magasiniers ─────────────────────────────────
const storekeepers = ref<UserResponse[]>([])
const storekeeperColumns = [
  { key: 'username', label: 'Nom' },
  { key: 'email',    label: 'Email' },
  { key: 'status',   label: 'Statut' },
  { key: 'actions',  label: 'Actions' },
]

// ── Chargement ─────────────────────────────────────────────────────

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await userService.list({ search: search.value, page: 0, size: 50 })
    users.value = response.content
  } catch {
    /* géré silencieusement */
  } finally {
    loading.value = false
  }
}

const loadTree = async () => {
  loadingTree.value = true
  try {
    tree.value = await userService.tree()
  } finally {
    loadingTree.value = false
  }
}

const loadStorekeepers = async () => {
  const warehouseId = currentUser.value?.warehouseId
  if (!warehouseId) return
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

// ── Actions admin ──────────────────────────────────────────────────

const openDeleteConfirmation = (id: number, username: string) => {
  deleteUserId.value  = id
  deleteUserName.value = username
  showDeleteConfirmation.value = true
}

const handleDelete = async () => {
  if (deleteUserId.value === null) return
  loading.value = true
  try {
    await userService.delete(deleteUserId.value)
    await loadUsers()
    if (activeTab.value === 'tree') await loadTree()
  } finally {
    loading.value = false
    showDeleteConfirmation.value = false
    deleteUserId.value  = null
    deleteUserName.value = ''
  }
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  deleteUserId.value  = null
  deleteUserName.value = ''
}

// ── Actions gestionnaire ───────────────────────────────────────────

const toggleStorekeeper = async (id: number, currentActive: boolean) => {
  loading.value = true
  try {
    await userService.toggleStorekeeper(id)
    // Mettre à jour localement sans recharger
    const idx = storekeepers.value.findIndex((u) => u.id === id)
    if (idx !== -1) storekeepers.value[idx] = { ...storekeepers.value[idx], isActive: !currentActive }
  } finally {
    loading.value = false
  }
}

// ── Init ──────────────────────────────────────────────────────────

onMounted(() => {
  if (isAdmin.value) {
    loadUsers()
    loadTree()
  } else if (isManager.value) {
    loadStorekeepers()
  }
})

watch(search, () => {
  if (isAdmin.value) loadUsers()
  else if (isManager.value) loadStorekeepers()
})
</script>
