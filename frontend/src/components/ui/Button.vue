<script setup lang="ts">
interface Props {
  variant?: 'primary' | 'secondary' | 'accent' | 'success' | 'danger' | 'ghost' | 'outline'
  size?: 'xs' | 'sm' | 'md' | 'lg'
  disabled?: boolean
  loading?: boolean
  fullWidth?: boolean
  type?: 'button' | 'submit' | 'reset'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
  loading: false,
  fullWidth: false,
  type: 'button'
})

const emit = defineEmits<{ click: [event: MouseEvent] }>()

const base = 'inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-base focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-accent disabled:opacity-50 disabled:cursor-not-allowed select-none'

const variants: Record<string, string> = {
  primary:   'bg-primary text-white hover:bg-primary-hover active:bg-primary-dark shadow-sm',
  secondary: 'bg-surface text-text-main border border-border hover:bg-surface-hover hover:border-border-strong shadow-sm',
  accent:    'bg-accent text-primary hover:bg-accent-hover active:bg-accent-dark font-bold shadow-md',
  success:   'bg-success text-white hover:bg-success-dark shadow-sm',
  danger:    'bg-error text-white hover:bg-error-dark shadow-sm',
  ghost:     'text-text-main hover:bg-primary-100 hover:text-primary',
  outline:   'border-2 border-primary text-primary hover:bg-primary-100',
}

const sizes: Record<string, string> = {
  xs: 'px-2.5 py-1 text-xs',
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-4 py-2 text-sm',
  lg: 'px-5 py-2.5 text-base',
}

function handleClick(event: MouseEvent) {
  if (!props.disabled && !props.loading) emit('click', event)
}
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="[base, variants[variant], sizes[size], { 'w-full': fullWidth }]"
    @click="handleClick"
  >
    <svg v-if="loading" class="animate-spin h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
    </svg>
    <slot />
  </button>
</template>
