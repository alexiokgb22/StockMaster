<template>
  <label class="block text-sm font-medium text-text-secondary">
    <span class="mb-2 inline-block">{{ label }}</span>
    <input
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      @input="onInput"
      class="w-full rounded-3xl border border-border bg-surface px-4 py-3 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
    />
  </label>
</template>

<script setup lang="ts">
const props = defineProps<{
  label: string
  modelValue?: string | number
  placeholder?: string
  type?: string
  disabled?: boolean
}>()

const emit = defineEmits<{ (event: 'update:modelValue', value: string | number): void }>()

const onInput = (event: Event) => {
  const target = event.target as HTMLInputElement | null
  if (!target) return
  emit('update:modelValue', target.value)
}
</script>
