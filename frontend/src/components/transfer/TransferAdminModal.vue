<template>
  <BaseModal :title="`Transfert — ${transfer.transferNumber}`" @close="$emit('close')">
    <div class="space-y-5">

      <!-- Info -->
      <div class="rounded-xl bg-gray-50 px-4 py-3 text-sm space-y-1">
        <p class="text-text-secondary">
          De <span class="font-medium text-text-main">{{ transfer.sourceWarehouseName }}</span>
          vers <span class="font-medium text-text-main">{{ transfer.targetWarehouseName }}</span>
        </p>
        <p class="text-text-secondary">
          Initié par <span class="font-medium text-text-main">{{ transfer.createdByUsername }}</span>
          le {{ new Date(transfer.createdAt).toLocaleDateString('fr-FR') }}
        </p>
        <p v-if="transfer.note" class="text-text-secondary">
          Note : <span class="font-medium text-text-main">{{ transfer.note }}</span>
        </p>
      </div>

      <!-- Lignes -->
      <div class="overflow-hidden rounded-xl border border-border">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 text-xs uppercase tracking-wide text-text-secondary">
            <tr>
              <th class="px-4 py-2 text-left">Produit</th>
              <th class="px-4 py-2 text-left">Zone source</th>
              <th class="px-4 py-2 text-right">Quantité</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-border">
            <tr v-for="line in transfer.lines" :key="line.id">
              <td class="px-4 py-2 font-medium">
                {{ line.productName }}
                <code class="ml-1 rounded bg-gray-100 px-1 text-xs">{{ line.productReference }}</code>
              </td>
              <td class="px-4 py-2 text-text-secondary">{{ line.sourceZoneName }}</td>
              <td class="px-4 py-2 text-right font-medium">{{ line.quantity }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <p v-if="transfer.status === 'PENDING'" class="text-sm text-text-secondary">
        En validant, le stock source sera <strong>mis en transit</strong> et le gestionnaire
        cible sera notifié pour confirmer la réception.
      </p>

      <!-- Formulaire annulation -->
      <div v-if="showCancelForm" class="space-y-3">
        <FormField label="Motif d'annulation (optionnel)">
          <textarea
            v-model="cancelReason"
            rows="2"
            placeholder="Expliquez pourquoi ce transfert est annulé…"
            class="w-full rounded-2xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
          />
        </FormField>
      </div>

      <p v-if="error" class="rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Fermer</BaseButton>

        <template v-if="transfer.status === 'PENDING' || transfer.status === 'VALIDATED'">
          <BaseButton
            v-if="!showCancelForm"
            type="button"
            variant="danger"
            @click="showCancelForm = true"
          >
            Annuler le transfert
          </BaseButton>
          <BaseButton
            v-if="showCancelForm"
            type="button"
            variant="danger"
            :loading="savingCancel"
            @click="handleCancel"
          >
            Confirmer l'annulation
          </BaseButton>
        </template>

        <BaseButton
          v-if="transfer.status === 'PENDING' && !showCancelForm"
          type="button"
          :loading="savingValidate"
          @click="handleValidate"
        >
          Valider le transfert
        </BaseButton>
      </div>
    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { transferService } from '@/services/transfer.service'
import type { TransferResponse } from '@/types/transfer.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ transfer: TransferResponse }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const savingValidate = ref(false)
const savingCancel = ref(false)
const showCancelForm = ref(false)
const cancelReason = ref('')
const error = ref('')

async function handleValidate() {
  error.value = ''
  savingValidate.value = true
  try {
    await transferService.validate(props.transfer.id)
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    savingValidate.value = false
  }
}

async function handleCancel() {
  error.value = ''
  savingCancel.value = true
  try {
    await transferService.cancel(props.transfer.id, {
      reason: cancelReason.value || undefined,
    })
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    savingCancel.value = false
  }
}
</script>
