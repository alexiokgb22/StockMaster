<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  modelValue?: string | number
  options: Array<{ value: string | number; label: string }>
  placeholder?: string
  disabled?: boolean
  error?: boolean
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
}>()

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm': return 'px-3 py-2 text-sm'
    case 'lg': return 'px-4 py-3.5 text-base'
    default:   return 'px-4 py-3 text-sm'
  }
})

function handleChange(e: Event) {
  const target = e.target as HTMLSelectElement
  emit('update:modelValue', target.value)
}
</script>

<template>
  <select
    :value="modelValue"
    :disabled="disabled"
    @change="handleChange"
    :class="[
      'w-full rounded-lg border bg-surface transition-all outline-none font-sans appearance-none',
      'bg-[url(\'data:image/svg+xml;charset=utf-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20fill%3D%22none%22%20viewBox%3D%220%200%2020%2020%22%3E%3Cpath%20stroke%3D%22%236b7280%22%20stroke-linecap%3D%22round%22%20stroke-linejoin%3D%22round%22%20stroke-width%3D%221.5%22%20d%3D%22m6%208%204%204%204-4%22%2F%3E%3C%2Fsvg%3E\')] bg-[length:1.25rem] bg-[right_0.5rem_center] bg-no-repeat',
      'pr-10',
      sizeClasses,
      error
        ? 'border-error focus:border-error focus:ring-2 focus:ring-error/20'
        : 'border-border focus:border-primary focus:ring-2 focus:ring-primary/15',
      disabled
        ? 'opacity-60 cursor-not-allowed bg-background'
        : 'hover:border-primary/50 text-text-main cursor-pointer'
    ]"
  >
    <option v-if="placeholder" value="" disabled>{{ placeholder }}</option>
    <option v-for="opt in options" :key="opt.value" :value="opt.value">
      {{ opt.label }}
    </option>
  </select>
</template>
