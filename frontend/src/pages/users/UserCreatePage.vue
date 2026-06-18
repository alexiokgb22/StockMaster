<template>
  <div>
    <PageHeader
      title="Créer un utilisateur"
      subtitle="Administration"
      description="Créez un compte avec rôle et entrepôt éventuel."
    />
    <BaseCard>
      <form @submit.prevent="submit" class="grid gap-6">
        <BaseInput label="Nom d'utilisateur" v-model="form.username" placeholder="sophie" />
        <BaseInput label="Email" v-model="form.email" type="email" placeholder="sophie@example.com" />
        <BaseInput label="Mot de passe" v-model="form.password" type="password" placeholder="••••••••" />

        <BaseSelect label="Rôle" v-model="form.roleId" :options="roleOptions" optional />

        <!-- Le select entrepôt se recharge à chaque changement de rôle
             pour n'afficher que les entrepôts sans gestionnaire déjà assigné. -->
        <BaseSelect
          v-if="showWarehouseSelect"
          label="Entrepôt"
          v-model="form.warehouseId"
          :options="warehouseOptions"
          :disabled="loadingWarehouses"
          optional
        />

        <!-- Erreur backend -->
        <div v-if="error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
          {{ error }}
        </div>

        <div class="flex items-center gap-3">
          <BaseButton type="submit" :disabled="loading">
            {{ loading ? 'Création…' : 'Créer' }}
          </BaseButton>
          <RouterLink to="/users" class="text-sm text-text-secondary hover:text-primary">
            Retour à la liste
          </RouterLink>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch, computed } from 'vue'
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
import type { CreateUserRequest } from '@/types/user.types'

const router = useRouter()
const loading = ref(false)
const loadingWarehouses = ref(false)
const error = ref<string | null>(null)

const roleOptions = ref<{ label: string; value: number }[]>([])
const warehouseOptions = ref<{ label: string; value: number }[]>([])
const roles = ref<RoleResponse[]>([])

const form = reactive<CreateUserRequest>({
  username: '',
  email: '',
  password: '',
  roleId: 0,
  warehouseId: null,
})

// Le select entrepôt n'est affiché que si le rôle sélectionné
// peut être associé à un entrepôt (Gestionnaire ou Magasinier)
const selectedRoleName = computed(() => {
  const role = roles.value.find((r) => r.id === form.roleId)
  return role?.name ?? ''
})

const showWarehouseSelect = computed(() =>
  ['Gestionnaire d\'Entrepôt', 'Magasinier'].includes(selectedRoleName.value),
)

// Recharge les entrepôts disponibles à chaque changement de rôle
// pour garantir une liste fraîche sans entrepôts déjà assignés.
watch(
  () => form.roleId,
  async () => {
    form.warehouseId = null
    if (!showWarehouseSelect.value) {
      warehouseOptions.value = []
      return
    }
    loadingWarehouses.value = true
    try {
      const response = await warehouseService.listUnassigned()
      warehouseOptions.value = response.content.map((w) => ({
        label: `${w.name} (${w.city})`,
        value: w.id,
      }))
    } finally {
      loadingWarehouses.value = false
    }
  },
)

const loadReferences = async () => {
  loading.value = true
  try {
    const rolesData = await roleService.list()
    roles.value = rolesData
    roleOptions.value = rolesData.map((r) => ({ label: r.name, value: r.id }))
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  error.value = null
  loading.value = true
  try {
    await userService.create(form)
    router.push({ name: 'Users' })
  } catch (err: unknown) {
    // Affiche le message d'erreur du backend (ex: "entrepôt déjà assigné")
    if (err && typeof err === 'object' && 'response' in err) {
      const response = (err as { response?: { data?: { message?: string } } }).response
      error.value = response?.data?.message ?? 'Une erreur est survenue'
    } else {
      error.value = 'Une erreur est survenue'
    }
    // Recharger la liste des entrepôts pour que le select soit à jour
    if (showWarehouseSelect.value) {
      const response = await warehouseService.listUnassigned()
      warehouseOptions.value = response.content.map((w) => ({
        label: `${w.name} (${w.city})`,
        value: w.id,
      }))
      form.warehouseId = null
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadReferences)
</script>
