<template>
  <div class="min-h-screen flex">

    <!-- ── Colonne gauche — branding ─────────────────────────── -->
    <div class="hidden lg:flex lg:w-1/2 relative overflow-hidden bg-primary flex-col justify-between p-12">

      <!-- Fond décoratif — cercles géométriques -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute -top-32 -left-32 w-[500px] h-[500px] rounded-full bg-white/5" />
        <div class="absolute top-1/3 -right-24 w-[350px] h-[350px] rounded-full bg-accent/20" />
        <div class="absolute -bottom-20 left-1/4 w-[300px] h-[300px] rounded-full bg-white/5" />
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] rounded-full border border-white/10" />
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[400px] h-[400px] rounded-full border border-white/10" />
      </div>

      <!-- Contenu haut — logo -->
      <div class="relative z-10">
        <div class="flex items-center gap-3">
          <!-- Icône entrepôt -->
          <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-accent">
            <svg class="h-6 w-6 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
          </div>
          <span class="text-xl font-bold text-white tracking-tight">StockMaster</span>
        </div>
      </div>

      <!-- Contenu centre — pitch -->
      <div class="relative z-10 space-y-6">
        <h2 class="text-4xl font-bold text-white leading-tight">
          Pilotez vos entrepôts<br />
          <span class="text-accent">en toute clarté.</span>
        </h2>
        <p class="text-white/70 text-base leading-relaxed max-w-sm">
          Stocks, réceptions, transferts, inventaires — tout ce dont vous avez besoin
          pour gérer votre logistique au quotidien.
        </p>

        <!-- Features -->
        <ul class="space-y-3">
          <li
            v-for="feature in features"
            :key="feature"
            class="flex items-center gap-3 text-white/80 text-sm"
          >
            <span class="flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full bg-accent/30 text-accent text-xs font-bold">
              ✓
            </span>
            {{ feature }}
          </li>
        </ul>
      </div>

      <!-- Contenu bas — mention -->
      <div class="relative z-10 text-white/40 text-xs">
        © {{ new Date().getFullYear() }} StockMaster · ERP WMS Foundation
      </div>
    </div>

    <!-- ── Colonne droite — formulaire ───────────────────────── -->
    <div class="flex w-full lg:w-1/2 flex-col items-center justify-center bg-background px-6 py-12 sm:px-12">

      <!-- Logo mobile (visible seulement sur petits écrans) -->
      <div class="mb-8 flex items-center gap-2 lg:hidden">
        <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
          <svg class="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
          </svg>
        </div>
        <span class="text-lg font-bold text-primary">StockMaster</span>
      </div>

      <div class="w-full max-w-md">

        <!-- En-tête formulaire -->
        <div class="mb-10">
          <h1 class="text-3xl font-bold text-text-main">Bon retour 👋</h1>
          <p class="mt-2 text-sm text-text-secondary">
            Connectez-vous pour accéder à votre espace de gestion.
          </p>
        </div>

        <!-- Formulaire -->
        <form @submit.prevent="submit" class="space-y-5">

          <!-- Champ username -->
          <div class="space-y-1.5">
            <label class="block text-sm font-medium text-text-main">
              Nom d'utilisateur
            </label>
            <div class="relative">
              <span class="pointer-events-none absolute inset-y-0 left-4 flex items-center text-text-secondary">
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </span>
              <input
                v-model="username"
                type="text"
                placeholder="Votre identifiant"
                autocomplete="username"
                :disabled="loading"
                class="w-full rounded-2xl border border-border bg-white py-3 pl-11 pr-4 text-sm text-text-main outline-none transition placeholder:text-text-secondary/60 focus:border-primary focus:ring-2 focus:ring-primary/15 disabled:opacity-50"
              />
            </div>
          </div>

          <!-- Champ mot de passe -->
          <div class="space-y-1.5">
            <label class="block text-sm font-medium text-text-main">
              Mot de passe
            </label>
            <div class="relative">
              <span class="pointer-events-none absolute inset-y-0 left-4 flex items-center text-text-secondary">
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </span>
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••"
                autocomplete="current-password"
                :disabled="loading"
                class="w-full rounded-2xl border border-border bg-white py-3 pl-11 pr-12 text-sm text-text-main outline-none transition placeholder:text-text-secondary/60 focus:border-primary focus:ring-2 focus:ring-primary/15 disabled:opacity-50"
              />
              <!-- Toggle visibilité -->
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-4 flex items-center text-text-secondary transition hover:text-primary"
                tabindex="-1"
              >
                <svg v-if="!showPassword" class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 4.411m0 0L21 21" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Message d'erreur -->
          <div
            v-if="errorMessage"
            class="flex items-center gap-2 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600"
          >
            <svg class="h-4 w-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            {{ errorMessage }}
          </div>

          <!-- Bouton submit -->
          <button
            type="submit"
            :disabled="loading || !username || !password"
            class="group relative w-full overflow-hidden rounded-2xl bg-primary py-3.5 text-sm font-semibold text-white shadow-md transition hover:bg-[#0a2d55] disabled:cursor-not-allowed disabled:opacity-60"
          >
            <span v-if="!loading" class="flex items-center justify-center gap-2">
              Se connecter
              <svg class="h-4 w-4 transition group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </span>
            <span v-else class="flex items-center justify-center gap-2">
              <svg class="h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
              Connexion en cours…
            </span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const { signIn, loading, currentUser } = useAuth()

const username     = ref('')
const password     = ref('')
const showPassword = ref(false)
const errorMessage = ref<string | null>(null)

const router = useRouter()
const route  = useRoute()

const features = [
  'Gestion multi-entrepôts en temps réel',
  'Traçabilité complète des mouvements',
  'Alertes automatiques sur les seuils de stock',
  'Tableaux de bord et rapports intégrés',
]

const submit = async () => {
  errorMessage.value = null
  try {
    await signIn({ username: username.value, password: password.value })

    if (currentUser.value?.mustChangePassword) {
      router.replace({ name: 'ChangePassword' })
      return
    }

    const redirect = route.query.redirect
    const safeRedirect =
      typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/'
    router.replace(safeRedirect)
  } catch {
    errorMessage.value = 'Identifiants incorrects ou serveur indisponible.'
  }
}
</script>
