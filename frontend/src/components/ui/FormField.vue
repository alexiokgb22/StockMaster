<script setup lang="ts">
interface Props {
  label?: string
  error?: string
  required?: boolean
  helpText?: string
}

defineProps<Props>()
</script>

<template>
  <div class="space-y-1.5">
    <!-- Label -->
    <label v-if="label" class="block text-label text-text-main">
      {{ label }}
      <span v-if="required" class="text-error ml-0.5">*</span>
    </label>

    <!-- Slot pour input/select/textarea -->
    <slot />

    <!-- Message d'erreur -->
    <Transition name="slide-down">
      <p v-if="error" class="text-xs text-error-dark font-medium flex items-center gap-1.5">
        <svg class="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        {{ error }}
      </p>
    </Transition>

    <!-- Texte d'aide -->
    <p v-if="helpText && !error" class="text-xs text-text-muted">
      {{ helpText }}
    </p>
  </div>
</template>

<style scoped>
.slide-down-enter-active, .slide-down-leave-active {
  transition: all 0.2s ease;
}
.slide-down-enter-from, .slide-down-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
