<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { platformApi } from '@/api/platform'
import { useEditorStore } from '@/stores/editor'

const props = defineProps<{
  tiptapJson: string
  title: string
}>()

const emit = defineEmits<{
  publish: []
}>()

const store = useEditorStore()

const platforms = [
  { code: 'WECHAT', name: '公众号', color: '#07c160' },
  { code: 'ZHIHU', name: '知乎', color: '#056de8' },
  { code: 'BILIBILI', name: 'B站', color: '#fb7299' },
  { code: 'XHS', name: '小红书', color: '#ff2442' },
  { code: 'TOUTIAO', name: '头条', color: '#e74c3c' }
]

const previewHtml = ref('')
const wordCount = ref(0)
const warnings = ref<Array<{ type: string; message: string }>>([])
const videoLimits = ref<Record<string, any>>({})
const isLoading = ref(false)

onMounted(async () => {
  try {
    const res: any = await platformApi.getVideoLimits()
    videoLimits.value = res.data || {}
  } catch { /* 接口暂未就绪 */ }
})

async function loadPreview(platformCode: string) {
  store.activePreviewPlatform = platformCode
  if (!props.tiptapJson) {
    previewHtml.value = ''
    wordCount.value = 0
    warnings.value = []
    return
  }

  // 优先使用 AI 改写后的内容
  const adapted = store.getAdaptedHtml(platformCode)
  if (adapted) {
    previewHtml.value = adapted
    wordCount.value = adapted.replace(/<[^>]+>/g, '').replace(/\s+/g, '').length
    warnings.value = []
    return
  }

  isLoading.value = true
  try {
    const res: any = await platformApi.convert({
      tiptapJson: props.tiptapJson,
      platformCode,
      title: props.title
    })
    previewHtml.value = res.data?.adaptedHtml || ''
    wordCount.value = res.data?.wordCount || 0
    warnings.value = res.data?.platformWarnings || []
  } catch {
    previewHtml.value = '<p style="color:#999">预览加载失败，请检查后端服务</p>'
    wordCount.value = 0
    warnings.value = []
  } finally {
    isLoading.value = false
  }
}

function getVideoWarning(platformCode: string): string | null {
  const limit = videoLimits.value[platformCode]
  if (!limit) return null
  if (!limit.supported) return limit.reason || '不支持视频'
  return null
}

watch(() => store.activePreviewPlatform, (code) => {
  if (code) loadPreview(code)
})

watch(() => store.adaptedHtmlMap.get(store.activePreviewPlatform), () => {
  if (store.activePreviewPlatform) {
    loadPreview(store.activePreviewPlatform)
  }
})

watch(() => props.tiptapJson, () => {
  if (store.activePreviewPlatform) {
    loadPreview(store.activePreviewPlatform)
  }
})
</script>

<template>
  <div class="preview-panel">
    <div class="platform-tabs">
      <button
        v-for="p in platforms"
        :key="p.code"
        :class="['tab-btn', { active: store.activePreviewPlatform === p.code }]"
        @click="store.activePreviewPlatform = p.code"
      >
        <span class="tab-dot" :style="{ background: p.color }"></span>
        {{ p.name }}
      </button>
    </div>

    <div class="platform-status">
      <label class="publish-check" v-for="p in platforms" :key="p.code">
        <input
          type="checkbox"
          :checked="store.selectedPlatforms.includes(p.code)"
          @change="store.togglePlatform(p.code)"
        />
        {{ p.name }}
      </label>
    </div>

    <div class="preview-content">
      <div class="phone-frame">
        <div v-if="isLoading" class="preview-loading">加载中...</div>
        <div v-else class="phone-screen" v-html="previewHtml"></div>
      </div>
    </div>

    <div class="preview-info">
      <span class="info-item">字数: {{ wordCount }}</span>
      <span class="info-item">平台: {{ platforms.find(p => p.code === store.activePreviewPlatform)?.name }}</span>
    </div>

    <div v-if="warnings.length > 0" class="preview-warnings">
      <span v-for="w in warnings" :key="w.type" class="warning-tag">
        {{ w.message }}
      </span>
    </div>

    <div v-if="getVideoWarning(store.activePreviewPlatform)" class="video-warning">
      {{ getVideoWarning(store.activePreviewPlatform) }}
    </div>

    <div class="preview-actions">
      <button class="publish-btn" :disabled="store.selectedPlatforms.length === 0"
              @click="emit('publish')">
        一键发布到已选平台 ({{ store.selectedPlatforms.length }})
      </button>
    </div>
  </div>
</template>

<style scoped>
.preview-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 12px;
}
.platform-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}
.tab-btn {
  padding: 4px 10px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.tab-btn.active {
  border-color: #4a90d9;
  background: #e6f0ff;
}
.tab-dot { width: 6px; height: 6px; border-radius: 50%; }
.platform-status {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  font-size: 12px;
  flex-wrap: wrap;
}
.publish-check { display: flex; align-items: center; gap: 4px; cursor: pointer; }
.preview-content {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 12px 0;
  overflow-y: auto;
}
.preview-loading {
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}
.phone-frame {
  width: 375px;
  min-height: 500px;
  border: 2px solid #333;
  border-radius: 20px;
  padding: 20px 16px;
  background: #fff;
}
.phone-screen {
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.phone-screen :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}
.phone-screen :deep(h1) { font-size: 18px; }
.phone-screen :deep(h2) { font-size: 16px; }
.phone-screen :deep(h3) { font-size: 15px; }
.phone-screen :deep(blockquote) {
  border-left: 3px solid #4a90d9;
  padding-left: 8px;
  margin-left: 0;
  color: #666;
}
.phone-screen :deep(pre) {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
}
.preview-info {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  font-size: 12px;
  color: #888;
}
.info-item { white-space: nowrap; }
.preview-warnings {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  padding: 4px 0;
}
.warning-tag {
  font-size: 11px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 2px 8px;
  border-radius: 3px;
}
.video-warning {
  font-size: 11px;
  color: #e6a23c;
  padding: 4px 0;
}
.preview-actions {
  padding: 8px 0;
  display: flex;
  gap: 8px;
}
.publish-btn {
  flex: 1;
  padding: 10px;
  background: #4a90d9;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}
.publish-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
