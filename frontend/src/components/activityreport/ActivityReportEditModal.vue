<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
    <div class="w-full max-w-lg overflow-hidden rounded-[32px] bg-white p-8 shadow-card">
      <div class="mb-6 flex items-center justify-between">
        <h2 class="text-lg font-semibold text-primary">Modifier le rapport</h2>
        <button @click="$emit('close')" class="rounded-full border border-border px-3 py-1.5 text-sm text-text-secondary hover:text-primary">
          Fermer
        </button>
      </div>

      <form @submit.prevent="submit" class="space-y-4">

        <!-- Horaires -->
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-1.5">
            <label class="block text-sm font-medium text-text-main">Heure d'arrivée</label>
            <input
              v-model="form.arrivalTime"
              type="time"
              class="w-full rounded-2xl border border-border bg-white px-4 py-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
            />
          </div>
          <div class="space-y-1.5">
            <label class="block text-sm font-medium text-text-main">Heure de départ</label>
            <input
              v-model="form.departureTime"
              type="time"
              class="w-full rounded-2xl border border-border bg-white px-4 py-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
            />
          </div>
        </div>

        <!-- Incidents -->
        <div class="space-y-1.5">
          <label class="block text-sm font-medium text-text-main">Incidents</label>
          <textarea
            v-model="form.incidents"
            rows="3"
            placeholder="Décrivez les incidents survenus…"
            class="w-full rounded-2xl border border-border bg-white px-4 py-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
          />
        </div>

        <!-- Observations -->
        <div class="space-y-1.5">
          <label class="block text-sm font-medium text-text-main">Observations générales</label>
          <textarea
            v-model="form.observations"
            rows="3"
            placeholder="Remarques terrain…"
            class="w-full rounded-2xl border border-border bg-white px-4 py-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
          />
        </div>

        <div class="flex justify-end gap-3 pt-2">
          <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
          <BaseButton type="submit" :disabled="loading">
            {{ loading ? 'Enregistrement…' : 'Enregistrer' }}
          </BaseButton>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useToastStore } from '@/stores/toast'
import { activityReportService } from '@/services/activityreport.service'
import type { ActivityReportResponse } from '@/types/activityreport.types'
import BaseButton from '@/components/ui/BaseButton.vue'

const props = defineProps<{ report: ActivityReportResponse }>()
const emit  = defineEmits<{ close: []; saved: [] }>()
const toast = useToastStore()

const loading = ref(false)
const form = ref({
  arrivalTime:   props.report.arrivalTime   ?? '',
  departureTime: props.report.departureTime ?? '',
  incidents:     props.report.incidents     ?? '',
  observations:  props.report.observations  ?? '',
})

async function submit() {
  loading.value = true
  try {
    await activityReportService.update(props.report.id, {
      arrivalTime:   form.value.arrivalTime   || undefined,
      departureTime: form.value.departureTime || undefined,
      incidents:     form.value.incidents     || undefined,
      observations:  form.value.observations  || undefined,
    })
    toast.success('Rapport mis à jour')
    emit('saved')
  } catch (e: any) {
    toast.error(e?.response?.data?.message ?? 'Impossible de modifier le rapport')
  } finally {
    loading.value = false
  }
}
</script>
