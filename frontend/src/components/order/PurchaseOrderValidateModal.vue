<template>
  <BaseModal :title="`Valider la commande ${order.orderNumber}`" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-5">

      <!-- Récap commande -->
      <div class="rounded-xl bg-gray-50 p-4 space-y-1 text-sm">
        <p class="text-text-secondary">
          Entrepôt : <span class="font-medium text-text-main">{{ order.warehouseName }}</span>
        </p>
        <p class="text-text-secondary">
          Créée par <span class="font-medium text-text-main">{{ order.createdByUsername }}</span>
          le {{ order.orderDate ? new Date(order.orderDate).toLocaleDateString('fr-FR') : '—' }}
        </p>
        <div class="mt-2 flex flex-wrap gap-2">
          <span
            v-for="line in order.lines"
            :key="line.id"
            class="rounded-full bg-white border border-border px-2 py-0.5 text-xs"
          >
            {{ line.productName }} × {{ line.quantity }}
          </span>
        </div>
        <p v-if="order.note" class="italic text-text-secondary mt-1">{{ order.note }}</p>
      </div>

      <!-- Choix du fournisseur -->
      <FormField label="Fournisseur" required>
        <div v-if="loadingSuppliers" class="text-sm text-text-secondary">Chargement des fournisseurs…</div>
        <select
          v-else
          v-model="form.supplierId"
          required
          class="w-full rounded-2xl border border-border bg-white px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option :value="null" disabled>-- Choisir un fournisseur --</option>
          <option v-for="s in suppliers" :key="s.id" :value="s.id">
            {{ s.name }}
            <template v-if="s.contactName"> — {{ s.contactName }}</template>
          </option>
        </select>
      </FormField>

      <!-- Date de livraison ajustable -->
      <FormField label="Date de livraison prévue">
        <BaseInput v-model="form.expectedDate" type="date" />
      </FormField>

      <!-- Note admin -->
      <FormField label="Note complémentaire (optionnel)">
        <textarea
          v-model="form.note"
          rows="2"
          placeholder="Informations complémentaires pour le gestionnaire…"
          class="w-full rounded-2xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
        />
      </FormField>

      <p v-if="error" class="rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving" :disabled="!form.supplierId">
          Valider la commande
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { supplierService } from '@/services/supplier.service'
import { purchaseOrderService } from '@/services/purchaseorder.service'
import type { SupplierResponse } from '@/types/supplier.types'
import type { PurchaseOrderResponse, ValidatePurchaseOrderRequest } from '@/types/purchaseorder.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ order: PurchaseOrderResponse }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')
const loadingSuppliers = ref(false)
const suppliers = ref<SupplierResponse[]>([])

const form = ref<{ supplierId: number | null; expectedDate: string; note: string }>({
  supplierId: null,
  expectedDate: props.order.expectedDate ?? '',
  note: props.order.note ?? '',
})

async function handleSubmit() {
  if (!form.value.supplierId) return

  error.value = ''
  saving.value = true
  try {
    const payload: ValidatePurchaseOrderRequest = {
      supplierId: form.value.supplierId,
      expectedDate: form.value.expectedDate || undefined,
      note: form.value.note || undefined,
    }
    await purchaseOrderService.validate(props.order.warehouseId, props.order.id, payload)
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  loadingSuppliers.value = true
  try {
    // On charge tous les fournisseurs actifs (jusqu'à 200 pour le select)
    const res = await supplierService.listAll({ active: true, size: 200 })
    suppliers.value = res.content
  } finally {
    loadingSuppliers.value = false
  }
})
</script>
