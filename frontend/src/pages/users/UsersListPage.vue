<template>
  <div>
    <PageHeader title="Utilisateurs" description="Gestion des comptes, activation et rôles." subtitle="Administration" />

    <div class="mb-6">
      <TableToolbar>
        <template #left>
          <BaseInput label="Recherche" v-model="search" placeholder="Nom ou email" />
        </template>
        <template #right>
          <RouterLink v-if="canCreate" :to="{ name: 'UserCreate' }">
            <BaseButton>Créer un utilisateur</BaseButton>
          </RouterLink>
        </template>
      </TableToolbar>
    </div>

    <BaseCard>
      <BaseTable :columns="columns">
        <tr v-for="user in users" :key="user.id" class="border-t border-border hover:bg-primary-light/30">
          <td class="px-4 py-4">{{ user.username }}</td>
          <td class="px-4 py-4">{{ user.email }}</td>
          <td class="px-4 py-4">{{ user.roleName }}</td>
          <td class="px-4 py-4">{{ user.warehouseName ?? '-' }}</td>
          <td class="px-4 py-4">
            <StatusBadge :label="user.isActive ? 'Actif' : 'Inactif'" :variant="user.isActive ? 'success' : 'danger'" />
          </td>
          <td class="px-4 py-4 space-x-2">
            <RouterLink :to="{ name: 'UserDetail', params: { id: user.id } }" class="text-primary hover:underline">Voir</RouterLink>
            <BaseButton
              v-if="canDelete && user.id !== currentUser?.id"
              type="button"
              variant="danger"
              @click="openDeleteConfirmation(user.id, user.username)"
            >
              Supprimer
            </BaseButton>
          </td>
        </tr>
      </BaseTable>
      <div v-if="error" class="mt-4 rounded-3xl border border-danger/10 bg-danger/5 p-4 text-sm text-danger">
        {{ error }}
      </div>
      <div v-if="!users.length" class="mt-8">
        <EmptyState title="Aucun utilisateur trouvé" description="Essayez une autre recherche ou créez un nouvel utilisateur." />
      </div>
    </BaseCard>

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
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import TableToolbar from '@/components/ui/TableToolbar.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ConfirmationDialog from '@/components/ui/ConfirmationDialog.vue'
import { userService } from '@/services/user.service'
import { usePermissions } from '@/composables/usePermissions'
import { useAuth } from '@/composables/useAuth'
import type { UserResponse } from '@/types/user.types'

const search = ref('')
const users = ref<UserResponse[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const columns = [
  { key: 'username', label: 'Utilisateur' },
  { key: 'email', label: 'Email' },
  { key: 'role', label: 'Rôle' },
  { key: 'warehouse', label: 'Entrepôt' },
  { key: 'status', label: 'Statut' },
  { key: 'actions', label: 'Actions' },
]

const { can } = usePermissions()
const { currentUser } = useAuth()
const canCreate = can('user.create')
const canDelete = can('user.delete')
const deleteUserId = ref<number | null>(null)
const deleteUserName = ref('')
const showDeleteConfirmation = ref(false)

const openDeleteConfirmation = (id: number, username: string) => {
  deleteUserId.value = id
  deleteUserName.value = username
  showDeleteConfirmation.value = true
}

const handleDelete = async () => {
  if (deleteUserId.value === null) {
    return
  }

  loading.value = true
  try {
    await userService.delete(deleteUserId.value)
    await loadUsers()
  } catch (err) {
    error.value = 'Impossible de supprimer l’utilisateur.'
  } finally {
    loading.value = false
    showDeleteConfirmation.value = false
    deleteUserId.value = null
    deleteUserName.value = ''
  }
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  deleteUserId.value = null
  deleteUserName.value = ''
}

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await userService.list({ search: search.value, page: 0, size: 50 })
    users.value = response.content
  } catch (err) {
    error.value = 'Impossible de charger les utilisateurs.'
  } finally {
    loading.value = false
  }
}

onMounted(loadUsers)
watch(search, () => loadUsers())
</script>
