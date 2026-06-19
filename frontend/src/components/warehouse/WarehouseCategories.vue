<template>
  <div>
    <div class="mb-4 flex items-center justify-between gap-4">
      <BaseInput v-model="search" placeholder="Rechercher…" class="max-w-xs" />
      <div class="flex gap-2">
        <!-- Admin : affecter une catégorie existante -->
        <BaseButton v-if="isAdmin && can('category.create')" variant="secondary" @click="openAssign">
          Affecter une catégorie
        </BaseButton>
        <!-- Gestionnaire : créer directement si aucune Admin définie -->
        <BaseButton v-if="isManager && can('category.create') && !adminHasDefinedAll" @click="openCreate">
          Nouvelle catégorie
        </BaseButton>
      </div>
    </div>

    <!-- Bannière : Admin a tout défini, gestionnaire en lecture seule -->
    <div
      v-if="isManager && adminHasDefinedAll"
      class="mb-4 rounded-lg bg-blue-50 border border-blue-200 px-4 py-3 text-sm text-blue-700"
    >
      Les catégories de cet entrepôt ont été définies par l'Administrateur. Consultation uniquement.
    </div>

    <BaseTable :columns="columns">
      <tr v-for="cat in categories" :key="cat.id" class="border-t border-border hover:bg-primary-light/30">
        <td class="px-4 py-4 font-medium text-text-main">{{ cat.name }}</td>
        <td class="px-4 py-4 text-text-secondary">{{ cat.description ?? '—' }}</td>
        <td class="px-4 py-4">
          <span
            class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
            :class="cat.productCount > 0 ? 'bg-primary/10 text-primary' : 'bg-gray-100 text-gray-500'"
          >
            {{ cat.productCount }} produit{{ cat.productCount !== 1 ? 's' : '' }}
          </span>
        </td>
        <td class="px-4 py-4">
          <span
            class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
            :class="cat.isAdminDefined ? 'bg-purple-100 text-purple-700' : 'bg-green-100 text-green-700'"
          >
            {{ cat.isAdminDefined ? 'Admin' : 'Gestionnaire' }}
          </span>
        </td>
        <td class="px-4 py-4 space-x-2">
          <BaseButton v-if="can('category.update') && canEdit(cat)" variant="secondary" @click="openEdit(cat)">Modifier</BaseButton>
          <!-- Admin : désaffecter (retire du warehouse, catégorie reste en base) -->
          <BaseButton
            v-if="isAdmin && cat.isAdminDefined"
            variant="danger"
            :disabled="cat.productCount > 0"
            :title="cat.productCount > 0 ? 'Impossible : des produits sont rattachés' : 'Retirer cette catégorie de l\'entrepôt'"
            @click="openUnassign(cat)"
          >
            Désaffecter
          </BaseButton>
          <!-- Gestionnaire : supprimer ses propres catégories -->
          <BaseButton
            v-if="isManager && !cat.isAdminDefined && can('category.delete')"
            variant="danger"
            :disabled="cat.productCount > 0"
            :title="cat.productCount > 0 ? 'Impossible : des produits sont rattachés' : ''"
            @click="openDelete(cat)"
          >
            Supprimer
          </BaseButton>
        </td>
      </tr>
    </BaseTable>

    <EmptyState v-if="!categories.length && !loading" title="Aucune catégorie" description="Aucune catégorie n'est encore définie pour cet entrepôt." />

    <div v-if="totalPages > 1" class="mt-4 flex justify-center gap-2">
      <BaseButton variant="secondary" :disabled="currentPage === 0" @click="currentPage--">Précédent</BaseButton>
      <span class="self-center text-sm text-text-secondary">Page {{ currentPage + 1 }} / {{ totalPages }}</span>
      <BaseButton variant="secondary" :disabled="currentPage >= totalPages - 1" @click="currentPage++">Suivant</BaseButton>
    </div>

    <!-- Modal affecter (Admin) : liste des catégories non affectées -->
    <BaseModal v-if="showAssign" title="Affecter une catégorie à cet entrepôt" @close="showAssign = false">
      <div class="space-y-4">
        <BaseInput v-model="assignSearch" placeholder="Rechercher une catégorie…" />
        <div v-if="unassignedCategories.length === 0" class="text-sm text-text-secondary py-4 text-center">
          Aucune catégorie disponible. Créez-en depuis le menu Catégories.
        </div>
        <div v-else class="max-h-64 overflow-y-auto space-y-1">
          <label
            v-for="cat in filteredUnassigned"
            :key="cat.id"
            class="flex items-center gap-3 rounded-lg px-3 py-2 cursor-pointer hover:bg-gray-50"
            :class="selectedCategoryIds.has(cat.id) ? 'bg-primary/5' : ''"
          >
            <input
              type="checkbox"
              :value="cat.id"
              :checked="selectedCategoryIds.has(cat.id)"
              @change="toggleSelect(cat.id)"
              class="rounded border-gray-300"
            />
            <div>
              <div class="text-sm font-medium text-text-main">{{ cat.name }}</div>
              <div v-if="cat.description" class="text-xs text-text-secondary">{{ cat.description }}</div>
            </div>
          </label>
        </div>
        <div class="flex justify-end gap-3 pt-2 border-t">
          <BaseButton type="button" variant="secondary" @click="showAssign = false">Annuler</BaseButton>
          <BaseButton :disabled="selectedCategoryIds.size === 0 || saving" @click="submitAssign">
            {{ saving ? 'Affectation…' : `Affecter (${selectedCategoryIds.size})` }}
          </BaseButton>
        </div>
      </div>
    </BaseModal>

    <!-- Modal création (Gestionnaire) -->
    <BaseModal v-if="showCreate" title="Nouvelle catégorie" @close="showCreate = false">
      <form @submit.prevent="submitCreate" class="space-y-4">
        <BaseInput label="Nom *" v-model="createForm.name" placeholder="Ex : Informatique" :error="createErrors.name" />
        <BaseInput label="Description" v-model="createForm.description" placeholder="Optionnel" />
        <div class="flex justify-end gap-3 pt-2">
          <BaseButton type="button" variant="secondary" @click="showCreate = false">Annuler</BaseButton>
          <BaseButton type="submit" :disabled="saving">{{ saving ? 'Création…' : 'Créer' }}</BaseButton>
        </div>
      </form>
    </BaseModal>

    <!-- Modal édition -->
    <BaseModal v-if="editTarget" :title="`Modifier « ${editTarget.name} »`" @close="editTarget = null">
      <form @submit.prevent="submitEdit" class="space-y-4">
        <BaseInput label="Nom *" v-model="editForm.name" :error="editErrors.name" />
        <BaseInput label="Description" v-model="editForm.description" />
        <div class="flex justify-end gap-3 pt-2">
          <BaseButton type="button" variant="secondary" @click="editTarget = null">Annuler</BaseButton>
          <BaseButton type="submit" :disabled="saving">{{ saving ? 'Enregistrement…' : 'Modifier' }}</BaseButton>
        </div>
      </form>
    </BaseModal>

    <ConfirmationDialog
      v-if="deleteTarget"
      :title="`Supprimer « ${deleteTarget.name} »`"
      message="Cette action est irréversible."
      confirmButton="Supprimer"
      cancelButton="Annuler"
      @confirm="confirmDelete"
      @cancel="deleteTarget = null"
    />

    <!-- Confirmation désaffectation (Admin) -->
    <ConfirmationDialog
      v-if="unassignTarget"
      :title="`Désaffecter « ${unassignTarget.name} »`"
      message="La catégorie sera retirée de cet entrepôt mais restera disponible pour une réaffectation ultérieure."
      confirmButton="Désaffecter"
      cancelButton="Annuler"
      @confirm="confirmUnassign"
      @cancel="unassignTarget = null"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePermissions } from '@/composables/usePermissions'
