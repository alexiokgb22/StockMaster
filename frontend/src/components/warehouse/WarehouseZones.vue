<template>
  <div>
    <!-- Barre d'outils -->
    <div class="mb-4 flex items-center justify-between gap-4">
      <h3 class="text-base font-semibold text-text-main">
        Zones ({{ zones.length }})
      </h3>
      <BaseButton
        v-if="can('zone.create') && canCreateZone"
        @click="openCreate"
      >
        Nouvelle zone
      </BaseButton>
    </div>

    <!-- Bannière admin a tout couvert -->
    <div
      v-if="adminHasDefinedZones && currentUser?.role !== 'Administrateur' && uncoveredCategories.length === 0"
      class="mb-4 rounded-lg bg-blue-50 border border-blue-200 px-4 py-3 text-sm text-blue-700"
    >
      Les zones de cet entrepôt ont été entièrement définies par l'Administrateur.
    </div>

    <!-- Bannière admin a fait un setup partiel -->
    <div
      v-if="adminHasDefinedZones && currentUser?.role !== 'Administrateur' && uncoveredCategories.length > 0"
      class="mb-4 rounded-lg bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-700"
    >
      L'Administrateur a défini des zones pour certaines catégories.
      Vous pouvez créer des zones pour les catégories non encore couvertes :
      <span class="font-medium">{{ uncoveredCategories.map(c => c.name).join(', ') }}</span>.
    </div>

    <!-- Tableau zones -->
    <BaseTable :columns="columns">
      <tr
        v-for="zone in zones"
        :key="zone.id"
        class="border-t border-border hover:bg-primary-light/30"
      >
        <td class="px-4 py-4 font-mono text-sm font-medium text-text-main">{{ zone.name }}</td>
        <td class="px-4 py-4 text-text-secondary">{{ zone.zoneType ?? '—' }}</td>
        <td class="px-4 py-4 text-text-secondary">
          {{ zone.capacity != null ? zone.capacity + ' m³' : '—' }}
        </td>
        <td class="px-4 py-4">
          <span v-if="zone.categoryName" class="inline-flex items-center rounded-full bg-primary/10 px-2.5 py-0.5 text-xs font-medium text-primary">
            {{ zone.categoryName }}
          </span>
          <span v-else class="text-xs text-text-secondary">Non assignée</span>
        </td>
        <td class="px-4 py-4">
          <span
            class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
            :class="zone.isAdminDefined ? 'bg-purple-100 text-purple-700' : 'bg-green-100 text-green-700'"
          >
            {{ zone.isAdminDefined ? 'Admin' : 'Gestionnaire' }}
          </span>
        </td>
        <td class="px-4 py-4 space-x-2">
          <!-- Modifier : admin toujours, gestionnaire uniquement ses zones -->
          <BaseButton
            v-if="can('zone.update') && canEditZone(zone)"
            variant="secondary"
            @click="openEdit(zone)"
          >
            Modifier
          </BaseButton>
          <!-- Affecter catégorie : zone sans catégorie Admin définie -->
          <BaseButton
            v-if="can('zone.update') && canAssignCategory(zone)"
            variant="secondary"
            @click="openAssign(zone)"
          >
            Affecter catégorie
          </BaseButton>
        </td>
      </tr>
    </BaseTable>

    <EmptyState
      v-if="!zones.length && !loading"
      title="Aucune zone"
      description="Aucune zone n'est encore définie pour cet entrepôt."
    />

    <!-- Modal création zone -->
    <BaseModal
      v-if="showCreateForm"
      title="Nouvelle zone"
      :open="showCreateForm"
      @close="showCreateForm = false"
    >
      <form @submit.prevent="submitCreate" class="space-y-4">
        <BaseSelect
          label="Type de zone *"
          v-model="createForm.zoneType"
          :options="zoneTypeOptions"
          :error="createErrors.zoneType"
        />
        <BaseInput
          :label="isManager ? 'Capacité (m³) *' : 'Capacité (m³)'"
          v-model.number="createForm.capacity"
          type="number"
          :placeholder="isManager ? 'Obligatoire' : 'Optionnel'"
          :error="createErrors.capacity"
        />
        <BaseSelect
          label="Catégorie (optionnel)"
          v-model="createForm.categoryId"
          :options="availableCategoryOptions"
          optional
          optionalLabel="— Aucune catégorie —"
        />
        <p v-if="allCategories.length === 0" class="text-xs text-amber-600">
          Aucune catégorie affectée à cet entrepôt. Affectez-en depuis l'onglet Catégories d'abord.
        </p>
        <div class="flex justify-end gap-3 pt-2">
          <BaseButton type="button" variant="secondary" @click="showCreateForm = false">Annuler</BaseButton>
          <BaseButton type="submit" :disabled="saving">
            {{ saving ? 'Création…' : 'Créer la zone' }}
          </BaseButton>
        </div>
      </form>
    </BaseModal>

    <!-- Modal affectation catégorie -->
    <BaseModal
      v-if="assignTarget"
      :title="`Affecter une catégorie à ${assignTarget.name}`"
      :open="!!assignTarget"
      @close="assignTarget = null"
    >
      <form @submit.prevent="submitAssign" class="space-y-4">
        <BaseSelect
          label="Catégorie *"
          v-model="assignCategoryId"
          :options="availableCategoryOptions"
          :error="assignError"
        />
        <p v-if="allCategories.length === 0" class="text-xs text-amber-600">
          Aucune catégorie n'est affectée à cet entrepôt. Allez d'abord dans l'onglet Catégories pour en affecter.
        </p>
        <div class="flex justify-end gap-3 pt-2">
          <BaseButton type="button" variant="secondary" @click="assignTarget = null">Annuler</BaseButton>
          <BaseButton type="submit" :disabled="saving">
            {{ saving ? 'Enregistrement…' : 'Affecter' }}
          </BaseButton>
        </div>
      </form>
    </BaseModal>

    <!-- Modal édition zone -->
    <BaseModal
      v-if="editTarget"
      :title="`Modifier ${editTarget.name}`"
      :open="!!editTarget"
      @close="editTarget = null"
    >
      <form @submit.prevent="submitEdit" class="space-y-4">
        <BaseSelect
          label="Type de zone *"
          v-model="editForm.zoneType"
          :options="zoneTypeOptions"
          :error="editErrors.zoneType"
        />
        <BaseInput
          :label="isManager ? 'Capacité (m³) *' : 'Capacité (m³)'"
          v-model.number="editForm.capacity"
          type="number"
          :placeholder="isManager ? 'Obligatoire' : 'Optionnel'"
          :error="editErrors.capacity"
        />
        <div class="flex justify-end gap-3 pt-2">
          <BaseButton type="button" variant="secondary" @click="editTarget = null">Annuler</BaseButton>
          <BaseButton type="submit" :disabled="saving">
            {{ saving ? 'Enregistrement…' : 'Enregistrer' }}
          </BaseButton>
        </div>
      </form>
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePermissions } from '@/composables/usePermissions'
import { zoneService } from '@/services/zone.service'
import { categoryService } from '@/services/category.service'
import { useToastStore } from '@/stores/toast'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseSelect from '@/components/ui/BaseSelect.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import BaseModal from '@/components/ui/BaseModal.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import type { ZoneResponse } from '@/types/zone.types'
import type { CategoryResponse } from '@/types/category.types'

