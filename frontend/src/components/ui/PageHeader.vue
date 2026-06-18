<script setup lang="ts">
interface Breadcrumb { label: string; to?: string }
interface Props {
  title: string
  subtitle?: string
  breadcrumbs?: Breadcrumb[]
}
defineProps<Props>()
</script>

<template>
  <div class="mb-6">
    <!-- Breadcrumb -->
    <nav v-if="breadcrumbs?.length" class="flex items-center gap-1.5 text-caption mb-2">
      <template v-for="(crumb, i) in breadcrumbs" :key="i">
        <router-link v-if="crumb.to" :to="crumb.to" class="text-text-muted hover:text-primary transition-fast">
          {{ crumb.label }}
        </router-link>
        <span v-else class="text-text-main font-medium">{{ crumb.label }}</span>
        <span v-if="i < breadcrumbs.length - 1" class="text-text-subtle">/</span>
      </template>
    </nav>

    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="heading-1">{{ title }}</h1>
        <p v-if="subtitle" class="mt-1 text-sm text-text-muted">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="flex items-center gap-2 shrink-0">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>
