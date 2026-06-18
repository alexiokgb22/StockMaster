<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { PERMISSIONS } from '@/utils/permissions'
import { useToast } from '@/composables/useToast'
import userService from '@/services/user.service'
import roleService from '@/services/role.service'
import warehouseService, { type WarehouseOption } from '@/services/warehouse.service'
import type { UserResponse, CreateUserRequest, UpdateUserRequest, RoleOption } from '@/types/user.types'
import Modal from '@/components/ui/Modal.vue'
import FormField from '@/components/ui/FormField.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Button from '@/components/ui/Button.vue'

interface Props {
  show: boolean
  user?: UserResponse | null  // null = création, UserResponse = édition
}

const props = defineProps<Props>()
const emit = defineEmits<{
  close: []
  saved: [user: UserResponse]
}>()

const auth = useAuthStore()
const toast = useToast()

const isAdmin      = computed(() => auth.hasPermission(PERMISSIONS.USER_CREATE))
const isManager    = computed(() => auth.hasPermission(PERMISSIONS.USER_CREATE_STOREKEEPER))
const isEdit       = computed(() => !!props.user)
const modalTitle   = computed(() => isEdit.value ? 'Modifier l\'utilisateur' : 'Nouvel utilisateur')

// Données du formulaire
const form = ref<CreateUserRequest & UpdateUserRequest>({
  username: '',
  email: '',
  password: '',
  roleId: undefined,
  warehouseId: undefined,
})

const errors = ref<Partial<Record<keyof typeof form.value, string>>>({})
const loading = ref(false)

// Options selects
const roles      = ref<RoleOption[]>([])
const warehouses = ref<WarehouseOption[]>([])

const roleOptions = computed(() =>
  roles.value
    .filter(r => r.name !== 'Administrateur')
    .map(r => ({ value: r.id, label: r.name }))
)
const warehouseOptions = computed(() =>
  warehouses.value.map(w => ({ value: w.id, label: w.city ? `${w.name} — ${w.city}` : w.name }))
)

// Afficher le champ entrepôt seulement si rôle = Magasinier (admin) ou toujours pour gestionnaire
const selectedRoleName = computed(() =>
  roles.value.find(r => r.id === Number(form.value.roleId))?.name ?? ''
)
const showWarehouseField = computed(() =>
  isManager.value || selectedRoleName.value === 'Magasinier'
)
// L'entrepôt est obligatoire uniquement pour le gestionnaire (son entrepôt est imposé)
// Pour l'admin, il peut affecter l'entrepôt plus tard
const warehouseRequired = computed(() => isManager.value && !isAdmin.value)

// Charger les options à l'ouverture
watch(() => props.show, async (val) => {
  if (!val) return
  errors.value = {}

  if (isEdit.value && props.user) {
    form.value = {
      username: props.user.username,
      email: props.user.email,
      password: '',
      roleId: props.user.roleId,
      warehouseId: props.user.warehouseId ?? undefined,
    }
  } else {
    form.value = { username: '', email: '', password: '', roleId: undefined, warehouseId: undefined }
  }

  if (isAdmin.value && roles.value.length === 0) {
    roles.value = await roleService.getAll()
  }
  if ((isAdmin.value || isManager.value) && warehouses.value.length === 0) {
    warehouses.value = await warehouseService.getAll()
  }
})

// Effacer l'entrepôt si on change le rôle et que ce n'est plus Magasinier
watch(() => form.value.roleId, () => {
  if (selectedRoleName.value !== 'Magasinier') {
    form.value.warehouseId = undefined
  }
})

function validate(): boolean {
  errors.value = {}
  if (!form.value.username?.trim())              errors.value.username = 'Le nom d\'utilisateur est obligatoire'
  else if (form.value.username.length < 3)       errors.value.username = 'Minimum 3 caractères'
  if (!form.value.email?.trim())                 errors.value.email = 'L\'email est obligatoire'
  else if (!/\S+@\S+\.\S+/.test(form.value.email)) errors.value.email = 'Email invalide'
  if (!isEdit.value && !form.value.password)     errors.value.password = 'Le mot de passe est obligatoire'
  if (!isEdit.value && form.value.password && form.value.password.length < 6)
                                                  errors.value.password = 'Minimum 6 caractères'
  if (isAdmin.value && !isEdit.value && !form.value.roleId)
                                                  errors.value.roleId = 'Le rôle est obligatoire'
  if (warehouseRequired.value && !form.value.warehouseId)
                                                  errors.value.warehouseId = 'L\'entrepôt est obligatoire'
  return Object.keys(errors.value).length === 0
}

