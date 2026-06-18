<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  modelValue?: string
  placeholder?: string
  disabled?: boolean
  readonly?: boolean
  error?: boolean
  rows?: number
  maxlength?: number
}

const props = withDefaults(defineProps<Props>(), {
  rows: 4
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

function handleInput(e: Event) {
  const target = e.target as HTMLTextAreaElement
  emit('update:modelValue', target.value)
}
</script>

<template>
  <div class="relative">
    <textarea
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      :rows="rows"
      :maxlength="maxlength"
      @input="handleInput"
      :class="[
        'w-full rounded-lg border bg-surface transition-all outline-none font-sans',
        'px-4 py-3 text-sm',
        'placeholder:text-text-muted text-text-main',
        'resize-y min-h-[80px]',
        error
          ? 'border-error focus:border-error focus:ring-2 focus:ring-error/20'
          : 'border-border focus:border-primary focus:ring-2 focus:ring-primary/15',
        disabled
          ? 'opacity-60 cursor-not-allowed bg-background'
          : 'hover:border-primary/50'
      ]"
    />
    <div v-if="maxlength" class="absolute bottom-2 right-3 text-xs text-text-muted pointer-events-none">
      {{ modelValue?.length || 0 }}/{{ maxlength }}
    </div>
  </div>
</template>
