<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Eye, EyeOff, BarChart3, Warehouse, Package, ArrowLeftRight } from '@lucide/vue'

const router = useRouter()
const route  = useRoute()
const auth   = useAuthStore()

const username   = ref('')
const password   = ref('')
const showPwd    = ref(false)
const submitting = ref(false)
const error      = ref('')

async function handleLogin() {
  if (!username.value.trim() || !password.value) {
    error.value = 'Veuillez remplir tous les champs.'
    return
  }
  submitting.value = true
  error.value = ''
  const ok = await auth.login({ username: username.value.trim(), password: password.value })
  submitting.value = false
  if (ok) {
    if (auth.mustChangePassword) {
      router.push('/change-password')
    } else {
      router.push((route.query.redirect as string) || '/dashboard')
    }
  } else {
    error.value = auth.error || 'Identifiants invalides. Veuillez réessayer.'
  }
}

const features = [
  { icon: Warehouse,        label: 'Gestion multi-entrepôts',  desc: 'Pilotez tous vos entrepôts depuis un seul tableau de bord.' },
  { icon: Package,          label: 'Suivi des stocks en temps réel', desc: "Seuils d'alerte, mouvements et historiques détaillés." },
  { icon: ArrowLeftRight,   label: 'Transferts & Inventaires', desc: 'Workflows complets avec traçabilité et validation.' },
]
</script>

<template>
  <div class="min-h-screen flex">

    <!-- ── Panneau gauche — branding ── -->
    <div class="hidden lg:flex lg:w-[55%] flex-col justify-between p-12 relative overflow-hidden"
      style="background: var(--gradient-hero)">

      <!-- Décoration fond -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute -top-32 -right-32 w-96 h-96 rounded-full opacity-10" style="background: #F8D830" />
        <div class="absolute -bottom-40 -left-20 w-80 h-80 rounded-full opacity-10" style="background: #F8D830" />
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] rounded-full opacity-5" style="background: #F8D830" />
      </div>

      <!-- Logo -->
      <div class="relative flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl flex items-center justify-center" style="background: var(--gradient-primary)">
          <BarChart3 class="w-6 h-6 text-primary" />
        </div>
        <div>
          <span class="text-white text-xl font-extrabold">StockMaster</span>
          <span class="block text-xs font-medium text-white/50">WMS Enterprise Platform</span>
        </div>
      </div>

      <!-- Contenu central -->
      <div class="relative space-y-10">
        <div>
          <h1 class="text-4xl font-extrabold text-white leading-tight">
            Maîtrisez votre<br />
            <span style="background: var(--gradient-primary); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;">
              chaîne logistique
            </span>
          </h1>
          <p class="mt-4 text-white/65 text-lg leading-relaxed max-w-md">
            Plateforme WMS de gestion des stocks, entrepôts et opérations logistiques en temps réel.
          </p>
        </div>

        <!-- Features -->
        <ul class="space-y-5">
          <li v-for="f in features" :key="f.label" class="flex items-start gap-4">
            <div class="w-10 h-10 rounded-xl flex items-center justify-center shrink-0" style="background: rgba(248,216,48,0.15); border: 1px solid rgba(248,216,48,0.3)">
              <component :is="f.icon" class="w-5 h-5" style="color: #F8D830" />
            </div>
            <div>
              <p class="font-semibold text-white">{{ f.label }}</p>
              <p class="text-sm text-white/55 mt-0.5">{{ f.desc }}</p>
            </div>
          </li>
        </ul>
      </div>

      <!-- Footer branding -->
      <div class="relative text-white/30 text-xs">
        © {{ new Date().getFullYear() }} StockMaster — v2.0 Enterprise
      </div>
    </div>

    <!-- ── Panneau droit — formulaire ── -->
    <div class="flex-1 flex items-center justify-center bg-background p-6 lg:p-12">
      <div class="w-full max-w-md">

        <!-- Mobile logo -->
        <div class="lg:hidden flex items-center gap-3 mb-8 justify-center">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center" style="background: var(--gradient-dark)">
            <BarChart3 class="w-6 h-6 text-accent" />
          </div>
          <span class="text-primary text-xl font-extrabold">StockMaster</span>
        </div>

        <!-- Titre -->
        <div class="mb-8">
          <h2 class="heading-1">Connexion</h2>
          <p class="mt-2 text-text-muted">Accédez à votre espace de gestion logistique.</p>
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
        <form @submit.prevent="handleLogin" class="space-y-4">

          <!-- Username -->
          <div>
            <label class="text-label block mb-1.5">Nom d'utilisateur</label>
            <input
              v-model="username"
              type="text"
              autocomplete="username"
              placeholder="admin"
              :class="['w-full px-4 py-3 rounded-xl border text-sm text-text-main bg-surface transition-base outline-none',
                'focus:border-primary focus:ring-2 focus:ring-primary/15',
                error ? 'border-error' : 'border-border'
              ]"
            />
          </div>

          <!-- Password -->
          <div>
            <label class="text-label block mb-1.5">Mot de passe</label>
            <div class="relative">
              <input
                v-model="password"
                :type="showPwd ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="••••••••"
                :class="['w-full px-4 py-3 pr-12 rounded-xl border text-sm text-text-main bg-surface transition-base outline-none',
                  'focus:border-primary focus:ring-2 focus:ring-primary/15',
                  error ? 'border-error' : 'border-border'
                ]"
              />
              <button
                type="button"
                @click="showPwd = !showPwd"
                class="absolute right-3.5 top-1/2 -translate-y-1/2 text-text-muted hover:text-primary transition-fast"
              >
                <Eye v-if="!showPwd" class="w-4 h-4" />
                <EyeOff v-else class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- Submit -->
          <button
            type="submit"
            :disabled="submitting"
            class="w-full py-3 px-4 rounded-xl font-bold text-primary text-base transition-base flex items-center justify-center gap-2 mt-2 disabled:opacity-60 disabled:cursor-not-allowed"
            style="background: var(--gradient-primary)"
          >
            <svg v-if="submitting" class="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            {{ submitting ? 'Connexion...' : 'Se connecter' }}
          </button>
        </form>

        <!-- Identifiants de démonstration -->
        <div class="mt-6 p-4 bg-primary-50 border border-primary-200 rounded-xl">
          <p class="text-xs font-bold text-primary uppercase tracking-wider mb-2">Accès démonstration</p>
          <p class="text-sm text-text-secondary">
            Identifiant : <span class="font-bold text-primary font-mono">admin</span>
            &nbsp;·&nbsp;
            Mot de passe : <span class="font-bold text-primary font-mono">admin123</span>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.slide-down-enter-active, .slide-down-leave-active { transition: all 0.2s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
