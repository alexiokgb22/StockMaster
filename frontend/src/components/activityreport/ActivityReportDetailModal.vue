<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
    <div class="w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-[32px] bg-white p-8 shadow-card">
      <div class="mb-6 flex items-center justify-between">
        <h2 class="text-lg font-semibold text-primary">Détail du rapport</h2>
        <button @click="$emit('close')" class="rounded-full border border-border px-3 py-1.5 text-sm text-text-secondary hover:text-primary">
          Fermer
        </button>
      </div>

      <div v-if="loading" class="py-12 text-center text-sm text-text-secondary">
        Chargement…
      </div>

      <div v-else-if="report" class="space-y-6">

        <!-- En-tête -->
        <div class="flex items-center justify-between">
          <div>
            <div class="text-xl font-bold text-text-main">{{ formatDate(report.reportDate) }}</div>
            <div class="mt-0.5 text-sm text-text-secondary">{{ report.storekeeperUsername }} · {{ report.warehouseName }}</div>
          </div>
          <StatusBadge
            :label="report.status === 'DRAFT' ? 'Brouillon' : 'Soumis'"
            :variant="report.status === 'DRAFT' ? 'warning' : 'success'"
          />
        </div>

        <!-- Horaires -->
        <div class="grid grid-cols-2 gap-4 rounded-2xl bg-gray-50 p-4">
          <div>
            <div class="text-xs text-text-secondary uppercase tracking-wide">Heure d'arrivée</div>
            <div class="mt-1 font-semibold">{{ report.arrivalTime ?? '—' }}</div>
          </div>
          <div>
            <div class="text-xs text-text-secondary uppercase tracking-wide">Heure de départ</div>
            <div class="mt-1 font-semibold">{{ report.departureTime ?? '—' }}</div>
          </div>
        </div>

        <!-- Compteurs opérations -->
        <div class="grid grid-cols-2 gap-4">
          <div class="rounded-2xl border border-border p-4 text-center">
            <div class="text-2xl font-bold text-primary">{{ report.receptionCount }}</div>
            <div class="text-xs text-text-secondary mt-1">Réception(s)</div>
          </div>
          <div class="rounded-2xl border border-border p-4 text-center">
            <div class="text-2xl font-bold text-primary">{{ report.dispatchCount }}</div>
            <div class="text-xs text-text-secondary mt-1">Sortie(s)</div>
          </div>
        </div>

        <!-- Liste des réceptions -->
        <div v-if="report.receptions && report.receptions.length > 0">
          <div class="mb-2 text-sm font-semibold text-text-main">Réceptions effectuées</div>
          <div class="space-y-2">
            <div
              v-for="op in report.receptions"
              :key="op.id"
              class="flex items-center justify-between rounded-xl bg-green-50 px-4 py-2 text-sm"
            >
              <span class="font-mono text-xs text-green-700">{{ op.number }}</span>
              <span class="text-text-secondary">{{ op.lineCount }} produit(s)</span>
              <span class="text-text-secondary">{{ formatTime(op.doneAt) }}</span>
            </div>
          </div>
        </div>

        <!-- Liste des sorties -->
        <div v-if="report.dispatches && report.dispatches.length > 0">
          <div class="mb-2 text-sm font-semibold text-text-main">Sorties effectuées</div>
          <div class="space-y-2">
            <div
              v-for="op in report.dispatches"
              :key="op.id"
              class="flex items-center justify-between rounded-xl bg-red-50 px-4 py-2 text-sm"
            >
              <span class="font-mono text-xs text-red-700">{{ op.number }}</span>
              <span class="text-text-secondary">{{ op.lineCount }} produit(s)</span>
              <span class="text-text-secondary">{{ formatTime(op.doneAt) }}</span>
            </div>
          </div>
        </div>

        <!-- Incidents -->
        <div v-if="report.incidents">
          <div class="mb-2 text-sm font-semibold text-amber-700">⚠ Incidents signalés</div>
          <div class="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-text-main whitespace-pre-wrap">
            {{ report.incidents }}
          </div>
        </div>
        <div v-else class="rounded-2xl bg-green-50 px-4 py-3 text-sm text-green-700">
          ✓ Aucun incident signalé
        </div>

        <!-- Observations -->
        <div v-if="report.observations">
          <div class="mb-2 text-sm font-semibold text-text-main">Observations générales</div>
          <div class="rounded-2xl bg-gray-50 px-4 py-3 text-sm text-text-main whitespace-pre-wrap">
            {{ report.observations }}
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useToastStore } from '@/stores/toast'
import { activityReportService } from '@/services/activityreport.service'
import type { ActivityReportResponse } from '@/types/activityreport.types'
import StatusBadge from '@/components/ui/StatusBadge.vue'

const props = defineProps<{ reportId: number }>()
defineEmits<{ close: [] }>()
const toast = useToastStore()

const loading = ref(false)
const report  = ref<ActivityReportResponse | null>(null)

async function load() {
  loading.value = true
  try {
    report.value = await activityReportService.getById(props.reportId)
  } catch {
    toast.error('Impossible de charger le rapport')
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('fr-FR', {
    weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
  })
}

function formatTime(dateStr: string): string {
  return new Date(dateStr).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
}

onMounted(load)
</script>
