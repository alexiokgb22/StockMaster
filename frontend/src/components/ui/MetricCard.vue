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
  primary: 'background: rgba(11,53,99,0.08); color: #0B3563',
  accent:  'background: rgba(248,216,48,0.15); color: #c9a800',
  success: 'background: #d1fae5; color: #059669',
  warning: 'background: #fef3c7; color: #d97706',
  error:   'background: #fee2e2; color: #dc2626',
  info:    'background: #dbeafe; color: #2563eb',
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
  <div class="bg-surface rounded-xl shadow-sm border border-border-light p-5 transition-base hover:shadow-md hover:-translate-y-px">
    <!-- Skeleton -->
    <template v-if="loading">
      <div class="flex items-start justify-between gap-3">
        <div class="space-y-2 flex-1">
          <div class="skeleton h-3 w-20 rounded" />
          <div class="skeleton h-7 w-24 rounded" />
          <div class="skeleton h-3 w-16 rounded" />
        </div>
        <div class="skeleton w-10 h-10 rounded-lg" />
      </div>
    </template>

    <!-- Content -->
    <template v-else>
      <div class="flex items-start justify-between gap-3">
        <div class="flex-1 min-w-0">
          <p class="text-[11px] font-semibold uppercase tracking-wider text-text-muted mb-2">{{ title }}</p>
          <p class="text-2xl font-bold text-text-main tracking-tight leading-none">{{ value }}</p>
          <p v-if="subtitle" class="text-caption mt-1">{{ subtitle }}</p>
          <div v-if="trendValue" class="flex items-center gap-1.5 mt-2">
            <span :class="['text-xs font-semibold', trendClasses[trend]]">
              {{ trendIcons[trend] }} {{ trendValue }}
            </span>
            <span class="text-caption">{{ trendLabel || 'vs mois dernier' }}</span>
          </div>
        </div>
        <div v-if="icon" class="p-2.5 rounded-lg shrink-0" :style="iconBg[color]">
          <component :is="icon" class="w-5 h-5" />
        </div>
      </div>
    </template>
  </div>
</template>
