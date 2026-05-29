<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <header class="app-header">
    <div class="header-left">
      <h1 class="logo" @click="router.push('/')">多平台内容发布工具</h1>
    </div>
    <nav class="header-nav">
      <button class="nav-btn" @click="router.push('/')">工作台</button>
      <button class="nav-btn" @click="router.push('/templates')">模板管理</button>
      <button class="nav-btn" @click="router.push('/history')">发布历史</button>
      <button class="nav-btn" @click="router.push('/accounts')">平台管理</button>
    </nav>
  </header>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.logo { font-size: 16px; font-weight: 600; color: #1a1a2e; cursor: pointer; margin: 0; }
.header-nav { display: flex; gap: 4px; }
.nav-btn {
  padding: 6px 12px;
  border: none;
  background: transparent;
  color: #555;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
}
.nav-btn:hover { background: #f0f0f0; color: #333; }
</style>
