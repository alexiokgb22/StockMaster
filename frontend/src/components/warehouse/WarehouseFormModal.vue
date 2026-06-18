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
      <!-- Le select ne propose que les gestionnaires sans entrepôt
           + le gestionnaire actuellement assigné (valeur courante).
           Un gestionnaire déjà assigné à un autre entrepôt n'apparaît pas. -->
      <FormField label="Gestionnaire">
        <div v-if="loadingManagers" class="text-sm text-gray-400">Chargement…</div>
        <Select
          v-else
          v-model="form.managerId"
          :options="managerOptions"
          placeholder="Aucun gestionnaire"
        />
      </FormField>

      <!-- Boutons -->
      <div class="flex justify-end gap-3 border-t pt-4">
        <Button variant="secondary" type="button" @click="$emit('close')">
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

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const warehouseStore = useWarehouseStore()

const loading = ref(false)
const loadingManagers = ref(false)
const error = ref<string | null>(null)
const availableManagers = ref<UserResponse[]>([])

const form = reactive<{
  name: string
  address: string
  city: string
  totalCapacity: number | null
  managerId: number | null
}>({
  name: '',
  address: '',
  city: '',
  totalCapacity: null,
  managerId: null,
})

const errors = reactive<Record<string, string>>({})

// Options du select : gestionnaires disponibles uniquement.
// L'option "Aucun" (valeur null) est toujours présente pour désaffecter.
const managerOptions = computed(() => [
  { value: null, label: 'Aucun gestionnaire' },
  ...availableManagers.value.map((m) => ({
    value: m.id,
    label: m.username,
  })),
])

const validateForm = () => {
  errors.name = !form.name ? "Le nom est requis" : ''
  errors.address = !form.address ? "L'adresse est requise" : ''
  errors.city = !form.city ? 'La ville est requise' : ''
  errors.totalCapacity = !form.totalCapacity ? 'La capacité est requise' : ''
  return !errors.name && !errors.address && !errors.city && !errors.totalCapacity
}

const handleSubmit = async () => {
  error.value = null
  if (!validateForm()) return

  loading.value = true
  try {
    if (props.warehouse) {
      // Modification
      // managerId = null → convention pour désaffecter (backend : managerId = 0)
      // managerId > 0    → réassignation
      // Pour indiquer "pas de changement", on enverrait undefined —
      // ici on envoie toujours la valeur courante du select pour être explicite.
      const updateData: UpdateWarehouseRequest = {
        name: form.name,
        address: form.address,
        city: form.city,
        totalCapacity: form.totalCapacity ?? undefined,
        // Sentinel 0 = désaffecter ; valeur normale = réassigner
        managerId: form.managerId === null ? 0 : form.managerId,
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
        managerId: form.managerId ?? undefined,
      }
      await warehouseStore.createWarehouse(createData)
      emit('create')
    }
  } catch (err: unknown) {
    if (err && typeof err === 'object' && 'response' in err) {
      const r = (err as { response?: { data?: { message?: string } } }).response
      error.value = r?.data?.message ?? 'Une erreur est survenue'
    } else {
      error.value = 'Une erreur est survenue'
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // Remplir le formulaire si modification
  if (props.warehouse) {
    form.name = props.warehouse.name
    form.address = props.warehouse.address
    form.city = props.warehouse.city
    form.totalCapacity = props.warehouse.totalCapacity
    form.managerId = props.warehouse.managerId ?? null
  }

  // Charger les gestionnaires disponibles :
  // - sans entrepôt assigné
  // - OU gestionnaire actuel de cet entrepôt (pour afficher la valeur courante)
  loadingManagers.value = true
  try {
    availableManagers.value = await userService.availableManagers(props.warehouse?.id ?? null)
  } catch {
    // Non bloquant — le select reste vide
  } finally {
    loadingManagers.value = false
  }
})
</script>
