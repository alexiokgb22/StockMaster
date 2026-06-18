<template>
  <div class="space-y-2" v-bind="$attrs">
    <label class="block text-sm font-medium text-text-secondary">
      {{ label }}<span v-if="required" class="text-error">*</span>
    </label>
    <slot />
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  label: string
  required?: boolean
}>()
</script>
