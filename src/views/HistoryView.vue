<script setup lang="ts">
import AppLayout from '@/components/layout/AppLayout.vue'
</script>

<template>
  <AppLayout>
    <div class="page">
      <h2>发布历史</h2>
      <p class="placeholder">发布历史列表将在此展示</p>
    </div>
  </AppLayout>
</template>

<style scoped>
.page { padding: 24px; flex: 1; }
.placeholder { color: #999; margin-top: 16px; }
</style>
