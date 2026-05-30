<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { dashboardApi } from '@/api/dashboard'

interface PublishItem {
  id: number
  contentId: number
  platformCode: string
  status: string
  platformUrl?: string
  errorMsg?: string
  createdAt: string
  retractedAt?: string
}

const history = ref<PublishItem[]>([])
const total = ref(0)
const page = ref(1)
const filterPlatform = ref('')
const filterStatus = ref('')
const retracting = ref<number | null>(null)

const platformOptions = ['', 'WECHAT', 'ZHIHU', 'BILIBILI', 'XHS', 'TOUTIAO']
const platformNames: Record<string, string> = {
  '': '全部平台', WECHAT: '公众号', ZHIHU: '知乎', BILIBILI: 'B站', XHS: '小红书', TOUTIAO: '头条'
}
const statusOptions = ['', 'SUCCESS', 'FAILED', 'RETRACTED', 'PENDING']
const statusNames: Record<string, string> = {
  '': '全部状态', SUCCESS: '成功', FAILED: '失败', RETRACTED: '已撤回', PENDING: '处理中'
}
const statusColors: Record<string, string> = {
  SUCCESS: '#52c41a', FAILED: '#ff4d4f', RETRACTED: '#999', PENDING: '#faad14',
  PREFLIGHT: '#faad14', FILLING: '#1890ff', PUBLISHING: '#1890ff', CONFIRMING: '#1890ff'
}

async function loadHistory() {
  try {
    const res: any = await dashboardApi.getHistory({
      page: page.value, size: 10,
      platform: filterPlatform.value || undefined,
      status: filterStatus.value || undefined
    })
    history.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ }
}

async function doRetract(publishLogId: number) {
  if (!confirm('确定撤回此发布吗？')) return
  retracting.value = publishLogId
  try {
    await dashboardApi.retract(publishLogId)
    await loadHistory()
  } catch (err: any) { alert('撤回失败: ' + (err.message || '')) }
  finally { retracting.value = null }
}

onMounted(loadHistory)
</script>

<template>
  <AppLayout>
    <div class="history-page">
      <h2>发布历史</h2>

      <div class="filters">
        <select v-model="filterPlatform" class="filter-select" @change="loadHistory">
          <option v-for="p in platformOptions" :key="p" :value="p">{{ platformNames[p] }}</option>
        </select>
        <select v-model="filterStatus" class="filter-select" @change="loadHistory">
          <option v-for="s in statusOptions" :key="s" :value="s">{{ statusNames[s] }}</option>
        </select>
      </div>

      <div v-if="history.length === 0" class="empty">暂无发布记录</div>

      <div v-else class="history-list">
        <div v-for="item in history" :key="item.id" class="history-item">
          <div class="item-left">
            <span class="platform-tag">{{ platformNames[item.platformCode] || item.platformCode }}</span>
            <span class="status-badge" :style="{ color: statusColors[item.status] || '#999' }">
              {{ statusNames[item.status] || item.status }}
            </span>
          </div>
          <div class="item-center">
            <a v-if="item.platformUrl" :href="item.platformUrl" target="_blank" class="item-link">
              查看文章 &rarr;
            </a>
            <span v-if="item.errorMsg" class="error-msg">{{ item.errorMsg }}</span>
          </div>
          <div class="item-right">
            <span class="time">{{ item.createdAt?.substring(0, 16) }}</span>
            <button
              v-if="item.status === 'SUCCESS'"
              class="retract-btn"
              :disabled="retracting === item.id"
              @click="doRetract(item.id)"
            >
              {{ retracting === item.id ? '撤回中...' : '撤回' }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="total > 10" class="pagination">
        <button :disabled="page <= 1" @click="page--; loadHistory()">上一页</button>
        <span>{{ page }} / {{ Math.ceil(total / 10) }}</span>
        <button :disabled="page >= Math.ceil(total / 10)" @click="page++; loadHistory()">下一页</button>
      </div>
    </div>
  </AppLayout>
</template>

<style scoped>
.history-page { padding: 24px; max-width: 900px; margin: 0 auto; }
.history-page h2 { margin: 0 0 16px; font-size: 20px; }
.filters { display: flex; gap: 10px; margin-bottom: 16px; }
.filter-select { padding: 5px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; }
.empty { color: #999; text-align: center; padding: 40px; }
.history-list { display: flex; flex-direction: column; gap: 6px; }
.history-item {
  display: flex; align-items: center; gap: 16px;
  padding: 12px 16px; background: #fff; border-radius: 6px;
  border: 1px solid #eee;
}
.item-left { display: flex; align-items: center; gap: 8px; min-width: 160px; }
.platform-tag { font-size: 12px; background: #f0f0f0; padding: 2px 8px; border-radius: 3px; font-weight: 500; }
.status-badge { font-size: 12px; font-weight: 500; }
.item-center { flex: 1; }
.item-link { color: #4a90d9; text-decoration: none; font-size: 13px; }
.item-link:hover { text-decoration: underline; }
.error-msg { font-size: 12px; color: #ff4d4f; }
.item-right { display: flex; align-items: center; gap: 12px; }
.time { font-size: 12px; color: #aaa; white-space: nowrap; }
.retract-btn {
  padding: 3px 10px; font-size: 11px; border: 1px solid #e8a0a0; background: #fff; color: #c0392b;
  border-radius: 3px; cursor: pointer;
}
.retract-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 12px; margin-top: 16px; font-size: 13px; }
.pagination button { padding: 4px 12px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; }
.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