const props = defineProps<{ warehouseId: number }>()

const { can } = usePermissions()
const authStore = useAuthStore()
const toastStore = useToastStore()
const currentUser = computed(() => authStore.currentUser)
const isManager = computed(() => currentUser.value?.role === "Gestionnaire d'entrepôt")

// ── État ───────────────────────────────────────────────────────────
const zones             = ref<ZoneResponse[]>([])
const allCategories     = ref<CategoryResponse[]>([])
const coveredCategoryIds = ref<number[]>([])   // catégories déjà couvertes par une zone Admin
const loading     = ref(false)
const saving      = ref(false)

const columns = [
  { key: 'name',     label: 'Zone' },
  { key: 'type',     label: 'Type' },
  { key: 'capacity', label: 'Capacité' },
  { key: 'category', label: 'Catégorie' },
  { key: 'creator',  label: 'Défini par' },
  { key: 'actions',  label: 'Actions' },
]

const zoneTypeOptions = [
  { label: 'Réception',  value: 'RECEPTION' },
  { label: 'Stockage',   value: 'STORAGE' },
  { label: 'Expédition', value: 'SHIPPING' },
]

const categoryOptions = computed(() =>
  allCategories.value.map((c) => ({ label: c.name, value: c.id })),
)

/**
 * Pour le gestionnaire : catégories non encore couvertes par une zone Admin.
 * Pour l'admin : toutes les catégories de l'entrepôt.
 */
