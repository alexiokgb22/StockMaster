<template>
  <BaseModal v-bind="$attrs" :title="title" @close="$emit('close')">
    <slot />
  </BaseModal>
</template>

<script setup lang="ts">
import BaseModal from './BaseModal.vue'

const props = defineProps<{
  title: string
}>()

const emit = defineEmits<{ (e: 'close'): void }>()
</script>