import { categoryService } from '@/services/category.service'
import { useToastStore } from '@/stores/toast'
import { useCategoryBus } from '@/stores/categoryBus'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import BaseModal from '@/components/ui/BaseModal.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ConfirmationDialog from '@/components/ui/ConfirmationDialog.vue'
import type { CategoryResponse } from '@/types/category.types'

const props = defineProps<{ warehouseId: number }>()

const { can } = usePermissions()
const authStore = useAuthStore()
const toastStore = useToastStore()
const categoryBus = useCategoryBus()

const currentUser = computed(() => authStore.currentUser)
const isAdmin     = computed(() => currentUser.value?.role === 'Administrateur')
const isManager   = computed(() => currentUser.value?.role === "Gestionnaire d'entrepôt")

// ── État liste ─────────────────────────────────────────────────────
const categories  = ref<CategoryResponse[]>([])
const loading     = ref(false)
const saving      = ref(false)
const search      = ref('')
const currentPage = ref(0)
const totalPages  = ref(0)

const columns = [
  { key: 'name',         label: 'Nom' },
  { key: 'description',  label: 'Description' },
  { key: 'productCount', label: 'Produits' },
  { key: 'creator',      label: 'Défini par' },
  { key: 'actions',      label: 'Actions' },
]

const adminHasDefinedAll = computed(() =>
  categories.value.length > 0 && categories.value.every((c) => c.isAdminDefined),
)

const canEdit = (cat: CategoryResponse) => isAdmin.value || !cat.isAdminDefined

// ── Affecter (Admin) ───────────────────────────────────────────────
const showAssign           = ref(false)
const assignSearch         = ref('')
const unassignedCategories = ref<CategoryResponse[]>([])
const selectedCategoryIds  = ref(new Set<number>())

const filteredUnassigned = computed(() => {
  const q = assignSearch.value.toLowerCase()
  return q
    ? unassignedCategories.value.filter((c) => c.name.toLowerCase().includes(q))
    : unassignedCategories.value
})

const toggleSelect = (id: number) => {
  if (selectedCategoryIds.value.has(id)) selectedCategoryIds.value.delete(id)
  else selectedCategoryIds.value.add(id)
  selectedCategoryIds.value = new Set(selectedCategoryIds.value)
}

