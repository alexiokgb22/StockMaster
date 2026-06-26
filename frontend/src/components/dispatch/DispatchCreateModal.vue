<template>
  <BaseModal title="Nouveau bon de sortie" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-5">

      <!-- Ajout d'une ligne -->
      <div>
        <div class="mb-3 flex items-center justify-between">
          <p class="text-sm font-medium text-text-main">Produits à sortir</p>
          <BaseButton type="button" size="sm" variant="secondary" @click="addLine">
            + Ajouter un produit
          </BaseButton>
        </div>

        <div v-if="lines.length === 0" class="rounded-xl border border-dashed border-border py-6 text-center text-sm text-text-secondary">
          Cliquez sur "Ajouter un produit" pour commencer
        </div>

        <div class="space-y-4">
          <div
            v-for="(line, idx) in lines"
            :key="idx"
            class="rounded-xl border border-border bg-surface p-4"
          >
            <div class="mb-3 flex items-center justify-between">
              <span class="text-sm font-medium text-text-main">Ligne {{ idx + 1 }}</span>
              <button
                type="button"
                @click="removeLine(idx)"
                class="text-xs text-red-500 hover:text-red-700"
              >
                Supprimer
              </button>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <!-- Sélection stock (produit + zone combinés) -->
              <FormField label="Produit / Zone" required class="col-span-2">
                <select
                  v-model.number="line.stockId"
                  required
                  @change="onStockChange(idx)"
                  class="w-full rounded-2xl border border-border bg-white px-3 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
                >
                  <option :value="0" disabled>-- Choisir un produit --</option>
                  <option
                    v-for="s in availableStocks"
                    :key="s.id"
                    :value="s.id"
                    :disabled="isStockAlreadySelected(s.id, idx)"
                  >
                    {{ s.productName }} — {{ s.zoneName }}
                    (dispo : {{ s.quantityAvailable }})
                  </option>
                </select>
              </FormField>

              <!-- Quantité -->
              <FormField label="Quantité à sortir" required>
                <BaseInput
                  v-model.number="line.quantityRequested"
                  type="number"
                  :min="1"
                  :max="line.maxAvailable"
                  required
                />
                <p v-if="line.maxAvailable > 0" class="mt-1 text-xs text-text-secondary">
                  Max disponible : {{ line.maxAvailable }}
                </p>
              </FormField>

              <!-- Note -->
              <FormField label="Note (optionnel)">
                <BaseInput v-model="line.note" placeholder="Observation…" />
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
          placeholder="Motif de la sortie, destinataire…"
          class="w-full rounded-2xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
        />
      </FormField>

      <p v-if="error" class="rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving" :disabled="lines.length === 0">
          Soumettre le bon
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { stockService } from '@/services/stock.service'
import { dispatchService } from '@/services/dispatch.service'
import type { StockResponse } from '@/types/stock.types'
import type { CreateDispatchRequest } from '@/types/dispatch.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ warehouseId: number }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')
const globalNote = ref('')
const availableStocks = ref<StockResponse[]>([])

interface LineForm {
  stockId: number
  productId: number
  zoneId: number
  quantityRequested: number
  maxAvailable: number
  note: string
}

const lines = ref<LineForm[]>([])

function addLine() {
  lines.value.push({
    stockId: 0,
    productId: 0,
    zoneId: 0,
    quantityRequested: 1,
    maxAvailable: 0,
    note: '',
  })
}

function removeLine(idx: number) {
  lines.value.splice(idx, 1)
}

function isStockAlreadySelected(stockId: number, currentIdx: number): boolean {
  return lines.value.some((l, i) => i !== currentIdx && l.stockId === stockId)
}

function onStockChange(idx: number) {
  const line = lines.value[idx]
  const stock = availableStocks.value.find((s) => s.id === line.stockId)
  if (stock) {
    line.productId = stock.productId
    line.zoneId = stock.zoneId
    line.maxAvailable = stock.quantityAvailable
    line.quantityRequested = Math.min(line.quantityRequested, stock.quantityAvailable) || 1
  }
}

async function handleSubmit() {
  error.value = ''

  if (lines.value.some((l) => l.stockId === 0)) {
    error.value = 'Veuillez sélectionner un produit pour chaque ligne'
    return
  }
  if (lines.value.some((l) => l.quantityRequested < 1)) {
    error.value = 'La quantité doit être au moins 1'
    return
  }

  saving.value = true
  try {
    const payload: CreateDispatchRequest = {
      note: globalNote.value || undefined,
      lines: lines.value.map((l) => ({
        productId: l.productId,
        zoneId: l.zoneId,
        quantityRequested: l.quantityRequested,
        note: l.note || undefined,
      })),
    }
    await dispatchService.create(props.warehouseId, payload)
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const res = await stockService.list(props.warehouseId, { size: 200 })
    // Garder uniquement les lignes avec du stock disponible
    availableStocks.value = res.content.filter((s) => s.quantityAvailable > 0)
  } catch {
    // stocks vides
  }
})
</script>
