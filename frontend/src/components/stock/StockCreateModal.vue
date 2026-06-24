<template>
  <BaseModal title="Initialiser une ligne de stock" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-4">

      <FormField label="Zone" required>
        <select
          v-model="form.zoneId"
          required
          @change="onZoneChange"
          class="w-full rounded-3xl border border-border bg-surface px-4 py-3 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option value="">Sélectionner une zone</option>
          <option v-for="z in zones" :key="z.id" :value="z.id">
            {{ z.name }}{{ z.categoryName ? ' — ' + z.categoryName : '' }}
          </option>
        </select>
      </FormField>

      <FormField label="Produit" required>
        <select
          v-model="form.productId"
          required
          :disabled="!form.zoneId || loadingProducts"
          class="w-full rounded-3xl border border-border bg-surface px-4 py-3 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 disabled:opacity-50"
        >
          <option value="">Sélectionner un produit</option>
          <option v-for="p in availableProducts" :key="p.id" :value="p.id">
            {{ p.name }} ({{ p.categoryName }})
          </option>
        </select>
        <p v-if="form.zoneId && availableProducts.length === 0 && !loadingProducts" class="text-xs text-gray-500 mt-1">
          Aucun produit disponible pour cette zone
        </p>
      </FormField>

      <FormField label="Quantité initiale" required>
        <BaseInput v-model.number="form.initialQuantity" type="number" min="0" required />
      </FormField>

      <div class="grid grid-cols-2 gap-4">
        <FormField label="Stock minimum">
          <BaseInput v-model.number="form.minStock" type="number" min="0" />
        </FormField>
        <FormField label="Stock maximum">
          <BaseInput v-model.number="form.maxStock" type="number" min="0" />
        </FormField>
      </div>

      <FormField label="Note">
        <BaseInput v-model="form.note" placeholder="Note d'initialisation (optionnel)" />
      </FormField>

      <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving">Créer</BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { stockService } from '@/services/stock.service'
import { productService } from '@/services/product.service'
import type { ZoneResponse } from '@/types/zone.types'
import type { ProductResponse } from '@/types/product.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ warehouseId: number; zones: ZoneResponse[] }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const loadingProducts = ref(false)
const error = ref('')
const availableProducts = ref<ProductResponse[]>([])

const form = ref({
  zoneId: '' as number | '',
  productId: '' as number | '',
  initialQuantity: 0,
  minStock: null as number | null,
  maxStock: null as number | null,
  note: '',
})

async function onZoneChange() {
  form.value.productId = ''
  availableProducts.value = []
  if (!form.value.zoneId) return

  const zone = props.zones.find((z) => z.id === Number(form.value.zoneId))
  loadingProducts.value = true
  try {
    availableProducts.value = await productService.listForSelect(
      props.warehouseId,
      zone?.categoryId ?? undefined,
    )
  } finally {
    loadingProducts.value = false
  }
}

async function handleSubmit() {
  error.value = ''
  saving.value = true
  try {
    await stockService.create(props.warehouseId, {
      productId: Number(form.value.productId),
      zoneId: Number(form.value.zoneId),
      initialQuantity: form.value.initialQuantity,
      minStock: form.value.minStock ?? undefined,
      maxStock: form.value.maxStock ?? undefined,
      note: form.value.note || undefined,
    })
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    saving.value = false
  }
}
</script>
