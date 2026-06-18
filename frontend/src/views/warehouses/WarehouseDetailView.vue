<template>
  <div class="min-h-screen bg-gray-50 px-6 py-8">
    <!-- Retour -->
    <div class="mb-6">
      <router-link
        to="/warehouses"
        class="text-sm text-blue-600 hover:underline"
      >
        ← Retour aux entrepôts
      </router-link>
    </div>

    <!-- En-tête -->
    <div v-if="!loading" class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">
            {{ warehouse?.name }}
          </h1>
          <p class="text-gray-600">{{ warehouse?.city }} • {{ warehouse?.address }}</p>
        </div>
        <Badge
          :variant="warehouse?.isActive ? 'success' : 'secondary'"
          :text="warehouse?.isActive ? 'Actif' : 'Inactif'"
        />
      </div>
    </div>

    <!-- Skeleton -->
    <SkeletonLoader v-if="loading" :count="3" />

    <!-- Contenu -->
    <div v-else class="space-y-6">
      <!-- Erreur -->
      <div v-if="error" class="rounded bg-red-50 p-4 text-red-700">
        {{ error }}
      </div>

      <!-- Cartes d'information -->
      <div class="grid gap-6 md:grid-cols-3">
        <!-- Capacité -->
        <BaseCard>
          <div class="space-y-2">
            <h3 class="text-sm font-medium text-gray-600">Capacité de stockage</h3>
            <div class="flex items-baseline gap-2">
              <p class="text-3xl font-bold text-gray-900">
                {{ warehouse?.usedCapacity }}/{{ warehouse?.totalCapacity }}
              </p>
              <p class="text-sm text-gray-600">m³</p>
            </div>
            <div class="space-y-1">
              <div class="flex justify-between text-xs text-gray-600">
                <span>Utilisé</span>
                <span>{{ capacityPercent }}%</span>
              </div>
              <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div
                  class="h-full bg-blue-600 transition-all"
                  :style="{ width: capacityPercent + '%' }"
                />
              </div>
            </div>
          </div>
        </BaseCard>

        <!-- Gestionnaire -->
        <BaseCard>
          <div class="space-y-2">
            <h3 class="text-sm font-medium text-gray-600">Gestionnaire</h3>
            <p class="text-lg font-medium text-gray-900">
              {{ warehouse?.managerName || 'Non assigné' }}
            </p>
          </div>
        </BaseCard>

        <!-- Dates -->
        <BaseCard>
          <div class="space-y-3 text-sm">
            <div>
              <p class="text-gray-600">Créé le</p>
              <p class="font-medium text-gray-900">
                {{ formatDate(warehouse?.createdAt) }}
              </p>
            </div>
            <div>
              <p class="text-gray-600">Modifié le</p>
              <p class="font-medium text-gray-900">
                {{ formatDate(warehouse?.updatedAt) }}
              </p>
            </div>
          </div>
        </BaseCard>
      </div>

      <!-- Formulaire d'édition -->
      <BaseCard v-if="canUpdate">
        <h3 class="mb-4 text-lg font-semibold text-gray-900">
          Modifier les informations
        </h3>
        <form @submit.prevent="handleUpdate" class="space-y-4">
          <div class="grid gap-4 md:grid-cols-2">
            <FormField label="Nom">
              <Input
                v-model="formData.name"
                :error="formErrors.name"
              />
            </FormField>
            <FormField label="Ville">
              <Input
                v-model="formData.city"
                :error="formErrors.city"
              />
            </FormField>
            <FormField label="Adresse" class="md:col-span-2">
              <Input
                v-model="formData.address"
                :error="formErrors.address"
              />
            </FormField>
            <FormField label="Capacité (m³)">
              <Input
                v-model.number="formData.totalCapacity"
                type="number"
                :error="formErrors.totalCapacity"
              />
            </FormField>
          </div>
          <div class="flex justify-end gap-3 pt-4 border-t">
            <Button variant="secondary" @click="resetForm">
              Annuler
            </Button>
            <Button :loading="updating" type="submit">
              Enregistrer
            </Button>
          </div>
        </form>
      </BaseCard>

      <!-- Actions -->
      <div v-if="canDisable" class="flex gap-2">
        <Button
          :variant="warehouse?.isActive ? 'danger' : 'secondary'"
          @click="openToggleConfirmation"
        >
          {{ warehouse?.isActive ? 'Désactiver cet entrepôt' : 'Activer cet entrepôt' }}
        </Button>
      </div>
    </div>

    <!-- Confirmation de désactivation -->
    <ConfirmationDialog
      v-if="showToggleConfirmation"
      title="Confirmation"
      :message="`Êtes-vous sûr de vouloir ${warehouse?.isActive ? 'désactiver' : 'activer'} cet entrepôt ?`"
      confirm-button="Confirmer"
      cancel-button="Annuler"
      @confirm="handleToggle"
      @cancel="showToggleConfirmation = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useWarehouseStore } from '@/stores/warehouse'
