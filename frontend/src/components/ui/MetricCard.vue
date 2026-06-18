<script setup lang="ts">
import type { Component } from 'vue'

interface Props {
  title: string
  value: string | number
  subtitle?: string
  icon?: Component
  trend?: 'up' | 'down' | 'neutral'
  trendValue?: string
  trendLabel?: string
  color?: 'primary' | 'accent' | 'success' | 'warning' | 'error' | 'info'
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  color: 'primary',
  trend: 'neutral',
  loading: false,
})

const iconBg: Record<string, string> = {
  primary: 'bg-primary text-white',
  accent:  'bg-accent text-primary',
  success: 'bg-success text-white',
  warning: 'bg-warning text-white',
  error:   'bg-error text-white',
  info:    'bg-info text-white',
}

const trendClasses: Record<string, string> = {
  up:      'text-success',
  down:    'text-error',
  neutral: 'text-text-muted',
}

const trendIcons: Record<string, string> = {
  up: '↑', down: '↓', neutral: '→'
}
</script>

<template>
  <div class="bg-surface rounded-xl shadow-sm border border-border-light p-5 transition-base hover:shadow-md">
    <!-- Skeleton -->
    <template v-if="loading">
      <div class="flex items-start justify-between">
        <div class="space-y-2 flex-1">
          <div class="skeleton h-3 w-24 rounded" />
          <div class="skeleton h-8 w-32 rounded" />
          <div class="skeleton h-3 w-20 rounded" />
        </div>
        <div class="skeleton w-12 h-12 rounded-xl" />
      </div>
    </template>

    <!-- Content -->
    <template v-else>
      <div class="flex items-start justify-between gap-3">
        <div class="flex-1 min-w-0">
          <p class="text-label mb-1">{{ title }}</p>
          <p class="text-3xl font-extrabold text-text-main tracking-tight">{{ value }}</p>
          <p v-if="subtitle" class="text-caption mt-1">{{ subtitle }}</p>
          <div v-if="trendValue" class="flex items-center gap-1.5 mt-2">
            <span :class="['text-sm font-bold', trendClasses[trend]]">
              {{ trendIcons[trend] }} {{ trendValue }}
            </span>
            <span class="text-caption">{{ trendLabel || 'vs mois dernier' }}</span>
          </div>
        </div>
        <div v-if="icon" :class="['p-3 rounded-xl shrink-0', iconBg[color]]">
          <component :is="icon" class="w-6 h-6" />
        </div>
      </div>
    </template>
  </div>
</template>
