<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import TiptapEditor from '@/components/editor/TiptapEditor.vue'
import PlatformPreviewPanel from '@/components/preview/PlatformPreviewPanel.vue'
import { useEditorStore } from '@/stores/editor'
import { contentApi } from '@/api/content'
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
