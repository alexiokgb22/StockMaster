<template>
  <BaseModal :title="`Bon de réception — ${order.orderNumber}`" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-5">

      <!-- Récap commande -->
      <div class="rounded-xl bg-gray-50 px-4 py-3 text-sm space-y-1">
        <p class="text-text-secondary">
          Fournisseur : <span class="font-medium text-text-main">{{ order.supplierName ?? '—' }}</span>
        </p>
        <p class="text-text-secondary">
          Livraison prévue :
          <span class="font-medium text-text-main">
            {{ order.expectedDate ? new Date(order.expectedDate).toLocaleDateString('fr-FR') : '—' }}
          </span>
        </p>
      </div>

      <!-- Lignes -->
      <div>
        <p class="mb-3 text-sm font-medium text-text-main">Produits à réceptionner</p>
        <div class="space-y-4">
          <div
            v-for="(line, idx) in lines"
            :key="line.purchaseOrderLineId"
            class="rounded-xl border border-border bg-surface p-4"
          >
            <div class="flex items-center justify-between mb-3">
              <div>
                <span class="font-medium text-text-main">{{ line.productName }}</span>
                <code class="ml-2 rounded bg-gray-100 px-1 text-xs">{{ line.productReference }}</code>
              </div>
              <span class="text-sm text-text-secondary">Commandé : {{ line.quantityExpected }}</span>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <!-- Quantité reçue -->
              <FormField label="Quantité reçue" required>
                <BaseInput
                  v-model.number="line.quantityReceived"
                  type="number"
                  :min="0"
                  :max="line.quantityExpected * 2"
                  required
                  :class="line.quantityReceived !== line.quantityExpected ? 'border-amber-400' : ''"
                />
                <p
                  v-if="line.quantityReceived !== line.quantityExpected"
                  class="mt-1 text-xs text-amber-600"
                >
                  Écart : {{ line.quantityReceived - line.quantityExpected }}
                  ({{ line.quantityReceived < line.quantityExpected ? 'manquant' : 'surplus' }})
                </p>
              </FormField>

              <!-- Zone de rangement -->
              <FormField label="Zone de rangement" required>
                <select
                  v-model.number="line.zoneId"
                  required
                  class="w-full rounded-2xl border border-border bg-white px-3 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
                >
                  <option :value="0" disabled>-- Choisir --</option>
                  <option v-for="z in zones" :key="z.id" :value="z.id">
                    {{ z.name }}
                    <template v-if="z.zoneType"> ({{ z.zoneType }})</template>
                  </option>
                </select>
              </FormField>

              <!-- Note ligne -->
              <FormField label="Note (optionnel)" class="col-span-2">
                <BaseInput v-model="line.note" placeholder="Observation sur ce produit" />
              </FormField>
            </div>
          </div>
        </div>
      </div>

      <!-- Note globale -->
      <FormField label="Note générale (optionnel)">
        <textarea
          v-model="globalNote"
          rows="2"
          placeholder="Remarques générales sur la réception…"
          class="w-full rounded-2xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
        />
      </FormField>

      <p v-if="error" class="rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving">Soumettre le bon</BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { zoneService } from '@/services/zone.service'
import { receptionService } from '@/services/reception.service'
import type { PurchaseOrderResponse } from '@/types/purchaseorder.types'
import type { ZoneResponse } from '@/types/zone.types'
import type { CreateReceptionRequest } from '@/types/reception.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ order: PurchaseOrderResponse; warehouseId: number }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')
const zones = ref<ZoneResponse[]>([])
const globalNote = ref('')

// Une entrée par ligne de commande
const lines = ref(
  props.order.lines.map((l) => ({
    purchaseOrderLineId: l.id,
    productName: l.productName,
    productReference: l.productReference,
    quantityExpected: l.quantity,
    quantityReceived: l.quantity, // pré-rempli avec la qté attendue
    zoneId: 0,
    note: '',
  })),
)

async function handleSubmit() {
  error.value = ''

  // Vérifier que toutes les zones sont sélectionnées
  if (lines.value.some((l) => l.zoneId === 0)) {
    error.value = 'Veuillez sélectionner une zone de rangement pour chaque produit'
    return
  }

  saving.value = true
  try {
    const payload: CreateReceptionRequest = {
      purchaseOrderId: props.order.id,
      note: globalNote.value || undefined,
      lines: lines.value.map((l) => ({
        purchaseOrderLineId: l.purchaseOrderLineId,
        quantityReceived: l.quantityReceived,
        zoneId: l.zoneId,
        note: l.note || undefined,
      })),
    }
    await receptionService.create(props.warehouseId, payload)
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const res = await zoneService.list(props.warehouseId, { size: 100 })
    zones.value = res.content
  } catch {
    // zones vides — l'utilisateur verra les selects vides
  }
})
</script>
