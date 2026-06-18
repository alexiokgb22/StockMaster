<script setup lang="ts">
interface Props {
  width?: string
  height?: string
  rounded?: 'sm' | 'md' | 'lg' | 'full'
  lines?: number
}
withDefaults(defineProps<Props>(), { rounded: 'md', lines: 1 })
const r: Record<string, string> = { sm: 'rounded', md: 'rounded-md', lg: 'rounded-xl', full: 'rounded-full' }
</script>

<template>
  <template v-if="lines > 1">
    <div v-for="i in lines" :key="i"
      :class="['skeleton', r[rounded]]"
      :style="{ height: height || '1rem', width: i === lines && lines > 1 ? '70%' : (width || '100%'), marginBottom: i < lines ? '0.5rem' : '0' }" />
  </template>
  <div v-else
    :class="['skeleton', r[rounded]]"
    :style="{ width: width || '100%', height: height || '1rem' }" />
</template>
