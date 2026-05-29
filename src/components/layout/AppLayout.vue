<script setup lang="ts">
import AppHeader from './AppHeader.vue'

defineProps<{
  showSidebar?: boolean
}>()
</script>

<template>
  <div class="app-layout">
    <AppHeader />
    <main class="app-main">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f6f8;
}
.app-main {
  flex: 1;
  overflow: hidden;
  display: flex;
}
</style>
