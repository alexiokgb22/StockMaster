<template>
  <div class="space-y-6">
    <PageHeader title="Fournisseurs" subtitle="Gestion des fournisseurs et de leurs affectations">
      <BaseButton @click="openCreate">+ Nouveau fournisseur</BaseButton>
    </PageHeader>

    <BaseCard>
      <div class="flex flex-wrap gap-3">
        <BaseInput v-model="filters.search" placeholder="Rechercher…" class="max-w-xs" @input="debouncedFetch" />
        <select
          v-model="filters.active"
          @change="fetchSuppliers"
          class="rounded-3xl border border-border bg-surface px-4 py-2 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
        >
          <option value="">Tous</option>
          <option value="true">Actifs</option>
          <option value="false">Inactifs</option>
        </select>
      </div>
    </BaseCard>

    <BaseCard>
      <div v-if="loading" class="py-8 text-center text-sm text-text-secondary">Chargement…</div>
      <BaseTable v-else :columns="columns">
        <tr
          v-for="s in suppliers"
          :key="s.id"
          class="border-t border-border hover:bg-primary-light/30 cursor-pointer"
          @click="goToDetail(s.id)"
        >
          <td class="px-4 py-3">
            <div class="font-medium text-text-main">{{ s.name }}</div>
            <div v-if="s.contactName" class="text-xs text-text-secondary">{{ s.contactName }}</div>
          </td>
          <td class="px-4 py-3 text-text-secondary">{{ s.city ?? '—' }}</td>
          <td class="px-4 py-3 text-text-secondary">{{ s.phone ?? '—' }}</td>
          <td class="px-4 py-3 text-text-secondary">{{ s.email ?? '—' }}</td>
          <td class="px-4 py-3">
            <StatusBadge :label="s.isActive ? 'Actif' : 'Inactif'" :variant="s.isActive ? 'success' : 'secondary'" />
          </td>
          <td class="px-4 py-3 space-x-2" @click.stop>
            <BaseButton size="sm" variant="secondary" @click="openEdit(s)">Modifier</BaseButton>
            <BaseButton size="sm" :variant="s.isActive ? 'danger' : 'secondary'" @click="handleToggle(s)">
              {{ s.isActive ? 'Désactiver' : 'Activer' }}
            </BaseButton>
          </td>
        </tr>
      </BaseTable>

      <EmptyState v-if="!loading && suppliers.length === 0" title="Aucun fournisseur" description="Créez votre premier fournisseur." />

      <div class="mt-4 flex items-center justify-between">
        <span class="text-sm text-text-secondary">{{ totalElements }} fournisseur(s)</span>
        <div class="flex gap-2">
          <BaseButton size="sm" variant="secondary" :disabled="page === 0" @click="page--; fetchSuppliers()">Précédent</BaseButton>
          <span class="py-1 text-sm">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
          <BaseButton size="sm" variant="secondary" :disabled="page + 1 >= totalPages" @click="page++; fetchSuppliers()">Suivant</BaseButton>
        </div>
      </div>
    </BaseCard>

    <SupplierFormModal
      v-if="showModal"
      :supplier="selectedSupplier"
      @close="showModal = false"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { supplierService } from '@/services/supplier.service'
import type { SupplierResponse } from '@/types/supplier.types'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import SupplierFormModal from '@/components/supplier/SupplierFormModal.vue'
import { useToastStore } from '@/stores/toast'

const router = useRouter()
const toast = useToastStore()

const suppliers     = ref<SupplierResponse[]>([])
const loading       = ref(false)
const page          = ref(0)
const totalPages    = ref(1)
const totalElements = ref(0)
const showModal        = ref(false)
const selectedSupplier = ref<SupplierResponse | null>(null)
const filters = ref({ search: '', active: '' })

const columns = [
  { key: 'name',    label: 'Nom' },
  { key: 'city',    label: 'Ville' },
  { key: 'phone',   label: 'Téléphone' },
  { key: 'email',   label: 'Email' },
  { key: 'status',  label: 'Statut' },
  { key: 'actions', label: '' },
]

let debounceTimer: ReturnType<typeof setTimeout>
function debouncedFetch() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => { page.value = 0; fetchSuppliers() }, 300)
}

async function fetchSuppliers() {
  loading.value = true
  try {
    const res = await supplierService.listAll({
      search: filters.value.search || undefined,
      active: filters.value.active !== '' ? filters.value.active === 'true' : undefined,
      page: page.value,
      size: 20,
    })
    suppliers.value     = res.content
    totalPages.value    = res.totalPages
    totalElements.value = res.totalElements
  } finally {
    loading.value = false
  }
}

function openCreate() { selectedSupplier.value = null; showModal.value = true }
function openEdit(s: SupplierResponse) { selectedSupplier.value = s; showModal.value = true }
function goToDetail(id: number) { router.push({ name: 'SupplierDetail', params: { id } }) }

async function handleToggle(s: SupplierResponse) {
  try {
    await supplierService.toggle(s.id)
    toast.success(s.isActive ? 'Fournisseur désactivé' : 'Fournisseur activé')
    fetchSuppliers()
  } catch (e: any) {
    toast.error(e.response?.data?.message ?? 'Erreur')
  }
}

function onSaved() { showModal.value = false; fetchSuppliers() }

onMounted(fetchSuppliers)
</script>
