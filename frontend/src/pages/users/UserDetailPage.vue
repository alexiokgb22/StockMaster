<template>
  <div>
    <PageHeader title="Détail utilisateur" subtitle="Administration" description="Mettez à jour le compte, le rôle et l’entrepôt." />

    <BaseCard>
      <div class="grid gap-6 md:grid-cols-2">
        <div>
          <BaseInput label="Nom d’utilisateur" v-model="form.username" placeholder="sophie" />
          <BaseInput label="Email" v-model="form.email" type="email" placeholder="sophie@example.com" />
          <div class="space-y-3">
            <div v-if="form.warehouseId && !showWarehouseSelect" class="rounded-lg border border-border bg-surface p-4">
              <div class="text-sm text-text-secondary">Entrepôt actuellement assigné</div>
              <div class="mt-2 text-base font-medium text-text-main">{{ currentWarehouseLabel }}</div>
              <BaseButton type="button" variant="ghost" class="mt-3" @click="showWarehouseSelect = true">
                Assigner à un autre entrepôt
              </BaseButton>
            </div>
            <div v-else>
              <BaseSelect label="Entrepôt" v-model="form.warehouseId" :options="warehouseOptions" optional />
              <BaseButton
                v-if="form.warehouseId"
                type="button"
                variant="secondary"
                class="mt-2"
                @click="form.warehouseId = null"
              >
                Désaffecter l'entrepôt
              </BaseButton>
            </div>
          </div>
        </div>
        <div>
          <BaseSelect label="Rôle" v-model="selectedRoleId" :options="roleOptions" optional />
          <div class="mt-6 space-y-3">
            <BaseButton
              v-if="user?.id !== currentUser?.id"
              type="button"
              variant="secondary"
              @click="toggleActivation"
            >
              {{ user?.isActive ? 'Désactiver' : 'Activer' }}
            </BaseButton>
          </div>
        </div>
      </div>

      <div class="mt-6 flex items-center gap-3">
        <BaseButton type="button" @click="submit" :disabled="loading">{{ loading ? 'Sauvegarde...' : 'Sauvegarder' }}</BaseButton>
        <RouterLink to="/users" class="text-sm text-text-secondary hover:text-primary">Retour à la liste</RouterLink>
      </div>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref,computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseSelect from '@/components/ui/BaseSelect.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import { useAuth } from '@/composables/useAuth'
import { userService } from '@/services/user.service'
import { roleService } from '@/services/role.service'
import { warehouseService } from '@/services/warehouse.service'
import type { UserResponse } from '@/types/user.types'
import type { RoleResponse } from '@/types/role.types'
import type { WarehouseResponse } from '@/types/warehouse.types'

const route = useRoute()
const router = useRouter()
const { currentUser } = useAuth()
const loading = ref(false)
const user = ref<UserResponse | null>(null)
const roleOptions = ref<{ label: string; value: number }[]>([])
const warehouseOptions = ref<{ label: string; value: number }[]>([])
const selectedRoleId = ref<number | null>(null)

const form = reactive({
  username: '',
  email: '',
  warehouseId: null as number | null,
})

const userId = Number(route.params.id)

const showWarehouseSelect = ref(false)

const currentWarehouseLabel = computed(() => {
  const assigned = warehouseOptions.value.find((option) => option.value === form.warehouseId)
  return assigned ? assigned.label : null
})

const availableWarehouses = computed(() => {
  return warehouseOptions.value.filter((option) => {
    if (!form.warehouseId) return true
    return option.value === form.warehouseId || option.label.includes('(Non assigné)')
  })
})

const loadData = async () => {
  loading.value = true
  try {
    const [userResponse, roles, warehouses] = await Promise.all([
      userService.get(userId),
      roleService.list(),
      warehouseService.list(),
    ])
    user.value = userResponse
    form.username = userResponse.username
    form.email = userResponse.email
    form.warehouseId = userResponse.warehouseId
    selectedRoleId.value = userResponse.roleId
    roleOptions.value = roles.map((role) => ({ label: role.name, value: role.id }))
    warehouseOptions.value = warehouses.content
      .filter((warehouse) => !warehouse.managerId || warehouse.id === userResponse.warehouseId)
      .map((warehouse) => ({ label: `${warehouse.name} (${warehouse.city})`, value: warehouse.id }))
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!user.value) return
  loading.value = true
  try {
    await userService.update(userId, { username: form.username, email: form.email, warehouseId: form.warehouseId })
    if (selectedRoleId.value && selectedRoleId.value !== user.value.roleId) {
      await userService.assignRole(userId, selectedRoleId.value)
    }
    router.push({ name: 'Users' })
  } finally {
    loading.value = false
  }
}

const toggleActivation = async () => {
  if (!user.value) return
  loading.value = true
  try {
    await userService.toggle(userId)
    await loadData()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
