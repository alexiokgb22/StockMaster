<script setup lang="ts">
type Variant =
  | 'draft' | 'validated' | 'shipped' | 'received' | 'cancelled' | 'pending'
  | 'critical' | 'success' | 'info' | 'warning' | 'default' | 'active' | 'inactive'

interface Props {
  variant?: Variant
  size?: 'sm' | 'md' | 'lg'
  dot?: boolean
  pill?: boolean
}

withDefaults(defineProps<Props>(), {
  variant: 'default',
  size: 'md',
  dot: false,
  pill: true
})

const variants: Record<Variant, string> = {
  draft:      'bg-gray-100 text-gray-600 border border-gray-200',
  validated:  'bg-info-light text-info-dark border border-info/20',
  shipped:    'bg-purple-100 text-purple-700 border border-purple-200',
  received:   'bg-success-light text-success-dark border border-success/20',
  cancelled:  'bg-error-light text-error-dark border border-error/20',
  pending:    'bg-warning-light text-warning-dark border border-warning/20',
  critical:   'bg-error-light text-error-dark border border-error/30 font-bold',
  success:    'bg-success-light text-success-dark border border-success/20',
  info:       'bg-info-light text-info-dark border border-info/20',
  warning:    'bg-warning-light text-warning-dark border border-warning/20',
  active:     'bg-success-light text-success-dark border border-success/20',
  inactive:   'bg-gray-100 text-gray-500 border border-gray-200',
  default:    'bg-primary-100 text-primary border border-primary-200',
}

const dots: Record<Variant, string> = {
  draft:     'bg-gray-400',
  validated: 'bg-info',
  shipped:   'bg-purple-500',
  received:  'bg-success',
  cancelled: 'bg-error',
  pending:   'bg-warning',
  critical:  'bg-error animate-pulse',
  success:   'bg-success',
  info:      'bg-info',
  warning:   'bg-warning',
  active:    'bg-success',
  inactive:  'bg-gray-400',
  default:   'bg-primary',
}

const sizes: Record<string, string> = {
  sm: 'text-xs px-2 py-0.5',
  md: 'text-xs px-2.5 py-1',
  lg: 'text-sm px-3 py-1.5',
}
</script>

<template>
  <span :class="['inline-flex items-center gap-1.5 font-semibold', pill ? 'rounded-full' : 'rounded-md', variants[variant ?? 'default'], sizes[size ?? 'md']]">
    <span v-if="dot" :class="['w-1.5 h-1.5 rounded-full shrink-0', dots[variant ?? 'default']]" />
    <slot />
  </span>
</template>
