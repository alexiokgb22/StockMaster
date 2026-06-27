<template>
  <BaseModal title="Nouvel inventaire" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-5">

      <!-- Type -->
      <FormField label="Type d'inventaire" required>
        <div class="flex gap-4">
          <label class="flex items-center gap-2 cursor-pointer">
            <input type="radio" v-model="form.inventoryType" value="FULL" class="accent-primary" />
            <div>
              <span class="text-sm font-medium text-text-main">Complet</span>
              <p class="text-xs text-text-secondary">Toutes les lignes de stock de l'entrepôt</p>
            </div>
          </label>
          <label class="flex items-center gap-2 cursor-pointer">
            <input type="radio" v-model="form.inventoryType" value="PARTIAL" class="accent-primary" />
            <div>
              <span class="text-sm font-medium text-text-main">Partiel</span>
              <p class="text-xs text-text-secondary">Zones sélectionnées uniquement</p>
            </div>
          </label>
        </div>
      </FormField>

      <!-- Sélection zones (PARTIAL uniquement) -->
      <FormField v-if="form.inventoryType === 'PARTIAL'" label="Zones à inventorier" required>
        <div v-if="loadingZones" class="text-xs text-text-secondary">Chargement des zones…</div>
        <div v-else-if="zones.length === 0" class="text-xs text-text-secondary">
          Aucune zone disponible.
        </div>
        <div v-else class="space-y-1 max-h-40 overflow-y-auto rounded-lg border border-border p-2">
          <label
            v-for="z in zones"
            :key="z.id"
            class="flex items-center gap-2 cursor-pointer rounded px-2 py-1 hover:bg-primary/5 text-sm"
          >
            <input type="checkbox" :value="z.id" v-model="form.zoneIds" class="accent-primary" />
            <span class="font-medium">{{ z.name }}</span>
            <span v-if="z.zoneType" class="text-xs text-text-secondary">({{ z.zoneType }})</span>
          </label>
        </div>
        <p v-if="form.inventoryType === 'PARTIAL' && form.zoneIds.length === 0" class="mt-1 text-xs text-amber-600">
          Sélectionnez au moins une zone.
        </p>
      </FormField>

      <!-- Note -->
      <FormField label="Note (optionnel)">
        <textarea
          v-model="form.note"
          rows="2"
          placeholder="Contexte de cet inventaire…"
          class="w-full rounded-2xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
        />
      </FormField>

      <!-- Avertissement -->
      <div class="rounded-xl bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-700">
        ⚠ Les quantités théoriques seront snapshotées au moment de la création.
        La clôture ajustera définitivement le stock selon les comptages physiques.
      </div>

      <p v-if="error" class="rounded-xl bg-red-50 px-4 py-2 text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving">Démarrer l'inventaire</BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { zoneService } from '@/services/zone.service'
import { inventoryService } from '@/services/inventory.service'
import type { ZoneResponse } from '@/types/zone.types'
import type { CreateInventoryRequest, InventoryType } from '@/types/inventory.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ warehouseId: number }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')
const zones = ref<ZoneResponse[]>([])
const loadingZones = ref(false)

const form = ref<{
  inventoryType: InventoryType
  note: string
  zoneIds: number[]
}>({
  inventoryType: 'FULL',
  note: '',
  zoneIds: [],
})

// Charger les zones quand on passe en mode PARTIAL
watch(() => form.value.inventoryType, (type) => {
  if (type === 'PARTIAL' && zones.value.length === 0) loadZones()
  if (type === 'FULL') form.value.zoneIds = []
})

async function loadZones() {
  loadingZones.value = true
  try {
    const res = await zoneService.list(props.warehouseId, { size: 100 })
    zones.value = res.content
  } catch {
    // silencieux
  } finally {
    loadingZones.value = false
  }
}

async function handleSubmit() {
  error.value = ''

  if (form.value.inventoryType === 'PARTIAL' && form.value.zoneIds.length === 0) {
    error.value = 'Sélectionnez au moins une zone pour un inventaire partiel'
    return
  }

  saving.value = true
  try {
    const payload: CreateInventoryRequest = {
      inventoryType: form.value.inventoryType,
      note: form.value.note || undefined,
      zoneIds: form.value.inventoryType === 'PARTIAL' ? form.value.zoneIds : undefined,
    }
    await inventoryService.create(props.warehouseId, payload)
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    saving.value = false
  }
}
</script>
