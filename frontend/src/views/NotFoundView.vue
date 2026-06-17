<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/dashboard')
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full text-center">
      <h1 class="text-9xl font-bold text-gray-900">404</h1>
      <h2 class="mt-6 text-3xl font-extrabold text-gray-900">Page non trouvée</h2>
      <p class="mt-2 text-gray-600">La page que vous recherchez n'existe pas.</p>

      <div class="mt-6">
        <button
          @click="goHome"
          class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          Retour à l'accueil
        </button>
      </div>
    </div>
  </div>
</template>
