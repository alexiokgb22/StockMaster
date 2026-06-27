<template>
  <div class="min-h-screen bg-gray-50 px-6 py-8">
    <PageHeader
      title="Journal d'activité"
      subtitle="Historique de toutes les opérations effectuées dans le système"
    />

    <!-- Filtres -->
    <BaseCard class="mt-6">
      <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">

        <!-- Type d'opération -->
        <div>
          <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-text-secondary">
            Type d'opération
          </label>
          <select
            v-model="filters.module"
            class="w-full rounded-lg border border-border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
            @change="onFilterChange"
          >
            <option value="">Toutes les opérations</option>
            <option v-for="m in MODULE_OPTIONS" :key="m.value" :value="m.value">
              {{ m.label }}
            </option>
          </select>
        </div>

        <!-- Résultat -->
        <div>
          <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-text-secondary">
            Résultat
          </label>
          <select
            v-model="outcomeFilter"
            class="w-full rounded-lg border border-border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
            @change="onOutcomeChange"
          >
            <option value="">Tous les résultats</option>
            <option value="success">Réussies</option>
            <option value="failed">Échouées</option>
          </select>
        </div>

        <!-- Utilisateur -->
        <div>
          <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-text-secondary">
            Effectuée par
          </label>
          <BaseInput
            v-model="filters.username"
            placeholder="Nom d'utilisateur…"
            @update:model-value="debouncedSearch"
          />
        </div>

        <!-- Entrepôt (admin seulement) -->
        <div v-if="isAdmin">
          <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-text-secondary">
            Entrepôt
          </label>
          <select
            v-model="filters.warehouseId"
            class="w-full rounded-lg border border-border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
            @change="onFilterChange"
          >
            <option :value="undefined">Tous les entrepôts</option>
            <option v-for="wh in warehouses" :key="wh.id" :value="wh.id">
              {{ wh.name }}
            </option>
          </select>
        </div>
      </div>

      <!-- Filtres dates -->
      <div class="mt-3 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <div>
          <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-text-secondary">
            Du
          </label>
          <input
            v-model="filters.from"
            type="datetime-local"
            class="w-full rounded-lg border border-border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
            @change="onFilterChange"
          />
        </div>
        <div>
          <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-text-secondary">
            Au
          </label>
          <input
            v-model="filters.to"
            type="datetime-local"
            class="w-full rounded-lg border border-border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
            @change="onFilterChange"
          />
        </div>
        <div class="flex items-end">
          <button
            @click="resetFilters"
            class="rounded-lg border border-border bg-white px-4 py-2 text-sm text-text-secondary transition-colors hover:bg-gray-100"
          >
            Réinitialiser les filtres
          </button>
        </div>
      </div>
    </BaseCard>

    <!-- Résultats -->
    <div class="mt-4">
      <div
        v-if="loading"
        class="rounded-2xl border border-border bg-white px-6 py-8 text-center text-sm text-text-secondary"
      >
        Chargement du journal…
      </div>

      <div
        v-else-if="error"
        class="rounded-2xl border border-red-200 bg-red-50 px-6 py-4 text-sm text-red-700"
      >
        {{ error }}
      </div>

      <template v-else>
        <div class="mb-2 text-sm text-text-secondary">
          {{ totalElements }} opération{{ totalElements !== 1 ? 's' : '' }} trouvée{{ totalElements !== 1 ? 's' : '' }}
        </div>

        <BaseCard v-if="logs.length === 0" class="py-12 text-center">
          <p class="text-text-secondary">Aucune opération enregistrée avec ces critères.</p>
        </BaseCard>

        <!-- Liste des événements — style "feed" -->
        <div v-else class="space-y-2">
          <div
            v-for="log in logs"
            :key="log.id"
            class="flex items-start gap-4 rounded-xl border border-border bg-white px-5 py-4 transition-colors hover:bg-gray-50/80"
          >
            <!-- Icône de statut -->
            <div
              :class="[
                'mt-0.5 flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full text-base',
                outcomeIconClass(log.action),
              ]"
            >
              {{ outcomeIcon(log.action) }}
            </div>

            <!-- Contenu principal -->
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
                <!-- Description lisible — c'est le titre de l'événement -->
                <span class="font-medium text-text-main">
                  {{ log.description || formatFallbackDescription(log) }}
                </span>
                <!-- Badge type d'opération -->
                <span class="rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-600">
                  {{ formatModule(log.module) }}
                </span>
                <!-- Badge résultat -->
                <span :class="outcomeBadgeClass(log.action)">
                  {{ formatOutcome(log.action) }}
                </span>
              </div>

              <!-- Ligne de métadonnées secondaires -->
              <div class="mt-1 flex flex-wrap items-center gap-x-4 gap-y-0.5 text-xs text-text-secondary">
                <span>
                  Par <span class="font-medium text-text-main">{{ log.username }}</span>
                  <span v-if="log.userRole"> · {{ formatRole(log.userRole) }}</span>
                </span>
                <span v-if="log.warehouseName">📦 {{ log.warehouseName }}</span>
                <span>{{ formatDate(log.createdAt) }}</span>
              </div>
            </div>

            <!-- Bouton détail (uniquement si données avant/après disponibles) -->
            <button
              v-if="log.oldValue || log.newValue"
              @click="openDetail(log)"
              class="flex-shrink-0 rounded-lg border border-border px-3 py-1.5 text-xs text-text-secondary transition-colors hover:bg-gray-100 hover:text-text-main"
            >
              Voir les modifications
            </button>
          </div>
        </div>

        <!-- Pagination -->
        <div class="mt-4 flex items-center justify-between">
          <span class="text-sm text-text-secondary">
            Page {{ currentPage + 1 }} / {{ totalPages }}
          </span>
          <div class="flex gap-2">
            <button
              :disabled="currentPage === 0"
              @click="changePage(currentPage - 1)"
              class="rounded-lg border border-border px-3 py-1.5 text-sm transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-40"
            >
              ← Précédent
            </button>
            <button
              :disabled="currentPage >= totalPages - 1"
              @click="changePage(currentPage + 1)"
              class="rounded-lg border border-border px-3 py-1.5 text-sm transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-40"
            >
              Suivant →
            </button>
          </div>
        </div>
      </template>
    </div>

    <!-- Modal détail avant/après -->
    <Teleport to="body">
      <div
        v-if="detailLog"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
        @click.self="detailLog = null"
      >
        <div class="w-full max-w-2xl rounded-2xl bg-white shadow-xl">
          <div class="flex items-center justify-between border-b border-border px-6 py-4">
            <div>
              <h3 class="text-base font-semibold text-text-main">
                Détail de l'opération
              </h3>
              <p class="mt-0.5 text-sm text-text-secondary">
                {{ detailLog.description || formatFallbackDescription(detailLog) }}
              </p>
            </div>
            <button @click="detailLog = null" class="text-xl text-text-secondary hover:text-text-main">
              ✕
            </button>
          </div>
          <div class="space-y-4 p-6">
            <!-- Informations contextuelles lisibles -->
            <div class="grid grid-cols-2 gap-3 rounded-xl bg-gray-50 p-4 text-sm">
              <div>
                <span class="text-xs text-text-secondary">Opération</span>
                <p class="font-medium">{{ formatModule(detailLog.module) }}</p>
              </div>
              <div>
                <span class="text-xs text-text-secondary">Résultat</span>
                <p class="font-medium">{{ formatOutcome(detailLog.action) }}</p>
              </div>
              <div>
                <span class="text-xs text-text-secondary">Effectuée par</span>
                <p class="font-medium">{{ detailLog.username }}</p>
              </div>
              <div>
                <span class="text-xs text-text-secondary">Date</span>
                <p class="font-medium">{{ formatDate(detailLog.createdAt) }}</p>
              </div>
            </div>

            <!-- Données avant/après — uniquement si présentes -->
            <div v-if="detailLog.oldValue || detailLog.newValue" class="space-y-3">
              <p class="text-sm font-medium text-text-secondary">Modifications enregistrées :</p>
              <div v-if="detailLog.oldValue" class="rounded-xl border border-red-200 bg-red-50 p-4">
                <p class="mb-2 text-xs font-semibold uppercase tracking-wide text-red-600">État avant</p>
                <pre class="overflow-auto text-xs text-red-800">{{ formatJson(detailLog.oldValue) }}</pre>
              </div>
              <div v-if="detailLog.newValue" class="rounded-xl border border-emerald-200 bg-emerald-50 p-4">
                <p class="mb-2 text-xs font-semibold uppercase tracking-wide text-emerald-600">État après</p>
                <pre class="overflow-auto text-xs text-emerald-800">{{ formatJson(detailLog.newValue) }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { auditLogService } from '@/services/auditlog.service'
import { warehouseService } from '@/services/warehouse.service'
import type { AuditLogResponse, AuditLogFilters } from '@/types/auditlog.types'
import type { WarehouseResponse } from '@/types/warehouse.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseInput from '@/components/ui/BaseInput.vue'

// ── Configuration des libellés métier ─────────────────────────────────────────

/** Correspondance module technique → libellé lisible pour les filtres */
const MODULE_OPTIONS = [
  { value: 'reception',     label: 'Réceptions de marchandises' },
  { value: 'dispatch',      label: 'Sorties de stock' },
  { value: 'transfer',      label: 'Transferts entre entrepôts' },
  { value: 'stock',         label: 'Ajustements de stock' },
  { value: 'purchaseorder', label: 'Commandes fournisseurs' },
  { value: 'inventory',     label: 'Inventaires' },
  { value: 'user',          label: 'Gestion des utilisateurs' },
  { value: 'warehouse',     label: 'Gestion des entrepôts' },
]

/** Correspondance action technique → libellé lisible */
const ACTION_LABELS: Record<string, string> = {
  CREATE:            'Créée',
  UPDATE:            'Modifiée',
  VALIDATE:          'Validée',
  REJECT:            'Rejetée',
  CANCEL:            'Annulée',
  RECEIVE:           'Réceptionnée',
  COMPLETE:          'Clôturée',
  DELETE:            'Supprimée',
  CREATE_FAILED:     'Échec de création',
  VALIDATE_FAILED:   'Échec de validation',
  REJECT_FAILED:     'Échec de rejet',
  CANCEL_FAILED:     'Échec d\'annulation',
  RECEIVE_FAILED:    'Échec de réception',
}

/** Correspondance rôle technique → libellé lisible */
const ROLE_LABELS: Record<string, string> = {
  'Administrateur':           'Administrateur',
  'Gestionnaire d\'entrepôt': 'Gestionnaire',
  'Magasinier':               'Magasinier',
  'Auditeur':                 'Auditeur',
}

// ── State ──────────────────────────────────────────────────────────────────────

const authStore = useAuthStore()
const currentUser = computed(() => authStore.currentUser)
const isAdmin = computed(() => currentUser.value?.role === 'Administrateur')

const loading = ref(false)
const error = ref('')
const logs = ref<AuditLogResponse[]>([])
const totalElements = ref(0)
const totalPages = ref(1)
const currentPage = ref(0)
const warehouses = ref<WarehouseResponse[]>([])
const detailLog = ref<AuditLogResponse | null>(null)

/** Filtre "résultat" : 'success' | 'failed' | '' */
const outcomeFilter = ref('')

const filters = ref<AuditLogFilters & { from?: string; to?: string }>({
  module: '',
  action: '',
  username: '',
  warehouseId: undefined,
  from: '',
  to: '',
})

// ── Filtres ───────────────────────────────────────────────────────────────────

let debounceTimer: ReturnType<typeof setTimeout>
function debouncedSearch() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => { currentPage.value = 0; fetchLogs() }, 400)
}