const availableCategoryOptions = computed(() => {
  if (currentUser.value?.role === 'Administrateur') {
    return allCategories.value.map((c) => ({ label: c.name, value: c.id }))
  }
  // Gestionnaire : exclure les catégories déjà couvertes par l'Admin
  return allCategories.value
    .filter((c) => !coveredCategoryIds.value.includes(c.id))
    .map((c) => ({ label: c.name, value: c.id }))
})

/** Catégories non couvertes disponibles pour le gestionnaire. */
const uncoveredCategories = computed(() =>
  allCategories.value.filter((c) => !coveredCategoryIds.value.includes(c.id)),
)

// Vrai si toutes les zones ont été créées par l'Admin
const adminHasDefinedZones = computed(() =>
  zones.value.length > 0 && zones.value.every((z) => z.isAdminDefined),
)

/**
 * Le gestionnaire peut créer une zone si :
 * - Aucune zone Admin n'existe (setup entièrement libre)
 * - OU il existe des catégories non encore couvertes par l'Admin
 *
 * L'Admin peut toujours créer.
 */
const canCreateZone = computed(() => {
  if (currentUser.value?.role === 'Administrateur') return true
  // Pas de zones Admin → libre
  if (!adminHasDefinedZones.value) return true
  // Des catégories non couvertes existent → le gestionnaire peut compléter
  return uncoveredCategories.value.length > 0
})

// Un gestionnaire peut affecter une catégorie uniquement si :
// - la zone n'a pas de catégorie Admin définie
const canAssignCategory = (zone: ZoneResponse) => {
  if (currentUser.value?.role === 'Administrateur') return true
  return !(zone.isAdminDefined && zone.categoryId !== null)
}

// ── Création ───────────────────────────────────────────────────────
const showCreateForm = ref(false)
const createForm = reactive({ zoneType: '', capacity: undefined as number | undefined, categoryId: undefined as number | undefined })
const createErrors = reactive({ zoneType: '', capacity: '' })

const openCreate = () => {
  createForm.zoneType = ''
  createForm.capacity = undefined
  createForm.categoryId = undefined
  createErrors.zoneType = ''
  createErrors.capacity = ''
  showCreateForm.value = true
}

const submitCreate = async () => {
  createErrors.zoneType = createForm.zoneType ? '' : 'Le type de zone est obligatoire'
  // Capacité obligatoire pour le gestionnaire
  if (isManager.value) {
    createErrors.capacity = (createForm.capacity && createForm.capacity > 0)
      ? ''
      : 'La capacité est obligatoire et doit être supérieure à 0'
  } else {
    createErrors.capacity = ''
  }
  if (createErrors.zoneType || createErrors.capacity) return

  saving.value = true
  try {
    await zoneService.create(props.warehouseId, {
      zoneType: createForm.zoneType,
      capacity: createForm.capacity,
      categoryId: createForm.categoryId || undefined,
    })
    toastStore.push({ title: 'Succès', message: 'Zone créée', variant: 'success' })
    showCreateForm.value = false
    await Promise.all([loadZones(), loadCategories()])
  } catch (err: any) {
    const msg = err?.response?.data?.message ?? 'Une erreur est survenue'
    toastStore.push({ title: 'Erreur', message: msg, variant: 'error' })
  } finally {
    saving.value = false
  }
}

