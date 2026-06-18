<template>
  <div>
    <PageHeader
      title="Changer le mot de passe"
      subtitle="Première connexion"
      description="Vous devez définir un nouveau mot de passe avant d'accéder à l'application."
    />

    <BaseCard>
      <form @submit.prevent="submit" class="space-y-6">
        <BaseInput
          label="Nouveau mot de passe"
          v-model="password"
          type="password"
          placeholder="••••••••"
        />
        <BaseInput
          label="Confirmer le mot de passe"
          v-model="confirmPassword"
          type="password"
          placeholder="••••••••"
        />

        <div class="text-sm text-error" v-if="errorMessage">{{ errorMessage }}</div>

        <div class="flex items-center gap-3">
          <BaseButton type="submit" :disabled="loading">
            {{ loading ? 'Enregistrement...' : 'Enregistrer' }}
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseCard from '@/components/ui/BaseCard.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { changePassword } = useAuth()
const password = ref('')
const confirmPassword = ref('')
const errorMessage = ref<string | null>(null)
const loading = ref(false)

const submit = async () => {
  errorMessage.value = null

  if (!password.value || !confirmPassword.value) {
    errorMessage.value = 'Veuillez renseigner les deux champs.'
    return
  }

  if (password.value !== confirmPassword.value) {
    errorMessage.value = 'Les mots de passe ne correspondent pas.'
    return
  }

  loading.value = true
  try {
    await changePassword(password.value)
    router.push({ name: 'Dashboard' })
  } catch (_error) {
    errorMessage.value = 'Impossible de changer le mot de passe. Réessayez.'
  } finally {
    loading.value = false
  }
}
</script>
