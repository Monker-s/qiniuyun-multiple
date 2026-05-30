<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Placeholder from '@tiptap/extension-placeholder'
import { filesApi } from '@/api/files'

const CHUNK_SIZE = 5 * 1024 * 1024 // 5MB per chunk

const props = defineProps<{
  contentId: number | null
  initialTitle?: string
  initialJson?: string
}>()

const emit = defineEmits<{
  change: [tiptapJson: string, title: string]
  save: []
}>()

const title = ref(props.initialTitle || '')
const isUploading = ref(false)
const uploadProgress = ref(0)
const imageInput = ref<HTMLInputElement | null>(null)
const videoInput = ref<HTMLInputElement | null>(null)

const editor = useEditor({
  content: props.initialJson ? JSON.parse(props.initialJson) : '',
  extensions: [
    StarterKit,
    Image.configure({ allowBase64: false }),
    Placeholder.configure({ placeholder: '开始编写内容...' })
  ],
  onUpdate: ({ editor: ed }) => {
    emit('change', JSON.stringify(ed.getJSON()), title.value)
  }
})

watch(() => props.initialJson, (json) => {
  if (json && editor.value) {
    const current = JSON.stringify(editor.value.getJSON())
    if (current !== json) {
      editor.value.commands.setContent(JSON.parse(json))
    }
  }
})

watch(() => props.initialTitle, (t) => {
  if (t !== undefined && t !== title.value) {
    title.value = t
  }
})

function onTitleChange() {
  emit('change', editor.value ? JSON.stringify(editor.value.getJSON()) : '', title.value)
}

function triggerImageUpload() {
  imageInput.value?.click()
}

function triggerVideoUpload() {
  videoInput.value?.click()
}

async function onImageSelected(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }
  await uploadAndInsert(file, 'IMAGE')
  if (imageInput.value) imageInput.value.value = ''
}

async function onVideoSelected(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!file.type.startsWith('video/')) {
    alert('请选择视频文件')
    return
  }
  await uploadAndInsert(file, 'VIDEO')
  if (videoInput.value) videoInput.value.value = ''
}

async function uploadAndInsert(file: File, type: string) {
  isUploading.value = true
  uploadProgress.value = 0
  try {
    let result: any
    if (file.size < CHUNK_SIZE) {
      const res = await filesApi.upload(file, (pct) => {
        uploadProgress.value = pct
      })
      result = res.data.data
    } else {
      const uploadId = crypto.randomUUID()
      const totalChunks = Math.ceil(file.size / CHUNK_SIZE)
      for (let i = 0; i < totalChunks; i++) {
        const start = i * CHUNK_SIZE
        const chunk = file.slice(start, start + CHUNK_SIZE)
        await filesApi.uploadChunk(chunk, uploadId, i, totalChunks)
        uploadProgress.value = Math.round(((i + 1) / totalChunks) * 100)
      }
      const mergeRes = await filesApi.mergeChunks(uploadId, file.name, type)
      result = mergeRes.data.data
    }

    if (type === 'IMAGE') {
      editor.value?.chain().focus().setImage({ src: result.url }).run()
    } else {
      // 插入视频占位：带样式的链接块
      editor.value?.chain().focus().insertContent({
        type: 'paragraph',
        content: [{
          type: 'text',
          text: `[视频: ${file.name}]`,
          marks: [{ type: 'bold' }]
        }]
      }).run()
      emit('change', JSON.stringify(editor.value?.getJSON()), title.value)
    }
  } catch (err: any) {
    alert('上传失败: ' + (err.message || '未知错误'))
  } finally {
    isUploading.value = false
    uploadProgress.value = 0
  }
}

function handleSave() {
  emit('save')
}

defineExpose({
  insertImage(url: string) {
    editor.value?.chain().focus().setImage({ src: url }).run()
  }
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>

<template>
  <div class="tiptap-editor">
    <div class="editor-header">
      <input
        v-model="title"
        class="title-input"
        type="text"
        placeholder="请输入文章标题..."
        @input="onTitleChange"
      />
      <button class="save-btn" @click="handleSave" :disabled="isUploading">
        保存草稿
      </button>
    </div>

    <div class="editor-toolbar">
      <button @click="editor?.chain().focus().toggleBold().run()"
              :class="{ active: editor?.isActive('bold') }">B</button>
      <button @click="editor?.chain().focus().toggleItalic().run()"
              :class="{ active: editor?.isActive('italic') }">I</button>
      <button @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()"
              :class="{ active: editor?.isActive('heading', { level: 2 }) }">H2</button>
      <button @click="editor?.chain().focus().toggleHeading({ level: 3 }).run()"
              :class="{ active: editor?.isActive('heading', { level: 3 }) }">H3</button>
      <button @click="editor?.chain().focus().toggleBulletList().run()"
              :class="{ active: editor?.isActive('bulletList') }">列表</button>
      <button @click="editor?.chain().focus().toggleBlockquote().run()"
              :class="{ active: editor?.isActive('blockquote') }">引用</button>
      <button @click="editor?.chain().focus().toggleCodeBlock().run()"
              :class="{ active: editor?.isActive('codeBlock') }">代码</button>
      <span class="toolbar-sep"></span>
      <button @click="triggerImageUpload" class="upload-btn" :disabled="isUploading">
        🖼 图片
      </button>
      <button @click="triggerVideoUpload" class="upload-btn" :disabled="isUploading">
        📹 视频
      </button>
      <input ref="imageInput" type="file" accept="image/*" style="display:none"
             @change="onImageSelected" />
      <input ref="videoInput" type="file" accept="video/*" style="display:none"
             @change="onVideoSelected" />

      <div v-if="isUploading" class="upload-progress">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
        </div>
        <span class="progress-text">{{ uploadProgress }}%</span>
      </div>
    </div>

    <EditorContent :editor="editor" class="editor-content" />
  </div>
</template>

<style scoped>
.tiptap-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.editor-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 24px 0;
}
.title-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 22px;
  font-weight: 600;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.save-btn {
  padding: 6px 16px;
  background: #4a90d9;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
}
.save-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.editor-toolbar {
  display: flex;
  gap: 2px;
  padding: 8px 24px;
  border-bottom: 1px solid #f0f0f0;
  flex-wrap: wrap;
  align-items: center;
}
.editor-toolbar button {
  padding: 4px 10px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 3px;
  cursor: pointer;
  font-size: 13px;
}
.editor-toolbar button.active {
  background: #e6f0ff;
  border-color: #4a90d9;
  color: #4a90d9;
}
.toolbar-sep {
  width: 1px;
  height: 20px;
  background: #ddd;
  margin: 0 4px;
}
.upload-btn {
  color: #555 !important;
}
.upload-progress {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 8px;
}
.progress-bar {
  width: 80px;
  height: 4px;
  background: #eee;
  border-radius: 2px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: #4a90d9;
  transition: width 0.3s;
}
.progress-text {
  font-size: 11px;
  color: #888;
}
.editor-content {
  flex: 1;
  padding: 16px 24px;
  overflow-y: auto;
}
.editor-content :deep(.ProseMirror) {
  outline: none;
  min-height: 300px;
  font-size: 15px;
  line-height: 1.8;
}
.editor-content :deep(.ProseMirror p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  color: #adb5bd;
  pointer-events: none;
  float: left;
  height: 0;
}
.editor-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}
.editor-content :deep(pre) {
  background: #f5f5f5;
  padding: 12px 16px;
  border-radius: 4px;
  font-size: 13px;
}
.editor-content :deep(blockquote) {
  border-left: 3px solid #4a90d9;
  padding-left: 12px;
  color: #666;
}
</style>
