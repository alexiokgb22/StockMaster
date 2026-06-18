<template>
  <div>
    <slot v-if="!hasError" />
    <div v-else class="rounded-[32px] border border-border bg-white p-10 text-center shadow-card">
      <div class="text-2xl font-semibold text-error">Erreur inattendue</div>
      <p class="mt-4 text-sm text-text-secondary">Une erreur s’est produite lors du rendu de cette section.</p>
      <button @click="reset" class="mt-6 rounded-2xl bg-primary px-5 py-3 text-sm font-semibold text-white hover:bg-[#0a2d55]">Réessayer</button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'

export default defineComponent({
  name: 'ErrorBoundary',
  setup() {
    const hasError = ref(false)

    const reset = () => {
      hasError.value = false
    }

    return {
      hasError,
      reset,
    }
  },
  errorCaptured(err, instance, info) {
    console.error('ErrorBoundary captured:', err, info)
    this.hasError = true
    return false
  },
})
</script>
