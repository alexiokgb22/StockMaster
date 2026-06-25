<template>
  <BaseModal :title="product ? 'Modifier le produit' : 'Nouveau produit'" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-4">

      <FormField label="Nom" required>
        <BaseInput v-model="form.name" placeholder="Nom du produit" required />
      </FormField>

      <FormField label="Catégorie" required>
        <select
          v-model="form.categoryId"
          required
          :disabled="!!warehouseId && !!product"
          class="w-full rounded-3xl border border-border bg-surface px-4 py-3 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
          @change="onCategoryChange"
        >
          <option value="">Sélectionner une catégorie</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
      </FormField>

      <!-- Entrepôts éligibles (admin, contexte global uniquement) -->
      <FormField v-if="!warehouseId && eligibleWarehouses.length > 0" label="Entrepôts">
        <div class="space-y-1 max-h-40 overflow-y-auto rounded-lg border border-border p-2">
          <label
            v-for="w in eligibleWarehouses"
            :key="w.id"
            class="flex items-center gap-2 cursor-pointer rounded px-2 py-1 hover:bg-primary-light/30 text-sm"
          >
            <input type="checkbox" :value="w.id" v-model="form.warehouseIds" class="accent-primary" />
            {{ w.name }}
          </label>
        </div>
        <p class="mt-1 text-xs text-text-secondary">
          {{ product ? 'Décochez pour désaffecter ce produit d\'un entrepôt.' : 'Choisissez les entrepôts qui disposeront de ce produit.' }}
        </p>
      </FormField>
      <div v-else-if="!warehouseId && loadingWarehouses" class="text-xs text-text-secondary">Chargement des entrepôts…</div>

      <FormField label="Description">
        <textarea
          v-model="form.description"
          class="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          rows="2"
        />
      </FormField>

      <div class="grid grid-cols-2 gap-4">
        <FormField label="Prix d'achat (€)">
          <BaseInput v-model.number="form.purchasePrice" type="number" min="0" step="0.01" />
        </FormField>
        <FormField label="Prix de vente (€)">
          <BaseInput v-model.number="form.salePrice" type="number" min="0" step="0.01" />
        </FormField>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <FormField label="Poids (kg)">
          <BaseInput v-model.number="form.weight" type="number" min="0" step="0.001" />
        </FormField>
        <FormField label="Volume (m³)">
          <BaseInput v-model.number="form.volume" type="number" min="0" step="0.001" />
        </FormField>
      </div>

      <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving">
          {{ product ? 'Enregistrer' : 'Créer' }}
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { productService } from '@/services/product.service'
import { warehouseService } from '@/services/warehouse.service'
import type { ProductResponse, CreateProductRequest, UpdateProductRequest } from '@/types/product.types'
import type { CategoryResponse } from '@/types/category.types'
import type { WarehouseResponse } from '@/types/warehouse.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{
  product: ProductResponse | null
  categories: CategoryResponse[]
  warehouseId?: number
}>()

const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')
const eligibleWarehouses = ref<WarehouseResponse[]>([])
const loadingWarehouses = ref(false)

const form = ref({
  name: '',
  description: '',
  categoryId: '' as number | string,
  purchasePrice: null as number | null,
  salePrice: null as number | null,
  weight: null as number | null,
  volume: null as number | null,
  warehouseIds: [] as number[],
})

// Charge les entrepôts éligibles pour une catégorie donnée
async function loadEligibleWarehouses(categoryId: number, preselectedIds: number[] = []) {
  if (props.warehouseId) return
  loadingWarehouses.value = true
  eligibleWarehouses.value = []
  try {
    const warehouseIds = await productService.getWarehousesByCategory(categoryId)
    if (warehouseIds.length === 0) return
    const res = await warehouseService.list({ size: 200 })
    eligibleWarehouses.value = res.content.filter(w => warehouseIds.includes(w.id))
    form.value.warehouseIds = preselectedIds.filter(id => warehouseIds.includes(id))
  } finally {
    loadingWarehouses.value = false
  }
}

watch(() => props.product, async (p) => {
  eligibleWarehouses.value = []
  if (p) {
    form.value = {
      name: p.name,
      description: p.description ?? '',
      categoryId: p.categoryId,
      purchasePrice: p.purchasePrice,
      salePrice: p.salePrice,
      weight: p.weight,
      volume: p.volume,
      warehouseIds: p.warehouseIds ? [...p.warehouseIds] : [],
    }
    // En mode édition globale admin, charger les entrepôts éligibles pré-cochés
    if (!props.warehouseId) {
      await loadEligibleWarehouses(p.categoryId, p.warehouseIds ? [...p.warehouseIds] : [])
    }
  } else {
    form.value = { name: '', description: '', categoryId: '', purchasePrice: null, salePrice: null, weight: null, volume: null, warehouseIds: [] }
  }
}, { immediate: true })

async function onCategoryChange() {
  eligibleWarehouses.value = []
  form.value.warehouseIds = []
  const catId = Number(form.value.categoryId)
  if (!catId) return
  await loadEligibleWarehouses(catId)
}

async function handleSubmit() {
  error.value = ''
  saving.value = true
  try {
    if (props.product) {
      const payload: UpdateProductRequest = {
        name: form.value.name,
        description: form.value.description || undefined,
        purchasePrice: form.value.purchasePrice ?? undefined,
        salePrice: form.value.salePrice ?? undefined,
        weight: form.value.weight ?? undefined,
        volume: form.value.volume ?? undefined,
      }
      await productService.update(props.product.id, payload)
      // Mise à jour de l'affectation des entrepôts (admin, contexte global)
      if (!props.warehouseId && eligibleWarehouses.value.length > 0) {
        await productService.updateWarehouses(props.product.id, form.value.warehouseIds)
      }
    } else {
      const payload: CreateProductRequest = {
        name: form.value.name,
        categoryId: Number(form.value.categoryId),
        description: form.value.description || undefined,
        purchasePrice: form.value.purchasePrice ?? undefined,
        salePrice: form.value.salePrice ?? undefined,
        weight: form.value.weight ?? undefined,
        volume: form.value.volume ?? undefined,
        warehouseIds: form.value.warehouseIds.length > 0 ? form.value.warehouseIds : undefined,
      }
      if (props.warehouseId) {
        await productService.createInWarehouse(props.warehouseId, payload)
      } else {
        await productService.create(payload)
      }
    }
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    saving.value = false
  }
}
</script>
