<template>
  <BaseModal :title="`Valider — ${reception.receptionNumber}`" @close="$emit('close')">
    <div class="space-y-5">

      <!-- Résumé -->
      <div class="rounded-xl bg-gray-50 px-4 py-3 text-sm space-y-1">
        <p class="text-text-secondary">
          Commande :
          <code class="rounded bg-white border border-border px-2 py-0.5 text-xs font-mono">
            {{ reception.purchaseOrderNumber }}
          </code>
          <span v-if="reception.supplierName" class="ml-1 text-primary font-medium">
            — {{ reception.supplierName }}
          </span>
        </p>
        <p class="text-text-secondary">
          Créé par <span class="font-medium text-text-main">{{ reception.createdByUsername }}</span>
          le {{ new Date(reception.createdAt).toLocaleDateString('fr-FR') }}
        </p>
      </div>

      <!-- Alerte écarts -->
      <div
        v-if="reception.gapCount > 0"
        class="rounded-xl bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-700"
      >
        ⚠ {{ reception.gapCount }} ligne(s) présentent un écart entre quantité attendue et reçue.
        La mise à jour du stock utilisera les quantités <strong>réellement reçues</strong>.
      </div>

      <!-- Tableau récap -->
      <div class="overflow-hidden rounded-xl border border-border">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 text-xs uppercase tracking-wide text-text-secondary">
            <tr>
              <th class="px-4 py-2 text-left">Produit</th>
              <th class="px-4 py-2 text-right">Attendu</th>
              <th class="px-4 py-2 text-right">Reçu</th>
              <th class="px-4 py-2 text-right">Écart</th>
              <th class="px-4 py-2 text-left">Zone</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-border">
            <tr v-for="line in reception.lines" :key="line.id">
              <td class="px-4 py-2 font-medium">{{ line.productName }}</td>
              <td class="px-4 py-2 text-right">{{ line.quantityExpected }}</td>
              <td class="px-4 py-2 text-right font-medium">{{ line.quantityReceived }}</td>
              <td class="px-4 py-2 text-right">
                <span
                  :class="line.gap === 0 ? 'text-green-600' : line.gap < 0 ? 'text-red-600' : 'text-amber-600'"
                  class="font-medium"
                >
                  {{ line.gap > 0 ? '+' : '' }}{{ line.gap }}
                </span>
              </td>
              <td class="px-4 py-2 text-text-secondary">{{ line.zoneName }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <p class="text-sm text-text-secondary">
        En validant, le stock sera mis à jour automatiquement et la commande sera
        <strong>clôturée</strong>.
      </p>

      <!-- Formulaire rejet -->
      <div v-if="showRejectForm" class="space-y-3">
        <FormField label="Motif du rejet (optionnel)">
          <textarea
            v-model="rejectReason"
            rows="2"
            placeholder="Expliquez pourquoi ce bon est rejeté…"
            class="w-full rounded-2xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
          />
        </FormField>
      </div>

      <p v-if="error" class="rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton
          v-if="!showRejectForm"
          type="button"
          variant="danger"
          @click="showRejectForm = true"
        >
          Rejeter
        </BaseButton>
        <BaseButton
          v-if="showRejectForm"
          type="button"
          variant="danger"
          :loading="savingReject"
          @click="handleReject"
        >
          Confirmer le rejet
        </BaseButton>
        <BaseButton
          v-if="!showRejectForm"
          type="button"
          :loading="savingValidate"
          @click="handleValidate"
        >
          Confirmer la validation
        </BaseButton>
      </div>
    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { receptionService } from '@/services/reception.service'
import type { ReceptionResponse } from '@/types/reception.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ reception: ReceptionResponse }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const savingValidate = ref(false)
const savingReject = ref(false)
const showRejectForm = ref(false)
const rejectReason = ref('')
const error = ref('')

async function handleValidate() {
  error.value = ''
  savingValidate.value = true
  try {
    await receptionService.validate(props.reception.warehouseId, props.reception.id)
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    savingValidate.value = false
  }
}

async function handleReject() {
  error.value = ''
  savingReject.value = true
  try {
    await receptionService.reject(props.reception.warehouseId, props.reception.id, {
      reason: rejectReason.value || undefined,
    })
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    savingReject.value = false
  }
}
</script>
