<template>
  <BaseModal :title="`Ajuster : ${stock.productName}`" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-4">

      <div class="bg-gray-50 rounded-lg p-3 text-sm space-y-1">
        <p><span class="text-gray-500">Zone :</span> {{ stock.zoneName }}</p>
        <p><span class="text-gray-500">Catégorie :</span> {{ stock.categoryName }}</p>
        <p><span class="text-gray-500">Quantité actuelle :</span>
          <span :class="stock.isBelowMin ? 'text-red-600 font-semibold' : 'font-medium'">
            {{ stock.quantityAvailable }}
          </span>
        </p>
      </div>

      <FormField label="Nouvelle quantité disponible">
        <BaseInput v-model.number="form.quantityAvailable" type="number" min="0" />
      </FormField>

      <div class="grid grid-cols-2 gap-4">
        <FormField label="Stock minimum">
          <BaseInput v-model.number="form.minStock" type="number" min="0" />
        </FormField>
        <FormField label="Stock maximum">
          <BaseInput v-model.number="form.maxStock" type="number" min="0" />
        </FormField>
      </div>

      <FormField label="Motif de l'ajustement">
        <BaseInput v-model="form.note" placeholder="Ex : correction d'inventaire" />
      </FormField>

      <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving">Enregistrer</BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { stockService } from '@/services/stock.service'
import type { StockResponse } from '@/types/stock.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ warehouseId: number; stock: StockResponse }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')

const form = ref({
  quantityAvailable: props.stock.quantityAvailable,
  minStock: props.stock.minStock,
  maxStock: props.stock.maxStock,
  note: '',
})

async function handleSubmit() {
  error.value = ''
  saving.value = true
  try {
    await stockService.update(props.warehouseId, props.stock.id, {
      quantityAvailable: form.value.quantityAvailable,
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
