<template>
  <BaseCard>
    <!-- En-tête inventaire actif -->
    <div class="mb-5 flex items-start justify-between">
      <div class="space-y-1">
        <div class="flex items-center gap-3">
          <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">
            {{ inventory.inventoryNumber }}
          </code>
          <StatusBadge label="En cours" variant="warning" />
          <span class="text-xs text-text-secondary">
            {{ inventory.inventoryType === 'FULL' ? 'Inventaire complet' : 'Inventaire partiel' }}
          </span>
        </div>
        <p class="text-xs text-text-secondary">
          Démarré le {{ new Date(inventory.startedAt).toLocaleDateString('fr-FR') }}
          par {{ inventory.createdByUsername }}
        </p>
      </div>

      <!-- Progression -->
      <div class="text-right">
        <div class="text-sm font-semibold text-text-main">
          {{ inventory.countedLines }} / {{ inventory.totalLines }} lignes saisies
        </div>
        <div class="mt-1 h-2 w-40 rounded-full bg-gray-100 overflow-hidden">
          <div
            class="h-full rounded-full bg-primary transition-all"
            :style="{ width: progressPercent + '%' }"
          />
        </div>
      </div>
    </div>

    <!-- Tableau de saisie -->
    <div class="overflow-x-auto rounded-xl border border-border">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 text-xs uppercase tracking-wide text-text-secondary">
          <tr>
            <th class="px-4 py-2 text-left">Produit</th>
            <th class="px-4 py-2 text-left">Zone</th>
            <th class="px-4 py-2 text-right">Théorique</th>
            <th class="px-4 py-2 text-right w-36">Physique</th>
            <th class="px-4 py-2 text-right">Écart</th>
            <th class="px-4 py-2 text-left">Note</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-border">
          <tr
            v-for="line in inventory.lines"
            :key="line.id"
            :class="[
              'transition',
              !line.counted ? 'bg-amber-50/40' : '',
              line.counted && line.gap !== 0 ? 'bg-red-50/30' : '',
            ]"
          >
            <!-- Produit -->
            <td class="px-4 py-2">
              <div class="font-medium text-text-main">{{ line.productName }}</div>
              <code class="text-xs text-text-secondary">{{ line.productReference }}</code>
            </td>

            <!-- Zone -->
            <td class="px-4 py-2 text-text-secondary">{{ line.zoneName }}</td>

            <!-- Théorique -->
            <td class="px-4 py-2 text-right font-medium">{{ line.theoreticalQty }}</td>

            <!-- Saisie physique -->
            <td class="px-4 py-2 text-right">
              <div v-if="editingLine === line.id" class="flex items-center justify-end gap-1">
                <input
                  v-model.number="editValue"
                  type="number"
                  min="0"
                  class="w-20 rounded-lg border border-primary px-2 py-1 text-right text-sm outline-none focus:ring-2 focus:ring-primary/20"
                  @keyup.enter="saveLine(line)"
                  @keyup.escape="cancelEdit"
                  ref="editInput"
                />
                <button
                  @click="saveLine(line)"
                  :disabled="savingLine === line.id"
                  class="rounded-lg bg-primary px-2 py-1 text-xs text-white hover:bg-primary/90 disabled:opacity-50"
                >
                  ✓
                </button>
                <button
                  @click="cancelEdit"
                  class="rounded-lg border border-border px-2 py-1 text-xs text-text-secondary hover:text-primary"
                >
                  ✕
                </button>
              </div>
              <div v-else class="flex items-center justify-end gap-2">
                <span
                  v-if="line.counted"
                  class="font-medium"
                  :class="line.gap === 0 ? 'text-green-600' : 'text-red-600'"
                >
                  {{ line.actualQty }}
                </span>
                <span v-else class="text-text-secondary italic text-xs">Non saisie</span>
                <button
                  v-if="canEdit"
                  @click="startEdit(line)"
                  class="rounded px-1.5 py-0.5 text-xs text-primary hover:bg-primary/10 transition"
                >
                  {{ line.counted ? 'Modifier' : 'Saisir' }}
                </button>
              </div>
            </td>

            <!-- Écart -->
            <td class="px-4 py-2 text-right">
              <span v-if="line.counted">
                <span
                  v-if="line.gap === 0"
                  class="text-green-600 font-medium"
                >✓ 0</span>
                <span
                  v-else
                  class="font-medium"
                  :class="line.gap! > 0 ? 'text-amber-600' : 'text-red-600'"
                >
                  {{ line.gap! > 0 ? '+' : '' }}{{ line.gap }}
                </span>
              </span>
              <span v-else class="text-text-secondary">—</span>
            </td>

            <!-- Note ligne -->
            <td class="px-4 py-2 text-xs text-text-secondary">
              {{ line.note ?? '—' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Résumé des écarts -->
    <div
      v-if="inventory.countedLines > 0"
      class="mt-4 flex flex-wrap gap-4 rounded-xl bg-gray-50 px-4 py-3 text-sm"
    >
      <span class="text-text-secondary">
        Lignes saisies :
        <strong class="text-text-main">{{ inventory.countedLines }}</strong>
      </span>
      <span class="text-text-secondary">
        Écarts :
        <strong :class="inventory.gapLines > 0 ? 'text-amber-600' : 'text-green-600'">
          {{ inventory.gapLines }}
        </strong>
      </span>
      <span v-if="inventory.gapLines > 0" class="text-text-secondary">
        Écart total (abs) :
        <strong class="text-red-600">{{ inventory.totalGap }}</strong>
      </span>
    </div>

    <!-- Actions gestionnaire -->
    <div v-if="isManager" class="mt-5 flex items-center justify-between">
      <BaseButton
        variant="danger"
        size="sm"
        :loading="cancelling"
        @click="handleCancel"
      >
        Annuler l'inventaire
      </BaseButton>

      <div class="flex items-center gap-3">
        <span
          v-if="inventory.countedLines < inventory.totalLines"
          class="text-xs text-amber-600"
        >
          {{ inventory.totalLines - inventory.countedLines }} ligne(s) restante(s)
        </span>
        <BaseButton
          :disabled="inventory.countedLines < inventory.totalLines || completing"
          :loading="completing"
          @click="handleComplete"
        >
          Clôturer l'inventaire
        </BaseButton>
      </div>
    </div>

    <!-- Avertissement clôture -->
    <p
      v-if="isManager && inventory.countedLines === inventory.totalLines && inventory.gapLines > 0"
      class="mt-3 rounded-xl bg-red-50 border border-red-200 px-4 py-2 text-xs text-red-700"
    >
      ⚠ {{ inventory.gapLines }} ligne(s) présentent des écarts. La clôture ajustera
      définitivement le stock — cette action est <strong>irréversible</strong>.
    </p>

    <p v-if="error" class="mt-3 rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">
      {{ error }}
    </p>
  </BaseCard>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { inventoryService } from '@/services/inventory.service'
import type { InventoryResponse, InventoryLineResponse } from '@/types/inventory.types'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'

const props = defineProps<{
  inventory: InventoryResponse
  warehouseId: number
  isManager: boolean
  isStorekeeper: boolean
}>()

const emit = defineEmits<{ refreshed: [inventory: InventoryResponse] }>()

// Qui peut saisir : gestionnaire ET magasinier
const canEdit = computed(() => props.isManager || props.isStorekeeper)

const editingLine = ref<number | null>(null)
const editValue = ref<number>(0)
const editNote = ref('')
const savingLine = ref<number | null>(null)
const completing = ref(false)
const cancelling = ref(false)
const error = ref('')
const editInput = ref<HTMLInputElement | null>(null)

const progressPercent = computed(() => {
  if (props.inventory.totalLines === 0) return 0
  return Math.round((props.inventory.countedLines / props.inventory.totalLines) * 100)
})

function startEdit(line: InventoryLineResponse) {
  editingLine.value = line.id
  editValue.value = line.actualQty ?? line.theoreticalQty
  editNote.value = line.note ?? ''
  nextTick(() => editInput.value?.focus())
}

function cancelEdit() {
  editingLine.value = null
}

async function saveLine(line: InventoryLineResponse) {
  if (editValue.value < 0) return
  savingLine.value = line.id
  error.value = ''
  try {
    const updated = await inventoryService.updateLine(
      props.warehouseId,
      props.inventory.id,
      line.id,
      { actualQty: editValue.value, note: editNote.value || undefined },
    )
    editingLine.value = null
    emit('refreshed', updated)
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Erreur lors de la sauvegarde'
  } finally {
    savingLine.value = null
  }
}

async function handleComplete() {
  if (!confirm(
    `Clôturer cet inventaire ?\n\n` +
    `${props.inventory.gapLines} ligne(s) ont des écarts — le stock sera ajusté définitivement.\n` +
    `Cette action est irréversible.`
  )) return

  completing.value = true
  error.value = ''
  try {
    const updated = await inventoryService.complete(props.warehouseId, props.inventory.id)
    emit('refreshed', updated)
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Erreur lors de la clôture'
  } finally {
    completing.value = false
  }
}

async function handleCancel() {
  if (!confirm('Annuler cet inventaire ? Aucun ajustement ne sera appliqué.')) return

  cancelling.value = true
  error.value = ''
  try {
    const updated = await inventoryService.cancel(props.warehouseId, props.inventory.id)
    emit('refreshed', updated)
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Erreur lors de l\'annulation'
  } finally {
    cancelling.value = false
  }
}
</script>
