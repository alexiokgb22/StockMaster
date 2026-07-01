<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
    <div class="w-full max-w-lg overflow-hidden rounded-[32px] bg-white p-8 shadow-card">
      <div class="mb-6 flex items-center justify-between">
        <h2 class="text-lg font-semibold text-primary">Nouveau rapport journalier</h2>
        <button @click="$emit('close')" class="rounded-full border border-border px-3 py-1.5 text-sm text-text-secondary hover:text-primary">
          Fermer
        </button>
      </div>

      <form @submit.prevent="submit" class="space-y-4">

        <!-- Date -->
        <div class="space-y-1.5">
          <label class="block text-sm font-medium text-text-main">Date de la journée *</label>
          <input
            v-model="form.reportDate"
            type="date"
            :max="today"
            required
            class="w-full rounded-2xl border border-border bg-white px-4 py-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
          />
        </div>

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
          <label class="block text-sm font-medium text-text-main">
            Incidents
            <span class="ml-1 text-xs text-text-secondary font-normal">(laisser vide si aucun)</span>
          </label>
          <textarea
            v-model="form.incidents"
            rows="3"
            placeholder="Décrivez les incidents survenus (produits endommagés, problèmes matériels, accidents…)"
            class="w-full rounded-2xl border border-border bg-white px-4 py-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
          />
        </div>

        <!-- Observations -->
        <div class="space-y-1.5">
          <label class="block text-sm font-medium text-text-main">Observations générales</label>
          <textarea
            v-model="form.observations"
            rows="3"
            placeholder="Remarques terrain, état du stock, suggestions…"
            class="w-full rounded-2xl border border-border bg-white px-4 py-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15 resize-none"
          />
        </div>

        <!-- Info -->
        <p class="text-xs text-text-secondary bg-blue-50 rounded-xl px-4 py-3">
          ℹ Le rapport est créé en brouillon. Les réceptions et sorties du jour seront
          automatiquement listées quand vous le soumettrez.
        </p>

        <!-- Actions -->
        <div class="flex justify-end gap-3 pt-2">
          <BaseButton type="button" variant="secondary" @click="$emit('close')">
            Annuler
          </BaseButton>
          <BaseButton type="submit" :disabled="loading || !form.reportDate">
            {{ loading ? 'Enregistrement…' : 'Créer le rapport' }}
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
import BaseButton from '@/components/ui/BaseButton.vue'

const emit  = defineEmits<{ close: []; saved: [] }>()
const toast = useToastStore()

const today = new Date().toISOString().split('T')[0]

const loading = ref(false)
const form = ref({
  reportDate:    today,
  arrivalTime:   '',
  departureTime: '',
  incidents:     '',
  observations:  '',
})

async function submit() {
  loading.value = true
  try {
    await activityReportService.create({
      reportDate:    form.value.reportDate,
      arrivalTime:   form.value.arrivalTime   || undefined,
      departureTime: form.value.departureTime || undefined,
      incidents:     form.value.incidents     || undefined,
      observations:  form.value.observations  || undefined,
    })
    toast.success('Rapport créé en brouillon')
    emit('saved')
  } catch (e: any) {
    toast.error(e?.response?.data?.message ?? 'Impossible de créer le rapport')
  } finally {
    loading.value = false
  }
}
</script>