import { usePermissions } from '@/composables/usePermissions'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import Badge from '@/components/ui/Badge.vue'
import FormField from '@/components/ui/FormField.vue'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'
import SkeletonLoader from '@/components/ui/SkeletonLoader.vue'
import ConfirmationDialog from '@/components/ui/ConfirmationDialog.vue'
import type { WarehouseResponse, UpdateWarehouseRequest } from '@/types/warehouse.types'

const route = useRoute()
const warehouseStore = useWarehouseStore()
const { hasPermission } = usePermissions()

const loading = ref(true)
const updating = ref(false)
const error = ref<string | null>(null)
const showToggleConfirmation = ref(false)

const warehouse = computed(() => warehouseStore.currentWarehouse)

const canUpdate = computed(() => hasPermission('warehouse.update'))
const canDisable = computed(() => hasPermission('warehouse.disable'))

const formData = reactive<UpdateWarehouseRequest>({
  name: '',
  address: '',
  city: '',
  totalCapacity: undefined
})

const formErrors = reactive<Record<string, string>>({})

const capacityPercent = computed(() => {
  if (!warehouse.value || warehouse.value.totalCapacity === 0) return 0
  return Math.round((warehouse.value.usedCapacity / warehouse.value.totalCapacity) * 100)
})

const formatDate = (date?: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('fr-FR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const resetForm = () => {
  if (warehouse.value) {
    formData.name = warehouse.value.name
    formData.city = warehouse.value.city
    formData.address = warehouse.value.address
    formData.totalCapacity = warehouse.value.totalCapacity
  }
}

const validateForm = () => {
  formErrors.name = !formData.name ? 'Le nom est requis' : ''
  formErrors.city = !formData.city ? 'La ville est requise' : ''
  formErrors.address = !formData.address ? 'L\'adresse est requise' : ''
  formErrors.totalCapacity = !formData.totalCapacity ? 'La capacité est requise' : ''

  return !formErrors.name && !formErrors.city && !formErrors.address && !formErrors.totalCapacity
}

const handleUpdate = async () => {
  error.value = null

  if (!validateForm() || !warehouse.value) {
    return
  }

  updating.value = true

  try {
    await warehouseStore.updateWarehouse(warehouse.value.id, formData)
  } catch (err: unknown) {
    error.value = err instanceof Error ? err.message : 'Erreur lors de la mise à jour'
  } finally {
    updating.value = false
  }
}

const openToggleConfirmation = () => {
  showToggleConfirmation.value = true
}

const handleToggle = async () => {
  if (!warehouse.value) return

  try {
    await warehouseStore.toggleWarehouse(warehouse.value.id)
    showToggleConfirmation.value = false
  } catch (err: unknown) {
    error.value = err instanceof Error ? err.message : 'Erreur lors du changement d\'état'
  }
}

onMounted(async () => {
  const id = parseInt(route.params.id as string)
  loading.value = true

  try {
    await warehouseStore.fetchWarehouse(id)
    resetForm()
  } catch (err: unknown) {
    error.value = err instanceof Error ? err.message : 'Erreur lors du chargement'
  } finally {
    loading.value = false
  }
})
</script>
