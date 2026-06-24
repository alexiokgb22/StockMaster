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
        >
          <option value="">Sélectionner une catégorie</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
      </FormField>

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
import type { ProductResponse, CreateProductRequest, UpdateProductRequest } from '@/types/product.types'
import type { CategoryResponse } from '@/types/category.types'
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

const form = ref({
  name: '',
  description: '',
  categoryId: '' as number | string,
  purchasePrice: null as number | null,
  salePrice: null as number | null,
  weight: null as number | null,
  volume: null as number | null,
})

watch(() => props.product, (p) => {
  if (p) {
    form.value = {
      name: p.name,
      description: p.description ?? '',
      categoryId: p.categoryId,
      purchasePrice: p.purchasePrice,
      salePrice: p.salePrice,
      weight: p.weight,
      volume: p.volume,
    }
  } else {
    form.value = { name: '', description: '', categoryId: '', purchasePrice: null, salePrice: null, weight: null, volume: null }
  }
}, { immediate: true })

async function handleSubmit() {
  error.value = ''
  saving.value = true
  try {
    if (props.product) {
      // Modification
      const payload: UpdateProductRequest = {
        name: form.value.name,
        description: form.value.description || undefined,
        purchasePrice: form.value.purchasePrice ?? undefined,
        salePrice: form.value.salePrice ?? undefined,
        weight: form.value.weight ?? undefined,
        volume: form.value.volume ?? undefined,
      }
      await productService.update(props.product.id, payload)
    } else {
      // Création
      const payload: CreateProductRequest = {
        name: form.value.name,
        categoryId: Number(form.value.categoryId),
        description: form.value.description || undefined,
        purchasePrice: form.value.purchasePrice ?? undefined,
        salePrice: form.value.salePrice ?? undefined,
        weight: form.value.weight ?? undefined,
        volume: form.value.volume ?? undefined,
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
