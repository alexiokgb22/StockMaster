<template>
  <BaseModal :title="`Réceptionner le transfert — ${transfer.transferNumber}`" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-5">

      <!-- Info transfert -->
      <div class="rounded-xl bg-gray-50 px-4 py-3 text-sm space-y-1">
        <p class="text-text-secondary">
          Depuis :
          <span class="font-medium text-text-main">{{ transfer.sourceWarehouseName }}</span>
        </p>
        <p class="text-text-secondary">
          Initié par
          <span class="font-medium text-text-main">{{ transfer.createdByUsername }}</span>
          le {{ new Date(transfer.createdAt).toLocaleDateString('fr-FR') }}
        </p>
        <p v-if="transfer.note" class="text-text-secondary">
          Note : <span class="font-medium text-text-main">{{ transfer.note }}</span>
        </p>
      </div>

      <!-- Affectation des zones cibles -->
      <div>
        <p class="mb-3 text-sm font-medium text-text-main">
          Affecter chaque produit à une zone de votre entrepôt
        </p>
        <div class="space-y-3">
          <div
            v-for="(line, idx) in lineAssignments"
            :key="line.transferLineId"
            class="rounded-xl border border-border bg-surface p-4"
          >
            <div class="mb-2 flex items-center justify-between">
              <span class="font-medium text-text-main">{{ line.productName }}</span>
              <span class="text-sm text-text-secondary">Qté : {{ line.quantity }}</span>
            </div>
            <div class="text-xs text-text-secondary mb-3">
              Zone source : {{ line.sourceZoneName }}
              <code class="ml-1 rounded bg-gray-100 px-1">{{ line.productReference }}</code>
            </div>
            <FormField label="Zone cible" required>
              <select
                v-model.number="line.targetZoneId"
                required
                class="w-full rounded-2xl border border-border bg-white px-3 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
              >
                <option :value="0" disabled>-- Choisir une zone --</option>
                <option v-for="z in zones" :key="z.id" :value="z.id">
                  {{ z.name }}
                  <template v-if="z.zoneType"> ({{ z.zoneType }})</template>
                </option>
              </select>
            </FormField>
          </div>
        </div>
      </div>

      <p class="text-sm text-text-secondary">
        En confirmant, le stock sera <strong>incrémenté</strong> dans les zones choisies
        et un mouvement <code class="rounded bg-gray-100 px-1 text-xs">ENTRY</code> sera enregistré.
      </p>

      <p v-if="error" class="rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving">Confirmer la réception</BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { zoneService } from '@/services/zone.service'
import { transferService } from '@/services/transfer.service'
import type { TransferResponse, ReceiveTransferRequest } from '@/types/transfer.types'
import type { ZoneResponse } from '@/types/zone.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ transfer: TransferResponse; warehouseId: number }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')
const zones = ref<ZoneResponse[]>([])

interface LineAssignment {
  transferLineId: number
  productName: string
  productReference: string
  quantity: number
  sourceZoneName: string
  targetZoneId: number
}

const lineAssignments = ref<LineAssignment[]>(
  props.transfer.lines.map((l) => ({
    transferLineId: l.id,
    productName: l.productName,
    productReference: l.productReference,
    quantity: l.quantity,
    sourceZoneName: l.sourceZoneName,
    targetZoneId: 0,
  })),
)

async function handleSubmit() {
  error.value = ''

  if (lineAssignments.value.some((l) => l.targetZoneId === 0)) {
    error.value = 'Veuillez sélectionner une zone cible pour chaque produit'
    return
  }

  saving.value = true
  try {
    const payload: ReceiveTransferRequest = {
      lines: lineAssignments.value.map((l) => ({
        transferLineId: l.transferLineId,
        targetZoneId: l.targetZoneId,
      })),
    }
    await transferService.receive(props.warehouseId, props.transfer.id, payload)
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
    // silencieux
  }
})
</script>
