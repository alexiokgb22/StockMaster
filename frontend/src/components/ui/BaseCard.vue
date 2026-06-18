<script setup lang="ts">
interface Props {
  padding?: 'none' | 'sm' | 'md' | 'lg' | 'xl'
  hover?: boolean
  bordered?: boolean
  flat?: boolean
}
withDefaults(defineProps<Props>(), { padding: 'md', hover: false, bordered: false, flat: false })

const paddings: Record<string, string> = {
  none: '',
  sm: 'p-4',
  md: 'p-6',
  lg: 'p-8',
  xl: 'p-10'
}
</script>

<template>
  <div :class="[
    'bg-surface rounded-xl transition-base',
    paddings[padding],
    flat  ? '' : 'shadow-sm border border-border-light',
    hover ? 'hover:shadow-md hover:-translate-y-0.5 cursor-pointer' : '',
    bordered ? 'border-2 border-border' : '',
  ]">
    <slot />
  </div>
</template>
