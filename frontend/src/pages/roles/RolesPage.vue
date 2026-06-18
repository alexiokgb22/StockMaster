<template>
  <div>
    <PageHeader title="Rôles" subtitle="Administration" description="Liste des niveaux d’accès configurés dans le backend." />

    <BaseCard>
      <BaseTable :columns="columns">
        <tr v-for="role in roles" :key="role.id" class="border-t border-border hover:bg-primary-light/30">
          <td class="px-4 py-4">{{ role.name }}</td>
          <td class="px-4 py-4">{{ role.id }}</td>
          <td class="px-4 py-4 text-text-secondary">Détail des permissions indisponible via l’API actuelle</td>
        </tr>
      </BaseTable>
      <div v-if="!roles.length" class="mt-8">
        <EmptyState title="Aucun rôle trouvé" description="Vérifiez l’état du backend ou les permissions d’accès." />
      </div>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { roleService } from '@/services/role.service'
import type { RoleResponse } from '@/types/role.types'

const roles = ref<RoleResponse[]>([])
const columns = [
  { key: 'name', label: 'Rôle' },
  { key: 'id', label: 'ID' },
  { key: 'permissions', label: 'Permissions' },
]

const loadRoles = async () => {
  try {
    roles.value = await roleService.list()
  } catch {
    roles.value = []
  }
}

onMounted(loadRoles)
</script>
