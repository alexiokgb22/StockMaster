<template>
  <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
    <div class="flex flex-1 items-center gap-3">
      <slot name="left" />
    </div>
    <div class="flex flex-wrap items-center gap-3">
      <slot name="right" />
    </div>
  </div>
</template>

<script setup lang="ts" />
