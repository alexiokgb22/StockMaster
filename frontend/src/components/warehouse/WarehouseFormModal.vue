<template>
  <Modal
    :title="warehouse ? 'Modifier l\'entrepôt' : 'Créer un entrepôt'"
    @close="$emit('close')"
  >
    <form @submit.prevent="handleSubmit" class="space-y-4">
      <!-- Erreur globale -->
      <div v-if="error" class="rounded bg-red-50 p-3 text-sm text-red-700">
        {{ error }}
      </div>

      <!-- Nom -->
      <FormField label="Nom de l'entrepôt" required>
        <Input
          v-model="form.name"
          placeholder="Ex: Entrepôt Principal"
          :error="errors.name"
        />
      </FormField>

      <!-- Adresse -->
      <FormField label="Adresse" required>
        <Input
          v-model="form.address"
          placeholder="Ex: 123 rue de la Paix"
          :error="errors.address"
        />
      </FormField>

      <!-- Ville -->
      <FormField label="Ville" required>
        <Input
          v-model="form.city"
          placeholder="Ex: Paris"
          :error="errors.city"
        />
      </FormField>

      <!-- Capacité totale -->
      <FormField label="Capacité totale (m³)" required>
        <Input
          v-model.number="form.totalCapacity"
          type="number"
          placeholder="Ex: 10000"
          :error="errors.totalCapacity"
        />
      </FormField>

      <!-- Gestionnaire -->
      <FormField label="Gestionnaire" v-if="managers.length > 0">
        <Select
          v-model="form.managerId"
          :options="managerOptions"
          placeholder="Sélectionner un gestionnaire"
        />
      </FormField>

      <!-- Boutons -->
      <div class="flex justify-end gap-3 border-t pt-4">
        <Button variant="secondary" @click="$emit('close')">
          Annuler
        </Button>
        <Button :loading="loading" type="submit">
          {{ warehouse ? 'Modifier' : 'Créer' }}
        </Button>
      </div>
    </form>
  </Modal>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { useWarehouseStore } from '@/stores/warehouse'
import { userService } from '@/services/user.service'
import Modal from '@/components/ui/Modal.vue'
import FormField from '@/components/ui/FormField.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Button from '@/components/ui/Button.vue'
import type { WarehouseResponse, CreateWarehouseRequest, UpdateWarehouseRequest } from '@/types/warehouse.types'
import type { UserResponse } from '@/types/user.types'

interface Props {
  warehouse?: WarehouseResponse | null
}

interface Emits {
  (e: 'close'): void
  (e: 'create'): void
  (e: 'update'): void
}

const emit = defineEmits<Emits>()

const warehouseStore = useWarehouseStore()

const loading = ref(false)
const error = ref<string | null>(null)
const managers = ref<UserResponse[]>([])

const form = reactive<{
  name: string
  address: string
  city: string
  totalCapacity: number | null
  managerId?: number | null
}>({
  name: '',
  address: '',
  city: '',
  totalCapacity: null,
  managerId: undefined
})

const errors = reactive<Record<string, string>>({})

const managerOptions = computed(() =>
  managers.value.map((m) => ({
    value: m.id,
    label: m.username
  }))
)

const validateForm = () => {
  errors.name = !form.name ? 'Le nom est requis' : ''
  errors.address = !form.address ? 'L\'adresse est requise' : ''
  errors.city = !form.city ? 'La ville est requise' : ''
  errors.totalCapacity = !form.totalCapacity ? 'La capacité est requise' : ''

  return !errors.name && !errors.address && !errors.city && !errors.totalCapacity
}

const handleSubmit = async () => {
  error.value = null

  if (!validateForm()) {
    return
  }

  loading.value = true

  try {
    if (props.warehouse) {
      // Modification
      const updateData: UpdateWarehouseRequest = {
        name: form.name,
        address: form.address,
        city: form.city,
        totalCapacity: form.totalCapacity || undefined,
        managerId: form.managerId || undefined
      }
      await warehouseStore.updateWarehouse(props.warehouse.id, updateData)
      emit('update')
    } else {
      // Création
      const createData: CreateWarehouseRequest = {
        name: form.name,
        address: form.address,
        city: form.city,
        totalCapacity: form.totalCapacity!,
        managerId: form.managerId || undefined
      }
      await warehouseStore.createWarehouse(createData)
      emit('create')
    }
  } catch (err: unknown) {
    error.value = err instanceof Error ? err.message : 'Une erreur est survenue'
  } finally {
    loading.value = false
  }
}

const props = defineProps<Props>()

onMounted(async () => {
  // Charger les gestionnaires
  try {
    const response = await userService.list({ role: 'Gestionnaire d\'entrepôt', size: 100 })
    managers.value = response.content
  } catch (err) {
    console.error('Erreur lors du chargement des gestionnaires', err)
  }

  // Remplir le formulaire si modification
  if (props.warehouse) {
    form.name = props.warehouse.name
    form.address = props.warehouse.address
    form.city = props.warehouse.city
    form.totalCapacity = props.warehouse.totalCapacity
    form.managerId = props.warehouse.managerId
  }
})
</script>
