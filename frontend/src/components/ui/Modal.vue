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

const emit = defineEmits<{
  close: []
}>()

const sizeClasses = {
  sm: 'max-w-md',
  md: 'max-w-lg',
  lg: 'max-w-2xl',
  xl: 'max-w-4xl'
}

function handleBackdropClick() {
  if (props.closeOnBackdrop) {
    emit('close')
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div 
          class="absolute inset-0 bg-primary/60 backdrop-blur-sm"
          @click="handleBackdropClick"
        />

        <!-- Modal -->
        <div 
          :class="[
            'relative bg-surface rounded-2xl shadow-2xl w-full',
            'max-h-[90vh] flex flex-col',
            sizeClasses[size]
          ]"
          @click.stop
        >
          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-border-light shrink-0">
            <h2 class="heading-3">{{ title }}</h2>
            <button 
              @click="emit('close')"
              class="p-2 rounded-lg text-text-muted hover:text-primary hover:bg-primary-50 transition-fast"
            >
              <X class="w-5 h-5" />
            </button>
          </div>

          <!-- Content -->
          <div class="flex-1 overflow-y-auto px-6 py-6">
            <slot />
          </div>

          <!-- Footer (optionnel) -->
          <div v-if="$slots.footer" class="px-6 py-4 border-t border-border-light bg-background shrink-0">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.25s ease;
}
.modal-enter-from, .modal-leave-to {
  opacity: 0;
}

.modal-enter-active .relative,
.modal-leave-active .relative {
  transition: all 0.25s ease;
}
.modal-enter-from .relative,
.modal-leave-to .relative {
  transform: scale(0.95) translateY(20px);
  opacity: 0;
}
</style>
