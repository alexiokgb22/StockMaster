<template>
  <BaseModal title="Nouveau transfert" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-5">

      <!-- Entrepôt cible -->
      <FormField label="Entrepôt de destination" required>
        <select
          v-model.number="targetWarehouseId"
          required
          class="w-full rounded-2xl border border-border bg-white px-3 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option :value="0" disabled>-- Choisir un entrepôt --</option>
          <option
            v-for="wh in targetWarehouses"
            :key="wh.id"
            :value="wh.id"
          >
            {{ wh.name }}
          </option>
        </select>
      </FormField>

      <!-- Lignes de transfert -->
      <div>
        <div class="mb-3 flex items-center justify-between">
          <p class="text-sm font-medium text-text-main">Produits à transférer</p>
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
              <button type="button" @click="removeLine(idx)" class="text-xs text-red-500 hover:text-red-700">
                Supprimer
              </button>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <!-- Sélection stock source -->
              <FormField label="Produit / Zone source" required class="col-span-2">
                <select
                  v-model.number="line.stockId"
                  required
                  @change="onStockChange(idx)"
                  class="w-full rounded-2xl border border-border bg-white px-3 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
                >
                  <option :value="0" disabled>-- Choisir --</option>
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
              <FormField label="Quantité à transférer" required>
                <BaseInput
                  v-model.number="line.quantity"
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
          placeholder="Motif du transfert…"
          class="w-full rounded-2xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
        />
      </FormField>

      <p v-if="error" class="rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving" :disabled="lines.length === 0 || targetWarehouseId === 0">
          Soumettre le transfert
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { stockService } from '@/services/stock.service'
import { warehouseService } from '@/services/warehouse.service'
import { transferService } from '@/services/transfer.service'
import type { StockResponse } from '@/types/stock.types'
import type { WarehouseResponse } from '@/types/warehouse.types'
import type { CreateTransferRequest } from '@/types/transfer.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ warehouseId: number }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')
const globalNote = ref('')
const targetWarehouseId = ref(0)
const availableStocks = ref<StockResponse[]>([])
const targetWarehouses = ref<WarehouseResponse[]>([])

interface LineForm {
  stockId: number
  productId: number
  sourceZoneId: number
  quantity: number
  maxAvailable: number
  note: string
}

const lines = ref<LineForm[]>([])

function addLine() {
  lines.value.push({
    stockId: 0,
    productId: 0,
    sourceZoneId: 0,
    quantity: 1,
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
    line.sourceZoneId = stock.zoneId
    line.maxAvailable = stock.quantityAvailable
    line.quantity = Math.min(line.quantity, stock.quantityAvailable) || 1
  }
}

async function handleSubmit() {
  error.value = ''

  if (targetWarehouseId.value === 0) {
    error.value = "Veuillez sélectionner l'entrepôt de destination"
    return
  }
  if (lines.value.some((l) => l.stockId === 0)) {
    error.value = 'Veuillez sélectionner un produit pour chaque ligne'
    return
  }

  saving.value = true
  try {
    const payload: CreateTransferRequest = {
      targetWarehouseId: targetWarehouseId.value,
      note: globalNote.value || undefined,
      lines: lines.value.map((l) => ({
        productId: l.productId,
        sourceZoneId: l.sourceZoneId,
        quantity: l.quantity,
        note: l.note || undefined,
      })),
    }
    await transferService.create(props.warehouseId, payload)
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const [stocksRes, warehousesRes] = await Promise.all([
      stockService.list(props.warehouseId, { size: 200 }),
      warehouseService.list({ active: true, size: 100 }),
    ])
    availableStocks.value = stocksRes.content.filter((s) => s.quantityAvailable > 0)
    // Exclure l'entrepôt courant
    targetWarehouses.value = warehousesRes.content.filter((w) => w.id !== props.warehouseId)
  } catch {
    // silencieux
  }
})
</script>
