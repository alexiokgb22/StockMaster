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

const base = 'inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-fast focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-accent disabled:opacity-50 disabled:cursor-not-allowed select-none whitespace-nowrap'

const variants: Record<string, string> = {
  primary:   'bg-primary text-white hover:bg-primary-hover active:bg-primary-dark shadow-sm',
  secondary: 'bg-surface text-text-main border border-border hover:bg-background hover:border-border-strong shadow-sm',
  accent:    'text-primary font-bold shadow-sm',
  success:   'bg-success text-white hover:bg-success-dark shadow-sm',
  danger:    'bg-error text-white hover:bg-error-dark shadow-sm',
  ghost:     'text-text-secondary hover:bg-primary-100 hover:text-primary',
  outline:   'border border-primary text-primary hover:bg-primary-100',
}

const accentStyle = 'background: linear-gradient(135deg, #F8D830 0%, #F8B830 100%)'

// Tailles normalisées : hauteur 32/36/40/44px
const sizes: Record<string, string> = {
  xs: 'h-8  px-3   text-xs',
  sm: 'h-9  px-3.5 text-sm',
  md: 'h-10 px-4   text-sm',
  lg: 'h-11 px-5   text-[15px]',
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
    :style="variant === 'accent' ? accentStyle : undefined"
    @click="handleClick"
  >
    <svg v-if="loading" class="animate-spin h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
    </svg>
    <slot />
  </button>
</template>
