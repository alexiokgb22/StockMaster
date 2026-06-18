<template>
  <button
    :type="buttonType"
    :class="buttonClass"
    :disabled="props.disabled"
    @click="$emit('click')"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  type?: 'button' | 'submit' | 'reset'
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  disabled?: boolean
}>()

const buttonType = props.type ?? 'button'
const buttonClass = computed(() => {
  const base = 'inline-flex items-center justify-center gap-2 rounded-2xl px-4 py-2 text-sm font-semibold transition'
  const variants: Record<string, string> = {
    primary: 'bg-primary text-white shadow-card hover:bg-[#0a2d55]',
    secondary: 'bg-surface text-text-main border border-border hover:bg-primary-light',
    danger: 'bg-error text-white hover:bg-[#d9433d]',
    ghost: 'bg-transparent text-text-secondary hover:bg-primary-light',
  }
  return `${base} ${variants[props.variant ?? 'primary']}`
})
</script>
