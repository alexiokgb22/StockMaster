<template>
  <div class="space-y-1">
    <BaseInput
      :modelValue="props.modelValue"
      :placeholder="props.placeholder"
      :type="props.type"
      :disabled="props.disabled"
      @update:modelValue="updateValue"
    />
    <p v-if="props.error" class="text-xs text-destructive">{{ props.error }}</p>
  </div>
</template>

<script setup lang="ts">
import BaseInput from './BaseInput.vue'

const props = defineProps<{
  modelValue?: string | number
  placeholder?: string
  type?: string
  disabled?: boolean
  error?: string
}>()

const emit = defineEmits<{ (event: 'update:modelValue', value: string | number): void }>()

const updateValue = (value: string | number) => emit('update:modelValue', value)
</script>
