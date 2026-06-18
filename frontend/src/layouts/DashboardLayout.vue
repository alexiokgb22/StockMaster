<template>
  <div class="min-h-screen bg-background text-text-main">
    <div class="flex min-h-screen">
      <AppSidebar />
      <div class="flex-1 flex flex-col">
        <AppHeader />
        <main class="flex-1 p-6 xl:p-8">
          <router-view />
        </main>
      </div>
    </div>
    <ToastContainer />
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'
</script>