function onFilterChange() {
  currentPage.value = 0
  fetchLogs()
}

function onOutcomeChange() {
  // On mappe le filtre lisible vers l'action technique envoyée au backend
  // "failed" → on filtre sur les actions contenant "_FAILED"
  // Pour l'instant on reset l'action et on filtre côté frontend sur le résultat
  currentPage.value = 0
  fetchLogs()
}

function resetFilters() {
  filters.value = { module: '', action: '', username: '', warehouseId: undefined, from: '', to: '' }
  outcomeFilter.value = ''
  currentPage.value = 0
  fetchLogs()
}

function changePage(page: number) {
  currentPage.value = page
  fetchLogs()
}

// ── Chargement ────────────────────────────────────────────────────────────────

async function fetchLogs() {
  loading.value = true
  error.value = ''
  try {
    const params: AuditLogFilters = { page: currentPage.value, size: 50 }
    if (filters.value.module)      params.module      = filters.value.module
    if (filters.value.username)    params.username    = filters.value.username
    if (filters.value.warehouseId) params.warehouseId = filters.value.warehouseId
    if (filters.value.from)        params.from        = new Date(filters.value.from as string).toISOString()
    if (filters.value.to)          params.to          = new Date(filters.value.to as string).toISOString()

    // Filtre résultat : on laisse le backend chercher en passant l'info dans l'action si possible
    // sinon on filtre côté client après (liste déjà paginée à 50, acceptable)
    const res = await auditLogService.search(params)

    let content = res.content
    if (outcomeFilter.value === 'success') {
      content = content.filter((l) => !l.action.includes('_FAILED'))
    } else if (outcomeFilter.value === 'failed') {
      content = content.filter((l) => l.action.includes('_FAILED'))
    }

    logs.value = content
    totalElements.value = res.totalElements
    totalPages.value = res.totalPages
  } catch {
    error.value = 'Impossible de charger le journal d\'activité.'
  } finally {
    loading.value = false
  }
}

