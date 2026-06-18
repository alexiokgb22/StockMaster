<template>
  <div>
    <PageHeader
      title="Créer un magasinier"
      subtitle="Gestion"
      :description="currentUser?.warehouseName ? `Entrepôt : ${currentUser.warehouseName}` : ''"
    />
    <BaseCard>
      <form @submit.prevent="submit" class="grid gap-6 max-w-md">
        <BaseInput
          label="Nom d'utilisateur"
          v-model="form.username"
          placeholder="jean.dupont"
          required
        />
        <BaseInput
          label="Email"
          v-model="form.email"
          type="email"
          placeholder="jean@exemple.com"
          required
        />
        <BaseInput
          label="Mot de passe provisoire"
          v-model="form.password"
          type="password"
          placeholder="••••••••"
          required
        />

        <!-- Info contextuelle : rôle et entrepôt sont fixés automatiquement -->
        <div class="rounded-lg border border-border bg-surface px-4 py-3 text-sm text-text-secondary">
          Le compte sera créé avec le rôle <strong>Magasinier</strong>
          et affecté à l'entrepôt <strong>{{ currentUser?.warehouseName }}</strong>.
          Le mot de passe devra être changé à la première connexion.
        </div>

        <div v-if="error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
          {{ error }}
        </div>

        <div class="flex items-center gap-3">
          <BaseButton type="submit" :disabled="loading">
            {{ loading ? 'Création…' : 'Créer le magasinier' }}
          </BaseButton>
          <RouterLink to="/users" class="text-sm text-text-secondary hover:text-primary">
            Retour
          </RouterLink>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import { userService } from '@/services/user.service'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { currentUser } = useAuth()
const loading = ref(false)
const error   = ref<string | null>(null)

const form = reactive({
  username: '',
  email: '',
  password: '',
  // roleId et warehouseId sont ignorés par l'endpoint /storekeeper (backend les fixe)
  roleId: 0,
})

const submit = async () => {
  error.value = null
  if (!form.username || !form.email || !form.password) {
    error.value = 'Tous les champs sont obligatoires.'
    return
  }
  loading.value = true
  try {
    await userService.createStorekeeper({ ...form, warehouseId: null })
    router.push({ name: 'Users' })
  } catch (err: unknown) {
    if (err && typeof err === 'object' && 'response' in err) {
      const r = (err as { response?: { data?: { message?: string } } }).response
      error.value = r?.data?.message ?? 'Une erreur est survenue.'
    } else {
      error.value = 'Une erreur est survenue.'
    }
  } finally {
    loading.value = false
  }
}
</script>
