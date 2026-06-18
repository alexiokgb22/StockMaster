<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  modelValue?: string | number
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'url' | 'search'
  placeholder?: string
  disabled?: boolean
  readonly?: boolean
  error?: boolean
  icon?: any
  iconPosition?: 'left' | 'right'
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  size: 'md',
  iconPosition: 'left'
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

const iconPaddingClass = computed(() => {
  if (!props.icon) return ''
  return props.iconPosition === 'left' ? 'pl-10' : 'pr-10'
})

function handleInput(e: Event) {
  const target = e.target as HTMLInputElement
  emit('update:modelValue', props.type === 'number' ? Number(target.value) : target.value)
}
</script>

<template>
  <div class="relative">
    <!-- Icon gauche -->
    <div v-if="icon && iconPosition === 'left'" class="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted pointer-events-none">
      <component :is="icon" class="w-4 h-4" />
    </div>

    <!-- Input -->
    <input
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      @input="handleInput"
      :class="[
        'w-full rounded-lg border bg-surface transition-all outline-none font-sans',
        'placeholder:text-text-muted text-text-main',
        sizeClasses,
        iconPaddingClass,
        error
          ? 'border-error focus:border-error focus:ring-2 focus:ring-error/20'
          : 'border-border focus:border-primary focus:ring-2 focus:ring-primary/15',
        disabled
          ? 'opacity-60 cursor-not-allowed bg-background'
          : 'hover:border-primary/50'
      ]"
    />

    <!-- Icon droite -->
    <div v-if="icon && iconPosition === 'right'" class="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted pointer-events-none">
      <component :is="icon" class="w-4 h-4" />
    </div>
  </div>
</template>
