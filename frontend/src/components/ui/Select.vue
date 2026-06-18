<template>
  <BaseSelect
    v-bind="$attrs"
    :modelValue="props.modelValue"
    @update:modelValue="updateValue"
  />
</template>

<script setup lang="ts">
import BaseSelect from './BaseSelect.vue'

const props = defineProps<{
  modelValue?: string | number
}>()

const emit = defineEmits<{ (event: 'update:modelValue', value: string | number): void }>()

const updateValue = (value: string | number) => emit('update:modelValue', value)
</script>
