<template>
  <div>
    <PageHeader
      title="Catégories"
      subtitle="Référentiel"
      description="Créez et gérez les catégories de produits. Affectez-les ensuite aux entrepôts."
    />

    <div class="mb-4 flex items-center justify-between gap-4">
      <BaseInput v-model="search" placeholder="Rechercher une catégorie…" class="max-w-xs" />
      <BaseButton v-if="can('category.create')" @click="openCreate">Nouvelle catégorie</BaseButton>
    </div>

    <BaseCard>
      <BaseTable :columns="columns">
        <tr
          v-for="cat in categories"
          :key="cat.id"
          class="border-t border-border hover:bg-primary-light/30"
        >
          <td class="px-4 py-4 font-medium text-text-main">{{ cat.name }}</td>
          <td class="px-4 py-4 text-text-secondary">{{ cat.description ?? '—' }}</td>
          <td class="px-4 py-4">
            <!-- Badge entrepôt ou "Non affectée" -->
            <span
              v-if="cat.isAssigned"
              class="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-700"
            >
              {{ cat.warehouseName }}
            </span>
            <span
              v-else
              class="inline-flex items-center rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-medium text-amber-700"
            >
              Non affectée
            </span>
          </td>
          <td class="px-4 py-4">
            <span
              class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
              :class="cat.productCount > 0 ? 'bg-primary/10 text-primary' : 'bg-gray-100 text-gray-500'"
            >
              {{ cat.productCount }} produit{{ cat.productCount !== 1 ? 's' : '' }}
            </span>
          </td>
          <td class="px-4 py-4 space-x-2">
            <BaseButton variant="secondary" @click="openEdit(cat)">Modifier</BaseButton>
            <BaseButton
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

      <EmptyState
        v-if="!categories.length && !loading"
        title="Aucune catégorie"
        description="Créez votre première catégorie."
      />
    </BaseCard>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-4 flex justify-center gap-2">
      <BaseButton variant="secondary" :disabled="currentPage === 0" @click="currentPage--">Précédent</BaseButton>
      <span class="self-center text-sm text-text-secondary">Page {{ currentPage + 1 }} / {{ totalPages }}</span>
      <BaseButton variant="secondary" :disabled="currentPage >= totalPages - 1" @click="currentPage++">Suivant</BaseButton>
    </div>

    <!-- Modal création / édition -->
    <BaseModal v-if="showForm" :title="editTarget ? 'Modifier la catégorie' : 'Nouvelle catégorie'" @close="closeForm">
      <form @submit.prevent="submitForm" class="space-y-4">
        <BaseInput label="Nom *" v-model="form.name" placeholder="Ex : Informatique" :error="formErrors.name" />
        <BaseInput label="Description" v-model="form.description" placeholder="Brève description (optionnel)" />
        <div v-if="editTarget?.isAssigned" class="rounded-lg bg-gray-50 px-3 py-2 text-sm text-gray-600">
          Affectée à : <span class="font-medium text-gray-900">{{ editTarget.warehouseName }}</span>
        </div>
        <div class="flex justify-end gap-3 pt-2">
          <BaseButton type="button" variant="secondary" @click="closeForm">Annuler</BaseButton>
          <BaseButton type="submit" :disabled="saving">{{ saving ? 'Enregistrement…' : (editTarget ? 'Modifier' : 'Créer') }}</BaseButton>
        </div>
      </form>
    </BaseModal>

    <!-- Confirmation suppression -->
    <ConfirmationDialog
      v-if="deleteTarget"
      :title="`Supprimer « ${deleteTarget.name} »`"
      message="Cette action est irréversible. Êtes-vous sûr ?"
      confirmButton="Supprimer"
      cancelButton="Annuler"
      @confirm="confirmDelete"
      @cancel="deleteTarget = null"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseTable from '@/components/ui/BaseTable.vue'
import BaseModal from '@/components/ui/BaseModal.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ConfirmationDialog from '@/components/ui/ConfirmationDialog.vue'
import { categoryService } from '@/services/category.service'
import { usePermissions } from '@/composables/usePermissions'
import { useToastStore } from '@/stores/toast'
import { useCategoryBus } from '@/stores/categoryBus'
import type { CategoryResponse } from '@/types/category.types'

const { can } = usePermissions()
const toastStore = useToastStore()
const categoryBus = useCategoryBus()

const categories  = ref<CategoryResponse[]>([])
const loading     = ref(false)
const saving      = ref(false)
const search      = ref('')
const currentPage = ref(0)
const totalPages  = ref(0)

const columns = [
  { key: 'name',         label: 'Nom' },
  { key: 'description',  label: 'Description' },
  { key: 'warehouse',    label: 'Entrepôt' },
  { key: 'productCount', label: 'Produits' },
  { key: 'actions',      label: 'Actions' },
]

const showForm   = ref(false)
const editTarget = ref<CategoryResponse | null>(null)
const form       = reactive({ name: '', description: '' })
const formErrors = reactive({ name: '' })
const deleteTarget = ref<CategoryResponse | null>(null)

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await categoryService.listAll({ search: search.value || undefined, page: currentPage.value, size: 20 })
    categories.value = res.content
    totalPages.value  = res.totalPages
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editTarget.value = null
  form.name = ''; form.description = ''; formErrors.name = ''
  showForm.value = true
}

const openEdit = (cat: CategoryResponse) => {
  editTarget.value = cat
  form.name = cat.name; form.description = cat.description ?? ''; formErrors.name = ''
  showForm.value = true
}

const closeForm = () => { showForm.value = false; editTarget.value = null }

const submitForm = async () => {
  formErrors.name = form.name.trim() ? '' : 'Le nom est obligatoire'
  if (formErrors.name) return
  saving.value = true
  try {
    if (editTarget.value) {
      await categoryService.update(editTarget.value.id, { name: form.name.trim(), description: form.description.trim() || undefined })
      toastStore.push({ title: 'Succès', message: 'Catégorie modifiée', variant: 'success' })
    } else {
      await categoryService.create({ name: form.name.trim(), description: form.description.trim() || undefined })
      toastStore.push({ title: 'Succès', message: 'Catégorie créée. Affectez-la à un entrepôt depuis la liste des entrepôts.', variant: 'success' })
    }
    closeForm()
    categoryBus.notify()
    await loadCategories()
  } catch (err: any) {
    toastStore.push({ title: 'Erreur', message: err?.response?.data?.message ?? 'Erreur', variant: 'error' })
  } finally {
    saving.value = false
  }
}

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

watch(search, () => { currentPage.value = 0; loadCategories() })
watch(currentPage, loadCategories)
// Recharge quand bus notifié (affectation depuis WarehouseDetail)
watch(() => categoryBus.version, loadCategories)
onMounted(loadCategories)
</script>
