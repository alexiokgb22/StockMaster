<template>
  <BaseModal :title="supplier ? 'Modifier le fournisseur' : 'Nouveau fournisseur'" @close="$emit('close')">
    <form @submit.prevent="handleSubmit" class="space-y-4">

      <div class="grid grid-cols-2 gap-4">
        <FormField label="Nom" required class="col-span-2">
          <BaseInput v-model="form.name" placeholder="Nom du fournisseur" required />
        </FormField>
        <FormField label="Contact principal">
          <BaseInput v-model="form.contactName" placeholder="Nom du contact" />
        </FormField>
        <FormField label="Téléphone">
          <BaseInput v-model="form.phone" placeholder="+33..." />
        </FormField>
        <FormField label="Email">
          <BaseInput v-model="form.email" type="email" placeholder="email@exemple.com" />
        </FormField>
        <FormField label="Ville">
          <BaseInput v-model="form.city" placeholder="Ville" />
        </FormField>
        <FormField label="Adresse" class="col-span-2">
          <BaseInput v-model="form.address" placeholder="Adresse complète" />
        </FormField>
      </div>

      <!-- Entrepôts -->
      <FormField label="Entrepôts affectés">
        <div v-if="loadingWarehouses" class="text-xs text-text-secondary">Chargement…</div>
        <div v-else-if="allWarehouses.length > 0" class="space-y-1 max-h-40 overflow-y-auto rounded-lg border border-border p-2">
          <label
            v-for="w in allWarehouses"
            :key="w.id"
            class="flex items-center gap-2 cursor-pointer rounded px-2 py-1 hover:bg-primary-light/30 text-sm"
          >
            <input type="checkbox" :value="w.id" v-model="form.warehouseIds" class="accent-primary" />
            <span>{{ w.name }}</span>
            <span v-if="w.city" class="text-xs text-text-secondary">— {{ w.city }}</span>
          </label>
        </div>
        <p class="mt-1 text-xs text-text-secondary">Ce fournisseur approvisionne les entrepôts sélectionnés.</p>
      </FormField>

      <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving">{{ supplier ? 'Enregistrer' : 'Créer' }}</BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { supplierService } from '@/services/supplier.service'
import { warehouseService } from '@/services/warehouse.service'
import type { SupplierResponse, CreateSupplierRequest, UpdateSupplierRequest } from '@/types/supplier.types'
import type { WarehouseResponse } from '@/types/warehouse.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ supplier: SupplierResponse | null }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')
const loadingWarehouses = ref(false)
const allWarehouses = ref<WarehouseResponse[]>([])

const form = ref({
  name: '',
  contactName: '',
  phone: '',
  email: '',
  city: '',
  address: '',
  warehouseIds: [] as number[],
})

watch(() => props.supplier, (s) => {
  if (s) {
    form.value = {
      name: s.name,
      contactName: s.contactName ?? '',
      phone: s.phone ?? '',
      email: s.email ?? '',
      city: s.city ?? '',
      address: s.address ?? '',
      warehouseIds: [...s.warehouseIds],
    }
  } else {
    form.value = { name: '', contactName: '', phone: '', email: '', city: '', address: '', warehouseIds: [] }
  }
}, { immediate: true })

onMounted(async () => {
  loadingWarehouses.value = true
  try {
    const res = await warehouseService.list({ size: 200, active: true })
    allWarehouses.value = res.content
  } finally {
    loadingWarehouses.value = false
  }
})

async function handleSubmit() {
  error.value = ''
  saving.value = true
  try {
    if (props.supplier) {
      const payload: UpdateSupplierRequest = {
        name: form.value.name,
        contactName: form.value.contactName || undefined,
        phone: form.value.phone || undefined,
        email: form.value.email || undefined,
        city: form.value.city || undefined,
        address: form.value.address || undefined,
        warehouseIds: form.value.warehouseIds,
      }
      await supplierService.update(props.supplier.id, payload)
    } else {
      const payload: CreateSupplierRequest = {
        name: form.value.name,
        contactName: form.value.contactName || undefined,
        phone: form.value.phone || undefined,
        email: form.value.email || undefined,
        city: form.value.city || undefined,
        address: form.value.address || undefined,
        warehouseIds: form.value.warehouseIds.length > 0 ? form.value.warehouseIds : undefined,
      }
      await supplierService.create(payload)
    }
    emit('saved')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Une erreur est survenue'
  } finally {
    saving.value = false
  }
}
</script>