async function submit() {
  if (!validate()) return
  loading.value = true
  try {
    let saved: UserResponse

    if (isEdit.value && props.user) {
      // Édition : username, email, warehouseId
      const updatePayload: UpdateUserRequest = {
        username: form.value.username,
        email: form.value.email,
        warehouseId: form.value.warehouseId,
      }
      saved = await userService.update(props.user.id, updatePayload)
      toast.success('Utilisateur modifié avec succès')
    } else if (isAdmin.value) {
      // Création admin
      saved = await userService.create({
        username: form.value.username!,
        email: form.value.email!,
        password: form.value.password!,
        roleId: Number(form.value.roleId),
        warehouseId: form.value.warehouseId,
      })
      toast.success('Utilisateur créé avec succès')
    } else {
      // Création gestionnaire → magasinier
      saved = await userService.createStorekeeper({
        username: form.value.username!,
        email: form.value.email!,
        password: form.value.password!,
        warehouseId: form.value.warehouseId,
      })
      toast.success('Magasinier créé avec succès')
    }

    emit('saved', saved)
    emit('close')
  } catch (err: any) {
    const msg = err.response?.data?.message || 'Une erreur est survenue'
    toast.error('Erreur', msg)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <Modal :show="show" :title="modalTitle" size="md" @close="emit('close')">
    <form class="space-y-4" @submit.prevent="submit">

      <FormField label="Nom d'utilisateur" required :error="errors.username">
        <Input v-model="form.username" placeholder="ex: j.dupont" :error="!!errors.username" />
      </FormField>

      <FormField label="Email" required :error="errors.email">
        <Input v-model="form.email" type="email" placeholder="ex: j.dupont@example.com" :error="!!errors.email" />
      </FormField>

      <FormField v-if="!isEdit" label="Mot de passe" required :error="errors.password">
        <Input v-model="form.password" type="password" placeholder="Minimum 6 caractères" :error="!!errors.password" />
      </FormField>

      <!-- Sélection du rôle — admin uniquement, création uniquement -->
      <FormField v-if="isAdmin && !isEdit" label="Rôle" required :error="errors.roleId">
        <Select
          v-model="form.roleId"
          :options="roleOptions"
          placeholder="Choisir un rôle"
          :error="!!errors.roleId"
        />
      </FormField>

      <!-- Gestionnaire : info rôle figé -->
      <div v-if="isManager && !isAdmin && !isEdit" class="rounded-lg bg-info-light border border-info/20 px-4 py-3 text-sm text-info-dark">
        Le compte sera créé avec le rôle <strong>Magasinier</strong> et affecté à votre entrepôt.
      </div>

      <!-- Entrepôt -->
      <FormField v-if="showWarehouseField && !isEdit" label="Entrepôt" :required="warehouseRequired" :error="errors.warehouseId">
        <Select
          v-model="form.warehouseId"
          :options="warehouseOptions"
          placeholder="Choisir un entrepôt"
          :error="!!errors.warehouseId"
        />
      </FormField>

      <!-- En édition, l'entrepôt est modifiable pour les admins -->
      <FormField v-if="isEdit && isAdmin" label="Entrepôt" :error="errors.warehouseId"
        help-text="Laisser vide pour les rôles non-magasinier">
        <Select
          v-model="form.warehouseId"
          :options="[{ value: '', label: '— Aucun —' }, ...warehouseOptions]"
          :error="!!errors.warehouseId"
        />
      </FormField>

    </form>

    <template #footer>
      <div class="flex justify-end gap-3">
        <Button variant="secondary" @click="emit('close')">Annuler</Button>
        <Button variant="primary" :loading="loading" @click="submit">
          {{ isEdit ? 'Enregistrer' : 'Créer' }}
        </Button>
      </div>
    </template>
  </Modal>
</template>