// ── Affectation catégorie ──────────────────────────────────────────
const assignTarget     = ref<ZoneResponse | null>(null)
const assignCategoryId = ref<number | undefined>(undefined)
const assignError      = ref('')

const openAssign = (zone: ZoneResponse) => {
  assignTarget.value = zone
  assignCategoryId.value = zone.categoryId ?? undefined
  assignError.value = ''
}

const submitAssign = async () => {
  if (!assignCategoryId.value) {
    assignError.value = 'Veuillez sélectionner une catégorie'
    return
  }
  if (!assignTarget.value) return

  saving.value = true
  try {
    await zoneService.assignCategory(props.warehouseId, assignTarget.value.id, {
      categoryId: Number(assignCategoryId.value),
    })
    toastStore.push({ title: 'Succès', message: 'Catégorie affectée à la zone', variant: 'success' })
    assignTarget.value = null
    await Promise.all([loadZones(), loadCategories()])
  } catch (err: any) {
    const msg = err?.response?.data?.message ?? 'Une erreur est survenue'
    toastStore.push({ title: 'Erreur', message: msg, variant: 'error' })
  } finally {
    saving.value = false
  }
}

// ── Édition ────────────────────────────────────────────────────────
const editTarget  = ref<ZoneResponse | null>(null)
const editForm    = reactive({ zoneType: '', capacity: undefined as number | undefined })
const editErrors  = reactive({ zoneType: '', capacity: '' })

/** Une zone est modifiable si : admin (toujours) ou gestionnaire (sa propre zone uniquement). */
const canEditZone = (zone: ZoneResponse) => {
  if (currentUser.value?.role === 'Administrateur') return true
  return !zone.isAdminDefined
}

const openEdit = (zone: ZoneResponse) => {
  editTarget.value = zone
  editForm.zoneType = zone.zoneType ?? ''
  editForm.capacity = zone.capacity ?? undefined
  editErrors.zoneType = ''
  editErrors.capacity = ''
}

const submitEdit = async () => {
  editErrors.zoneType = editForm.zoneType ? '' : 'Le type de zone est obligatoire'
  if (isManager.value) {
    editErrors.capacity = (editForm.capacity && editForm.capacity > 0)
      ? ''
      : 'La capacité est obligatoire et doit être supérieure à 0'
  } else {
    editErrors.capacity = ''
  }
  if (editErrors.zoneType || editErrors.capacity || !editTarget.value) return

  saving.value = true
  try {
    await zoneService.update(props.warehouseId, editTarget.value.id, {
      zoneType: editForm.zoneType || undefined,
      capacity: editForm.capacity,
    })
    toastStore.push({ title: 'Succès', message: 'Zone modifiée', variant: 'success' })
    editTarget.value = null
    await loadZones()
  } catch (err: any) {
    toastStore.push({ title: 'Erreur', message: err?.response?.data?.message ?? 'Erreur', variant: 'error' })
  } finally {
    saving.value = false
  }
}

// ── Chargement ─────────────────────────────────────────────────────
const loadZones = async () => {
  loading.value = true
  try {
    const res = await zoneService.list(props.warehouseId, { size: 100 })
    zones.value = res.content
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const [catRes, coveredIds] = await Promise.all([
      categoryService.listByWarehouse(props.warehouseId, { size: 100 }),
      zoneService.coveredCategoryIds(props.warehouseId),
    ])
    allCategories.value = catRes.content
    coveredCategoryIds.value = coveredIds
  } catch {
    // pas bloquant
  }
}

onMounted(async () => {
  await Promise.all([loadZones(), loadCategories()])
})
</script>
