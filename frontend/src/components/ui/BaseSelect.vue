<template>
  <label class="block text-sm font-medium text-text-secondary">
    <span class="mb-2 inline-block">{{ label }}</span>
    <select
      :value="modelValue"
      :disabled="disabled"
      @change="onChange"
      class="w-full rounded-3xl border border-border bg-surface px-4 py-3 text-sm text-text-main outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
    >
      <option v-if="optional" value="">{{ optionalLabel }}</option>
      <option v-for="option in options" :key="option.value" :value="option.value">
        {{ option.label }}
      </option>
    </select>
  </label>
</template>

<script setup lang="ts">
interface SelectOption {
  label: string
  value: string | number
}

const props = defineProps<{
  label: string
  modelValue?: string | number
  options?: SelectOption[]
  optional?: boolean
  optionalLabel?: string
  disabled?: boolean
}>()

const emit = defineEmits<{ (event: 'update:modelValue', value: string | number): void }>()

const onChange = (event: Event) => {
  const target = event.target as HTMLSelectElement | null
  if (!target) return
  emit('update:modelValue', target.value)
}
</script>
