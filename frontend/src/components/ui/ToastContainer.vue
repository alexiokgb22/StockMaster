<script setup lang="ts">
import { CheckCircle, XCircle, AlertTriangle, Info, X } from '@lucide/vue'
import { useToast } from '@/composables/useToast'

const { toasts, remove } = useToast()

const icons = { success: CheckCircle, error: XCircle, warning: AlertTriangle, info: Info }
const colors: Record<string, string> = {
  success: 'border-l-success bg-success-light',
  error:   'border-l-error bg-error-light',
  warning: 'border-l-warning bg-warning-light',
  info:    'border-l-info bg-info-light',
}
const iconColors: Record<string, string> = {
  success: 'text-success-dark',
  error:   'text-error-dark',
  warning: 'text-warning-dark',
  info:    'text-info-dark',
}
</script>

<template>
  <Teleport to="body">
    <div class="fixed top-5 right-5 z-[--z-toast] flex flex-col gap-2 max-w-sm w-full pointer-events-none">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="['pointer-events-auto flex items-start gap-3 p-4 rounded-xl shadow-xl border border-border-light border-l-4', colors[toast.type]]"
        >
          <component :is="icons[toast.type]" :class="['w-5 h-5 shrink-0 mt-0.5', iconColors[toast.type]]" />
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold text-text-main">{{ toast.title }}</p>
            <p v-if="toast.message" class="text-xs text-text-muted mt-0.5">{{ toast.message }}</p>
          </div>
          <button @click="remove(toast.id)" class="shrink-0 text-text-muted hover:text-text-main transition-fast">
            <X class="w-4 h-4" />
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: all 0.25s ease; }
.toast-enter-from { opacity: 0; transform: translateX(100%); }
.toast-leave-to   { opacity: 0; transform: translateX(100%); }
</style>
