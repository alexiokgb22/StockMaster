<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const errorMessage = ref('')
const isSubmitting = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    errorMessage.value = 'Veuillez remplir tous les champs'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  const success = await authStore.login({
    username: username.value,
    password: password.value
  })

  isSubmitting.value = false

  if (success) {
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } else {
    errorMessage.value = authStore.error || 'Identifiants invalides'
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Logo et titre -->
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">StockMaster</h2>
        <p class="mt-2 text-center text-sm text-gray-600">Connexion à votre compte</p>
      </div>

      <!-- Formulaire de connexion -->
      <form class="mt-8 space-y-6" @submit.prevent="handleLogin">
        <!-- Message d'erreur -->
        <div v-if="errorMessage" class="rounded-md bg-red-50 p-4">
          <div class="flex">
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800">{{ errorMessage }}</h3>
            </div>
          </div>
        </div>

        <div class="rounded-md shadow-sm -space-y-px">
          <!-- Username -->
          <div>
            <label for="username" class="sr-only">Nom d'utilisateur ou email</label>
            <input
              id="username"
              v-model="username"
              name="username"
              type="text"
              autocomplete="username"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="Nom d'utilisateur ou email"
            />
          </div>

          <!-- Password -->
          <div>
            <label for="password" class="sr-only">Mot de passe</label>
            <input
              id="password"
              v-model="password"
              name="password"
              type="password"
              autocomplete="current-password"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="Mot de passe"
            />
          </div>
        </div>

        <!-- Bouton de connexion -->
        <div>
          <button
            type="submit"
            :disabled="isSubmitting"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="isSubmitting">Connexion en cours...</span>
            <span v-else>Se connecter</span>
          </button>
        </div>

        <!-- Informations de test -->
        <div class="rounded-md bg-blue-50 p-4 mt-4">
          <div class="flex">
            <div class="ml-3">
              <h3 class="text-sm font-medium text-blue-800">Identifiants par défaut</h3>
              <div class="mt-2 text-sm text-blue-700">
                <p>Vous pouvez utiliser :</p>
                <ul class="list-disc list-inside mt-1">
                  <li>Username: <strong>admin</strong></li>
                  <li>Email: <strong>admin@stockmaster.com</strong></li>
                </ul>
                <p class="mt-2">Password: <strong>admin123</strong></p>
              </div>
            </div>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>
