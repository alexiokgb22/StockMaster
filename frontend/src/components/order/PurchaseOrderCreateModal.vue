<template>
  <BaseModal title="Nouvelle demande d'approvisionnement" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-5">

      <!-- Infos générales -->
      <div class="grid grid-cols-2 gap-4">
        <FormField label="Date de livraison souhaitée" class="col-span-1">
          <BaseInput v-model="form.expectedDate" type="date" />
        </FormField>
        <FormField label="Note / commentaire" class="col-span-2">
          <textarea
            v-model="form.note"
            rows="2"
            placeholder="Précisions sur la commande (optionnel)"
            class="w-full rounded-2xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
          />
        </FormField>
      </div>

      <!-- Lignes de commande -->
      <div>
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-medium text-text-main">Produits à commander</span>
          <BaseButton type="button" size="sm" variant="secondary" @click="addLine">+ Ajouter un produit</BaseButton>
        </div>

        <div v-if="lines.length === 0" class="rounded-xl border border-dashed border-border p-4 text-center text-sm text-text-secondary">
          Aucun produit ajouté. Cliquez sur "+ Ajouter un produit".
        </div>

        <div v-else class="space-y-3">
          <div
            v-for="(line, idx) in lines"
            :key="idx"
            class="grid grid-cols-12 gap-2 items-end rounded-xl border border-border bg-surface p-3"
          >
            <!-- Sélection produit -->
            <div class="col-span-5">
              <label class="mb-1 block text-xs text-text-secondary">Produit</label>
              <select
                v-model="line.productId"
                required
                class="w-full rounded-2xl border border-border bg-white px-3 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
              >
                <option value="" disabled>-- Choisir --</option>
                <optgroup
                  v-for="group in groupedProducts"
                  :key="group.category"
                  :label="group.category"
                >
                  <option v-for="p in group.products" :key="p.id" :value="p.id">
                    {{ p.name }} <span v-if="p.reference">({{ p.reference }})</span>
                  </option>
                </optgroup>
              </select>
            </div>

            <!-- Quantité -->
            <div class="col-span-3">
              <label class="mb-1 block text-xs text-text-secondary">Quantité</label>
              <BaseInput
                v-model.number="line.quantity"
                type="number"
                :min="1"
                placeholder="Qté"
                required
              />
            </div>

            <!-- Prix unitaire -->
            <div class="col-span-3">
              <label class="mb-1 block text-xs text-text-secondary">Prix unit. (opt.)</label>
              <BaseInput
                v-model.number="line.unitPrice"
                type="number"
                :min="0"
                step="0.01"
                placeholder="0.00"
              />
            </div>

            <!-- Supprimer -->
            <div class="col-span-1 flex justify-end">
              <button
                type="button"
                @click="removeLine(idx)"
                class="rounded-full p-1 text-red-400 hover:text-red-600 transition"
                title="Supprimer cette ligne"
              >
                ✕
              </button>
            </div>
          </div>
        </div>
      </div>

      <p v-if="error" class="rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving" :disabled="lines.length === 0">
          Envoyer la demande
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { productService } from '@/services/product.service'
import { purchaseOrderService } from '@/services/purchaseorder.service'
import type { ProductResponse } from '@/types/product.types'
import type { CreatePurchaseOrderRequest } from '@/types/purchaseorder.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ warehouseId: number }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')
const products = ref<ProductResponse[]>([])

const form = ref({ expectedDate: '', note: '' })
const lines = ref<{ productId: number | ''; quantity: number; unitPrice: number | null }[]>([])

// Produits groupés par catégorie pour l'affichage dans les selects
const groupedProducts = computed(() => {
  const map = new Map<string, ProductResponse[]>()
  for (const p of products.value) {
    const key = p.categoryName ?? 'Sans catégorie'
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(p)
  }
  return Array.from(map.entries()).map(([category, prods]) => ({ category, products: prods }))
})

function addLine() {
  lines.value.push({ productId: '', quantity: 1, unitPrice: null })
}

function removeLine(idx: number) {
  lines.value.splice(idx, 1)
}

async function handleSubmit() {
  error.value = ''

  // Validation : toutes les lignes doivent avoir un produit sélectionné
  const hasEmpty = lines.value.some((l) => l.productId === '')
  if (hasEmpty) {
    error.value = 'Veuillez sélectionner un produit pour chaque ligne'
    return
  }

  // Pas de produit en double
  const ids = lines.value.map((l) => l.productId)
  if (new Set(ids).size !== ids.length) {
    error.value = 'Vous ne pouvez pas commander le même produit deux fois sur la même commande'
    return
  }

  saving.value = true
  try {
    const payload: CreatePurchaseOrderRequest = {
      expectedDate: form.value.expectedDate || undefined,
      note: form.value.note || undefined,
      lines: lines.value.map((l) => ({
        productId: l.productId as number,
        quantity: l.quantity,
        unitPrice: l.unitPrice ?? undefined,
      })),
    }
    await purchaseOrderService.create(props.warehouseId, payload)
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const res = await productService.listByWarehouse(props.warehouseId, { size: 200 })
    products.value = res.content.filter((p) => p.isActive)
  } catch {
    // pas de produits disponibles — l'utilisateur verra la liste vide
  }
})
</script>
