<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import TiptapEditor from '@/components/editor/TiptapEditor.vue'
import PlatformPreviewPanel from '@/components/preview/PlatformPreviewPanel.vue'
import { useEditorStore } from '@/stores/editor'
import { contentApi } from '@/api/content'
import { aiApi } from '@/api/ai'
import type { Content } from '@/types/content'

const route = useRoute()
const router = useRouter()
const store = useEditorStore()

const contentId = ref<number | null>(null)
const currentTiptapJson = ref<string>('')
const currentTitle = ref<string>('')
const initialJson = ref<string>('')
const initialTitle = ref<string>('')
const saveStatus = ref<'idle' | 'saving' | 'saved' | 'error'>('idle')
const templateList = ref<any[]>([])
const applying = ref(false)

// 从路由加载内容
const contentIdParam = route.params.id
if (contentIdParam) {
  const id = Number(contentIdParam)
  if (!isNaN(id)) {
    loadContent(id)
  }
}

async function loadContent(id: number) {
  try {
    const res = await contentApi.getById(id)
    const content: Content = res.data.data
    contentId.value = content.id!
    currentTiptapJson.value = content.tiptapJson
    currentTitle.value = content.title
    initialJson.value = content.tiptapJson
    initialTitle.value = content.title
    store.setContent(content)
  } catch (err) {
    console.error('加载内容失败', err)
  }
}

function onContentChange(json: string, title: string) {
  currentTiptapJson.value = json
  currentTitle.value = title
}

async function handleSave() {
  if (saveStatus.value === 'saving') return
  saveStatus.value = 'saving'
  try {
    const res = await contentApi.save({
      id: contentId.value || undefined,
      title: currentTitle.value || '未命名文章',
      tiptapJson: currentTiptapJson.value
    })
    const saved: Content = res.data.data
    if (!contentId.value) {
      contentId.value = saved.id!
      router.replace({ name: 'editor', params: { id: saved.id } })
    }
    initialJson.value = saved.tiptapJson
    initialTitle.value = saved.title
    store.setContent(saved)
    saveStatus.value = 'saved'
    setTimeout(() => { if (saveStatus.value === 'saved') saveStatus.value = 'idle' }, 2000)
  } catch (err) {
    console.error('保存失败', err)
    saveStatus.value = 'error'
    setTimeout(() => { if (saveStatus.value === 'error') saveStatus.value = 'idle' }, 3000)
  }
}

async function loadTemplates() {
  try {
    const res: any = await aiApi.getTemplates()
    templateList.value = res.data?.records || res.data || []
  } catch { /* ignore */ }
}

async function applyTemplate(templateId: number) {
  if (!currentTiptapJson.value) { alert('请先编写内容'); return }
  // 保存获取 contentId
  if (!contentId.value) await handleSave()
  if (!contentId.value) { alert('保存失败，无法应用模板'); return }

  applying.value = true
  const targetPlatform = store.activePreviewPlatform
  try {
    const res: any = await aiApi.applyTemplate({
      contentId: contentId.value,
      templateId,
      targetPlatformCode: targetPlatform
    })
    const html = res.data?.adaptedHtml
    if (html) {
      store.setAdaptedHtml(targetPlatform, html)
    }
  } catch (err: any) {
    alert('AI改写失败: ' + (err.message || ''))
  } finally {
    applying.value = false
  }
}

onMounted(() => {
  loadTemplates()
})

const saveLabel: Record<string, string> = {
  idle: '已保存',
  saving: '保存中...',
  saved: '保存成功',
  error: '保存失败'
}
</script>

<template>
  <AppLayout>
    <div class="workspace">
      <div class="editor-panel">
        <TiptapEditor
          :content-id="contentId"
          :initial-title="initialTitle"
          :initial-json="initialJson"
          @change="onContentChange"
          @save="handleSave"
        />
        <!-- 模板按钮栏 -->
        <div class="template-bar" v-if="templateList.length > 0">
          <span class="template-label">AI风格改写:</span>
          <button
            v-for="t in templateList"
            :key="t.id"
            class="template-btn"
            :disabled="applying"
            :title="t.description"
            @click="applyTemplate(t.id)"
          >
            {{ t.icon }} {{ t.name }}
          </button>
          <span v-if="applying" class="applying-text">AI改写中...</span>
        </div>
      </div>
      <div class="preview-panel">
        <PlatformPreviewPanel
          :tiptap-json="currentTiptapJson"
          :title="currentTitle"
        />
      </div>
    </div>
    <div v-if="saveStatus !== 'idle'" class="save-toast" :class="saveStatus">
      {{ saveLabel[saveStatus] }}
    </div>
  </AppLayout>
</template>

<style scoped>
.workspace {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.editor-panel {
  flex: 1;
  overflow-y: auto;
  border-right: 1px solid #e8e8e8;
  background: #fff;
}
.preview-panel {
  flex: 1;
  overflow-y: auto;
  background: #fafafa;
}
.template-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}
.template-label {
  font-size: 12px;
  color: #888;
  margin-right: 4px;
}
.template-btn {
  padding: 4px 10px;
  font-size: 12px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 14px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}
.template-btn:hover {
  background: #e6f0ff;
  border-color: #4a90d9;
}
.template-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.applying-text {
  font-size: 12px;
  color: #4a90d9;
  margin-left: 6px;
}
.save-toast {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 8px 24px;
  border-radius: 6px;
  font-size: 13px;
  z-index: 1000;
  transition: opacity 0.3s;
}
.save-toast.saving {
  background: #fff3cd;
  color: #856404;
}
.save-toast.saved {
  background: #d4edda;
  color: #155724;
}
.save-toast.error {
  background: #f8d7da;
  color: #721c24;
}
</style>
