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

      <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

      <div class="flex justify-end gap-3 pt-2">
        <BaseButton type="button" variant="secondary" @click="$emit('close')">Annuler</BaseButton>
        <BaseButton type="submit" :loading="saving">{{ supplier ? 'Enregistrer' : 'Créer' }}</BaseButton>
      </div>
    </form>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { supplierService } from '@/services/supplier.service'
import type { SupplierResponse, CreateSupplierRequest, UpdateSupplierRequest } from '@/types/supplier.types'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import FormField from '@/components/ui/FormField.vue'

const props = defineProps<{ supplier: SupplierResponse | null }>()
const emit = defineEmits<{ close: []; saved: [] }>()

const saving = ref(false)
const error = ref('')

const form = ref({ name: '', contactName: '', phone: '', email: '', city: '', address: '' })

watch(() => props.supplier, (s) => {
  form.value = s
    ? { name: s.name, contactName: s.contactName ?? '', phone: s.phone ?? '', email: s.email ?? '', city: s.city ?? '', address: s.address ?? '' }
    : { name: '', contactName: '', phone: '', email: '', city: '', address: '' }
}, { immediate: true })

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
