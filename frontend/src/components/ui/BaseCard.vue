<script setup lang="ts">
interface Props {
  padding?: 'none' | 'sm' | 'md' | 'lg'
  hover?: boolean
  flat?: boolean
}
withDefaults(defineProps<Props>(), { padding: 'md', hover: false, flat: false })

// Espacements sur l'échelle 8/16/24/32px
const paddings: Record<string, string> = {
  none: '',
  sm:   'p-4',     /* 16px */
  md:   'p-5',     /* 20px — était p-6 (24px), légèrement réduit */
  lg:   'p-6',     /* 24px */
}
</script>

<template>
  <div :class="[
    'bg-surface rounded-xl transition-base',
    paddings[padding],
    flat ? '' : 'shadow-sm border border-border-light',
    hover ? 'hover:shadow-md hover:-translate-y-px cursor-pointer' : '',
  ]">
    <slot />
  </div>
</template>