async function fetchWarehouses() {
  if (!isAdmin.value) return
  try {
    const res = await warehouseService.list({ size: 100 })
    warehouses.value = res.content
  } catch {}
}

function openDetail(log: AuditLogResponse) {
  detailLog.value = log
}

// ── Fonctions de formatage lisibles ──────────────────────────────────────────

function formatDate(value: string) {
  return new Date(value).toLocaleString('fr-FR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

function formatModule(m: string) {
  return MODULE_OPTIONS.find((o) => o.value === m)?.label ?? m
}

function formatRole(r: string) {
  return ROLE_LABELS[r] ?? r
}

/**
 * Transforme l'action technique en libellé lisible.
 * Ex: "VALIDATE" → "Validée" / "CREATE_FAILED" → "Échec de création"
 */
function formatOutcome(action: string) {
  return ACTION_LABELS[action] ?? action
}

/**
 * Si la description est vide (cas rare), construit une phrase de secours lisible.
 * On évite d'exposer "Reception #42" ou "VALIDATE" bruts.
 */
function formatFallbackDescription(log: AuditLogResponse) {
  const module  = formatModule(log.module)
  const outcome = formatOutcome(log.action).toLowerCase()
  return `${module} — opération ${outcome}`
}

/** Icône emoji selon le résultat de l'action */
function outcomeIcon(action: string) {
  if (action.includes('_FAILED'))                             return '⚠️'
  if (['VALIDATE', 'RECEIVE', 'COMPLETE'].includes(action))  return '✅'
  if (['REJECT', 'CANCEL', 'DELETE'].includes(action))       return '🚫'
  if (action === 'CREATE')                                    return '➕'
  if (action === 'UPDATE')                                    return '✏️'
  return '📋'
}

/** Classe CSS du cercle autour de l'icône */
function outcomeIconClass(action: string) {
  if (action.includes('_FAILED'))                             return 'bg-amber-100'
  if (['VALIDATE', 'RECEIVE', 'COMPLETE'].includes(action))  return 'bg-emerald-100'
  if (['REJECT', 'CANCEL', 'DELETE'].includes(action))       return 'bg-red-100'
  if (action === 'CREATE')                                    return 'bg-blue-100'
  return 'bg-gray-100'
}

/** Classe CSS du badge de résultat */
function outcomeBadgeClass(action: string) {
  const base = 'rounded-full px-2.5 py-0.5 text-xs font-medium'
  if (action.includes('_FAILED'))                             return `${base} bg-amber-100 text-amber-700`
  if (['VALIDATE', 'RECEIVE', 'COMPLETE'].includes(action))  return `${base} bg-emerald-100 text-emerald-700`
  if (['REJECT', 'CANCEL', 'DELETE'].includes(action))       return `${base} bg-red-100 text-red-700`
  if (action === 'CREATE')                                    return `${base} bg-blue-100 text-blue-700`
  if (action === 'UPDATE')                                    return `${base} bg-amber-100 text-amber-700`
  return `${base} bg-gray-100 text-gray-700`
}

function formatJson(value: string) {
  try { return JSON.stringify(JSON.parse(value), null, 2) }
  catch { return value }
}

onMounted(() => {
  fetchWarehouses()
  fetchLogs()
})
</script>
