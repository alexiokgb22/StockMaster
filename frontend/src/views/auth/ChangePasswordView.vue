<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import authService from '@/services/auth.service'
import { KeyRound, Eye, EyeOff, ShieldCheck } from 'lucide-vue-next'

const router = useRouter()
const auth   = useAuthStore()

const newPassword     = ref('')
const confirmPassword = ref('')
const showNew         = ref(false)
const showConfirm     = ref(false)
const submitting      = ref(false)
const error           = ref('')

const strength = computed(() => {
  const p = newPassword.value
  if (!p) return 0
  let score = 0
  if (p.length >= 8)           score++
  if (/[A-Z]/.test(p))         score++
  if (/[0-9]/.test(p))         score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  return score
})

const strengthLabel = computed(() => ['', 'Faible', 'Moyen', 'Bon', 'Fort'][strength.value])
const strengthColor = computed(() => ['', 'bg-error', 'bg-warning', 'bg-info', 'bg-success'][strength.value])

async function handleSubmit() {
  error.value = ''
  if (newPassword.value.length < 6) {
    error.value = 'Le mot de passe doit contenir au moins 6 caractères.'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    error.value = 'Les mots de passe ne correspondent pas.'
    return
  }
  submitting.value = true
  try {
    await authService.changePassword(newPassword.value)
    // Rafraîchir l'utilisateur pour mettre mustChangePassword à false
    await auth.fetchUser()
    router.push('/dashboard')
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Une erreur est survenue.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-background p-6">
    <div class="w-full max-w-md">

      <!-- Icône + titre -->
      <div class="flex flex-col items-center mb-8 text-center">
        <div class="w-16 h-16 rounded-2xl flex items-center justify-center mb-4 shadow-md"
          style="background: var(--gradient-dark)">
          <KeyRound class="w-8 h-8 text-accent" />
        </div>
        <h1 class="heading-1">Changement de mot de passe</h1>
        <p class="mt-2 text-text-muted max-w-sm">
          Pour des raisons de sécurité, vous devez définir un nouveau mot de passe avant de continuer.
        </p>
      </div>

      <!-- Alerte info -->
      <div class="flex items-start gap-3 p-4 bg-info-light border border-info/20 rounded-xl mb-6">
        <ShieldCheck class="w-5 h-5 text-info shrink-0 mt-0.5" />
        <p class="text-sm text-info-dark">
          Choisissez un mot de passe robuste : au moins 8 caractères, une majuscule, un chiffre et un caractère spécial.
        </p>
      </div>

      <!-- Erreur -->
      <Transition name="slide-down">
        <div v-if="error" class="flex items-start gap-3 p-4 bg-error-light border border-error/20 rounded-xl mb-5">
          <svg class="w-5 h-5 text-error shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <p class="text-sm text-error-dark font-medium">{{ error }}</p>
        </div>
      </Transition>

      <!-- Formulaire -->
      <form @submit.prevent="handleSubmit" class="space-y-5 bg-surface rounded-2xl border border-border-light p-6 shadow-sm">

        <!-- Nouveau mot de passe -->
        <div>
          <label class="text-label block mb-1.5">Nouveau mot de passe <span class="text-error">*</span></label>
          <div class="relative">
            <input
              v-model="newPassword"
              :type="showNew ? 'text' : 'password'"
              placeholder="Minimum 6 caractères"
              autocomplete="new-password"
              class="w-full px-4 py-3 pr-12 rounded-xl border border-border text-sm text-text-main bg-background focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/15 transition-base"
            />
            <button type="button" @click="showNew = !showNew"
              class="absolute right-3.5 top-1/2 -translate-y-1/2 text-text-muted hover:text-primary transition-fast">
              <Eye v-if="!showNew" class="w-4 h-4" />
              <EyeOff v-else class="w-4 h-4" />
            </button>
          </div>

          <!-- Jauge de force -->
          <div v-if="newPassword" class="mt-2 space-y-1">
            <div class="flex gap-1">
              <div v-for="i in 4" :key="i"
                :class="['h-1 flex-1 rounded-full transition-all duration-300', i <= strength ? strengthColor : 'bg-border']" />
            </div>
            <p class="text-xs text-text-muted">Force : <span class="font-semibold">{{ strengthLabel }}</span></p>
          </div>
        </div>

        <!-- Confirmation -->
        <div>
          <label class="text-label block mb-1.5">Confirmer le mot de passe <span class="text-error">*</span></label>
          <div class="relative">
            <input
              v-model="confirmPassword"
              :type="showConfirm ? 'text' : 'password'"
              placeholder="Répétez le mot de passe"
              autocomplete="new-password"
              :class="[
                'w-full px-4 py-3 pr-12 rounded-xl border text-sm text-text-main bg-background focus:outline-none focus:ring-2 transition-base',
                confirmPassword && confirmPassword !== newPassword
                  ? 'border-error focus:border-error focus:ring-error/15'
                  : 'border-border focus:border-primary focus:ring-primary/15'
              ]"
            />
            <button type="button" @click="showConfirm = !showConfirm"
              class="absolute right-3.5 top-1/2 -translate-y-1/2 text-text-muted hover:text-primary transition-fast">
              <Eye v-if="!showConfirm" class="w-4 h-4" />
              <EyeOff v-else class="w-4 h-4" />
            </button>
          </div>
          <p v-if="confirmPassword && confirmPassword !== newPassword" class="mt-1 text-xs text-error-dark font-medium">
            Les mots de passe ne correspondent pas
          </p>
        </div>

        <button
          type="submit"
          :disabled="submitting"
          class="w-full py-3 px-4 rounded-xl font-bold text-primary text-base transition-base flex items-center justify-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed"
          style="background: var(--gradient-primary)"
        >
          <svg v-if="submitting" class="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          {{ submitting ? 'Enregistrement...' : 'Définir le nouveau mot de passe' }}
        </button>
      </form>

    </div>
  </div>
</template>

<style scoped>
.slide-down-enter-active, .slide-down-leave-active { transition: all 0.2s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
