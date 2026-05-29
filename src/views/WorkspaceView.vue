<script setup lang="ts">
import { ref } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import TiptapEditor from '@/components/editor/TiptapEditor.vue'
import PlatformPreviewPanel from '@/components/preview/PlatformPreviewPanel.vue'

const contentId = ref<number | null>(null)
const currentTiptapJson = ref<string>('')
const currentTitle = ref<string>('')

function onContentChange(json: string, title: string) {
  currentTiptapJson.value = json
  currentTitle.value = title
}
</script>

<template>
  <AppLayout>
    <div class="workspace">
      <div class="editor-panel">
        <TiptapEditor
          :content-id="contentId"
          @change="onContentChange"
        />
      </div>
      <div class="preview-panel">
        <PlatformPreviewPanel
          :tiptap-json="currentTiptapJson"
          :title="currentTitle"
        />
      </div>
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
</style>
