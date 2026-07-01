<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <PageHeader title="Sorties" subtitle="Historique des sorties de l'entrepôt" />
      <BaseButton v-if="isStorekeeper" @click="showCreateModal = true">
        + Nouveau bon de sortie
      </BaseButton>
    </div>

    <!-- Filtre statut -->
    <BaseCard>
      <select
        v-model="statusFilter"
        @change="page = 0; fetchDispatches()"
        class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
      >
        <option value="">Tous les statuts</option>
        <option value="VALIDATED">Validé</option>
      </select>
    </BaseCard>

    <!-- Liste -->
    <BaseCard>
      <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">
        Chargement…
      </div>
      <EmptyState
        v-else-if="dispatches.length === 0"
        title="Aucun bon de sortie"
        description="L'historique des sorties apparaîtra ici."
      />
      <div v-else class="space-y-3">
        <div
          v-for="d in dispatches"
          :key="d.id"
          class="rounded-xl border border-border bg-surface p-4"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <code class="rounded bg-gray-100 px-2 py-0.5 text-xs font-mono">
                {{ d.dispatchNumber }}
              </code>
              <StatusBadge label="Validé" variant="success" />
            </div>
            <span class="text-sm text-text-secondary">
              {{ new Date(d.createdAt).toLocaleDateString('fr-FR') }}
            </span>
          </div>

          <div class="mt-2 text-sm text-text-secondary">
            {{ d.lines.length }} produit(s) · Par {{ d.createdByUsername }}
            <span v-if="d.note" class="ml-2 italic">— {{ d.note }}</span>
          </div>

          <!-- Infos client -->
          <div class="mt-1 text-xs text-text-secondary">
            Client : {{ d.clientFirstName }} {{ d.clientLastName }}
            <span v-if="d.deliveryAddress"> · {{ d.deliveryAddress }}</span>
          </div>

          <div class="mt-3">
            <BaseButton
              v-if="can('dispatch.print_bordereau')"
              size="sm"
              variant="secondary"
              @click="printBordereau(d.id)"
            >
              Imprimer bordereau
            </BaseButton>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="mt-4 flex items-center justify-between">
        <span class="text-sm text-text-secondary">{{ totalElements }} bon(s)</span>
        <div class="flex gap-2">
          <BaseButton
            size="sm"
            variant="secondary"
            :disabled="page === 0"
            @click="page--; fetchDispatches()"
          >
            Précédent
          </BaseButton>
          <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
          <BaseButton
            size="sm"
            variant="secondary"
            :disabled="page + 1 >= totalPages"
            @click="page++; fetchDispatches()"
          >
            Suivant
          </BaseButton>
        </div>
      </div>
    </BaseCard>

    <!-- Modale création -->
    <DispatchCreateModal
      v-if="showCreateModal"
      :warehouse-id="warehouseId"
      @close="showCreateModal = false"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePermissions } from '@/composables/usePermissions'
import { dispatchService } from '@/services/dispatch.service'
import type { DispatchResponse, DispatchStatus } from '@/types/dispatch.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import DispatchCreateModal from '@/components/dispatch/DispatchCreateModal.vue'

const authStore = useAuthStore()
const { can }   = usePermissions()

const warehouseId   = computed(() => authStore.currentUser?.warehouseId!)
const isStorekeeper = computed(() => authStore.currentUser?.role === 'Magasinier')

// ── État ─────────────────────────────────────────────────────
const dispatches    = ref<DispatchResponse[]>([])
const loading       = ref(false)
const page          = ref(0)
const totalPages    = ref(1)
const totalElements = ref(0)
const statusFilter  = ref<DispatchStatus | ''>('')
const showCreateModal = ref(false)

// ── Chargement ────────────────────────────────────────────────
async function fetchDispatches() {
  loading.value = true
  try {
    const res = await dispatchService.list(warehouseId.value, {
      status: (statusFilter.value || undefined) as DispatchStatus | undefined,
      page:   page.value,
      size:   20,
    })
    dispatches.value    = res.content
    totalPages.value    = res.totalPages
    totalElements.value = res.totalElements
  } finally {
    loading.value = false
  }
}

// ── Actions ───────────────────────────────────────────────────
async function printBordereau(dispatchId: number) {
  const url      = dispatchService.getBordereauUrl(warehouseId.value, dispatchId)
  const dispatch = dispatches.value.find((d) => d.id === dispatchId)
  const fileName = dispatch
    ? `${dispatch.dispatchNumber.replace(/[^a-z0-9]/gi, '-').toLowerCase()}.html`
    : `bordereau-${dispatchId}.html`

  try {
    const response = await fetch(url, {
      credentials: 'include',
      headers: { Accept: 'text/html' },
    })
    if (!response.ok) throw new Error()

    const html    = await response.text()
    const blob    = new Blob([html], { type: 'text/html;charset=utf-8' })
    const blobUrl = URL.createObjectURL(blob)

    window.open(blobUrl, '_blank', 'noopener,noreferrer')

    const link = document.createElement('a')
    link.href  = blobUrl
    link.download = fileName
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    setTimeout(() => URL.revokeObjectURL(blobUrl), 1000)
  } catch {
    window.open(url, '_blank', 'noopener,noreferrer')
  }
}

function onSaved() {
  showCreateModal.value = false
  fetchDispatches()
}

onMounted(fetchDispatches)
</script>