const openAssign = async () => {
  assignSearch.value = ''
  selectedCategoryIds.value = new Set()
  unassignedCategories.value = await categoryService.listUnassigned()
  showAssign.value = true
}

const submitAssign = async () => {
  saving.value = true
  let success = 0
  try {
    for (const id of selectedCategoryIds.value) {
      await categoryService.assignToWarehouse(id, { warehouseId: props.warehouseId })
      success++
    }
    toastStore.push({ title: 'Succès', message: `${success} catégorie(s) affectée(s)`, variant: 'success' })
    showAssign.value = false
    categoryBus.notify()
    await loadCategories()
  } catch (err: any) {
    toastStore.push({ title: 'Erreur', message: err?.response?.data?.message ?? 'Erreur', variant: 'error' })
  } finally {
    saving.value = false
  }
}

// ── Créer (Gestionnaire) ───────────────────────────────────────────
const showCreate  = ref(false)
const createForm  = reactive({ name: '', description: '' })
const createErrors = reactive({ name: '' })

const openCreate = () => {
  createForm.name = ''; createForm.description = ''; createErrors.name = ''
  showCreate.value = true
}

const submitCreate = async () => {
  createErrors.name = createForm.name.trim() ? '' : 'Le nom est obligatoire'
  if (createErrors.name) return
  saving.value = true
  try {
    await categoryService.createInWarehouse(props.warehouseId, {
      name: createForm.name.trim(),
      description: createForm.description.trim() || undefined,
    })
    toastStore.push({ title: 'Succès', message: 'Catégorie créée', variant: 'success' })
    showCreate.value = false
    categoryBus.notify()
    await loadCategories()
  } catch (err: any) {
    toastStore.push({ title: 'Erreur', message: err?.response?.data?.message ?? 'Erreur', variant: 'error' })
  } finally {
    saving.value = false
  }
}

// ── Éditer ────────────────────────────────────────────────────────
const editTarget = ref<CategoryResponse | null>(null)
const editForm   = reactive({ name: '', description: '' })
const editErrors = reactive({ name: '' })

const openEdit = (cat: CategoryResponse) => {
  editTarget.value = cat
  editForm.name = cat.name; editForm.description = cat.description ?? ''; editErrors.name = ''
}

const submitEdit = async () => {
  editErrors.name = editForm.name.trim() ? '' : 'Le nom est obligatoire'
  if (editErrors.name || !editTarget.value) return
  saving.value = true
  try {
    await categoryService.update(editTarget.value.id, {
      name: editForm.name.trim(),
      description: editForm.description.trim() || undefined,
    })
    toastStore.push({ title: 'Succès', message: 'Catégorie modifiée', variant: 'success' })
    editTarget.value = null
    categoryBus.notify()
    await loadCategories()
  } catch (err: any) {
    toastStore.push({ title: 'Erreur', message: err?.response?.data?.message ?? 'Erreur', variant: 'error' })
  } finally {
    saving.value = false
  }
}

// ── Supprimer (Gestionnaire — ses propres catégories uniquement) ──
const deleteTarget = ref<CategoryResponse | null>(null)
const openDelete = (cat: CategoryResponse) => { deleteTarget.value = cat }

const confirmDelete = async () => {
  if (!deleteTarget.value) return
  try {
    await categoryService.delete(deleteTarget.value.id)
    toastStore.push({ title: 'Succès', message: 'Catégorie supprimée', variant: 'success' })
    deleteTarget.value = null
    categoryBus.notify()
    await loadCategories()
  } catch (err: any) {
    toastStore.push({ title: 'Erreur', message: err?.response?.data?.message ?? 'Erreur', variant: 'error' })
    deleteTarget.value = null
  }
}

// ── Désaffecter (Admin — catégories Admin-defined uniquement) ──────
const unassignTarget = ref<CategoryResponse | null>(null)
const openUnassign = (cat: CategoryResponse) => { unassignTarget.value = cat }

const confirmUnassign = async () => {
  if (!unassignTarget.value) return
  try {
    await categoryService.unassignFromWarehouse(props.warehouseId, unassignTarget.value.id)
    toastStore.push({ title: 'Succès', message: 'Catégorie désaffectée — elle est de nouveau disponible', variant: 'success' })
    unassignTarget.value = null
    categoryBus.notify()
    await loadCategories()
  } catch (err: any) {
    toastStore.push({ title: 'Erreur', message: err?.response?.data?.message ?? 'Erreur', variant: 'error' })
    unassignTarget.value = null
  }
}

// ── Chargement ────────────────────────────────────────────────────
const loadCategories = async () => {
  loading.value = true
  try {
    const res = await categoryService.listByWarehouse(props.warehouseId, {
      search: search.value || undefined,
      page: currentPage.value,
      size: 20,
    })
    categories.value = res.content
    totalPages.value  = res.totalPages
  } finally {
    loading.value = false
  }
}

watch(search, () => { currentPage.value = 0; loadCategories() })
watch(currentPage, loadCategories)
onMounted(loadCategories)
</script>
