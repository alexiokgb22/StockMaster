<template>
  <AuthLayout>
    <div class="rounded-[32px] border border-border bg-white p-10 shadow-card">
      <div class="mb-8 text-center">
        <h1 class="text-3xl font-semibold text-primary">Connexion</h1>
        <p class="mt-2 text-sm text-text-secondary">Accédez à votre espace StockMaster</p>
      </div>

      <form @submit.prevent="submit" class="space-y-6">
        <BaseInput label="Nom d’utilisateur" v-model="username" placeholder="admin" />
        <BaseInput label="Mot de passe" v-model="password" type="password" placeholder="••••••••" />

        <div class="text-right text-sm text-error" v-if="errorMessage">{{ errorMessage }}</div>

        <BaseButton type="submit" :disabled="loading">{{ loading ? 'Connexion...' : 'Se connecter' }}</BaseButton>
      </form>
    </div>
  </AuthLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AuthLayout from '@/layouts/AuthLayout.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import { useAuth } from '@/composables/useAuth'
import type { LoginRequest } from '@/types/auth.types'

const { signIn, loading, currentUser } = useAuth()
const username = ref('')
const password = ref('')
const errorMessage = ref<string | null>(null)

const router = useRouter()
const route = useRoute()

const submit = async () => {
  errorMessage.value = null

  try {
    await signIn({
      username: username.value,
      password: password.value,
    })

    if (currentUser.value?.mustChangePassword) {
      router.replace({ name: 'ChangePassword' })
      return
    }

    const redirect = route.query.redirect

    const safeRedirect =
      typeof redirect === 'string' && redirect.startsWith('/')
        ? redirect
        : '/'

    router.replace(safeRedirect)
  } catch (error) {
    errorMessage.value = 'Identifiants incorrects ou serveur indisponible.'
  }
}

</script>
