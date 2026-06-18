<template>
  <BaseButton
    :type="props.type"
    :variant="props.variant"
    :disabled="props.disabled || props.loading"
    @click="$emit('click')"
  >
    <template #default>
      <span v-if="props.loading" class="inline-flex items-center gap-2">
        <span class="h-4 w-4 animate-spin rounded-full border-2 border-white/25 border-t-white"></span>
        <span>Chargement...</span>
      </span>
      <span v-else>
        <slot />
      </span>
    </template>
  </BaseButton>
</template>

<script setup lang="ts">
import BaseButton from './BaseButton.vue'

const props = defineProps<{
  type?: 'button' | 'submit' | 'reset'
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  disabled?: boolean
  loading?: boolean
}>()

const emit = defineEmits<{ (event: 'click'): void }>()
</script>
