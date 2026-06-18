<template>
  <div>
    <PageHeader title="Créer un utilisateur" subtitle="Administration" description="Créez un compte avec rôle et entrepôt éventuel." />
    <BaseCard>
      <form @submit.prevent="submit" class="grid gap-6">
        <BaseInput label="Nom d’utilisateur" v-model="form.username" placeholder="sophie" />
        <BaseInput label="Email" v-model="form.email" type="email" placeholder="sophie@example.com" />
        <BaseInput label="Mot de passe" v-model="form.password" type="password" placeholder="••••••••" />
        <BaseSelect label="Rôle" v-model="form.roleId" :options="roleOptions" optional />
        <BaseSelect label="Entrepôt" v-model="form.warehouseId" :options="warehouseOptions" optional />

        <div class="flex items-center gap-3">
          <BaseButton type="submit" :disabled="loading">{{ loading ? 'Création...' : 'Créer' }}</BaseButton>
          <RouterLink to="/users" class="text-sm text-text-secondary hover:text-primary">Retour à la liste</RouterLink>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseSelect from '@/components/ui/BaseSelect.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import { userService } from '@/services/user.service'
import { roleService } from '@/services/role.service'
import { warehouseService } from '@/services/warehouse.service'
import type { RoleResponse } from '@/types/role.types'
import type { WarehouseResponse } from '@/types/warehouse.types'
import type { CreateUserRequest } from '@/types/user.types'

const router = useRouter()
const loading = ref(false)
const roleOptions = ref<{ label: string; value: number }[]>([])
const warehouseOptions = ref<{ label: string; value: number }[]>([])

const form = reactive<CreateUserRequest>({
  username: '',
  email: '',
  password: '',
  roleId: 0,
  warehouseId: null,
})

const loadReferences = async () => {
  loading.value = true
  try {
    const [roles, warehouses] = await Promise.all([roleService.list(), warehouseService.list()])
    roleOptions.value = roles.map((role) => ({ label: role.name, value: role.id }))
    warehouseOptions.value = warehouses.map((warehouse) => ({ label: `${warehouse.name} (${warehouse.city})`, value: warehouse.id }))
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  loading.value = true
  try {
    await userService.create(form)
    router.push({ name: 'Users' })
  } catch {
  } finally {
    loading.value = false
  }
}

onMounted(loadReferences)
</script>
