<script setup lang="ts">
import { X } from 'lucide-vue-next'

interface Props {
  show: boolean
  title?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
  closeOnBackdrop?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  closeOnBackdrop: true
})

const emit = defineEmits<{ close: [] }>()

// Tailles : min 560px, cohérent avec les formulaires
const sizeClasses: Record<string, string> = {
  sm: 'w-full max-w-[480px]',
  md: 'w-full max-w-[600px]',
  lg: 'w-full max-w-[760px]',
  xl: 'w-full max-w-[960px]',
}

function handleBackdropClick() {
  if (props.closeOnBackdrop) emit('close')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6">
        <!-- Backdrop -->
        <div
          class="absolute inset-0 bg-slate-900/50 backdrop-blur-[2px]"
          @click="handleBackdropClick"
        />

        <!-- Panel -->
        <div
          :class="['relative bg-surface rounded-2xl shadow-2xl flex flex-col', 'max-h-[92vh]', sizeClasses[size]]"
          @click.stop
        >
          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-border-light shrink-0">
            <h2 class="heading-3">{{ title }}</h2>
            <button
              @click="emit('close')"
              class="p-1.5 rounded-lg text-text-muted hover:text-text-main hover:bg-background transition-fast"
            >
              <X class="w-5 h-5" />
            </button>
          </div>

          <!-- Content -->
          <div class="flex-1 overflow-y-auto px-6 py-5">
            <slot />
          </div>

          <!-- Footer -->
          <div v-if="$slots.footer" class="px-6 py-4 border-t border-border-light bg-background/50 shrink-0 rounded-b-2xl">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active { transition: opacity 0.2s ease; }
.modal-leave-active { transition: opacity 0.15s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }

.modal-enter-active .relative { transition: transform 0.2s cubic-bezier(0.34, 1.3, 0.64, 1), opacity 0.2s ease; }
.modal-leave-active .relative  { transition: transform 0.15s ease, opacity 0.15s ease; }
.modal-enter-from .relative { transform: scale(0.96) translateY(12px); opacity: 0; }
.modal-leave-to   .relative { transform: scale(0.98) translateY(6px);  opacity: 0; }
</style>
