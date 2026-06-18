<template>
  <div class="overflow-hidden rounded-[32px] border border-border bg-white shadow-card">
    <table class="w-full min-w-full border-collapse text-left text-sm text-text-secondary">
      <thead class="bg-background text-text-secondary/80">
        <tr>
          <th v-for="column in columns" :key="column.key" class="px-4 py-4 font-medium">
            {{ column.label }}
          </th>
        </tr>
      </thead>
      <tbody>
        <slot />
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
interface TableColumn {
  key: string
  label: string
}

const props = defineProps<{
  columns: TableColumn[]
}>()
</script>
