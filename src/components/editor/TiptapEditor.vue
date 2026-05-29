<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Placeholder from '@tiptap/extension-placeholder'

const props = defineProps<{
  contentId: number | null
}>()

const emit = defineEmits<{
  change: [tiptapJson: string, title: string]
}>()

const title = ref('')

const editor = useEditor({
  content: '',
  extensions: [
    StarterKit,
    Image.configure({ allowBase64: false }),
    Placeholder.configure({ placeholder: '开始编写内容...' })
  ],
  onUpdate: ({ editor }) => {
    emit('change', JSON.stringify(editor.getJSON()), title.value)
  }
})

function onTitleChange() {
  if (editor.value) {
    emit('change', JSON.stringify(editor.value.getJSON()), title.value)
  }
}

function insertImage(url: string) {
  editor.value?.chain().focus().setImage({ src: url }).run()
}

defineExpose({ insertImage })
</script>

<template>
  <div class="tiptap-editor">
    <input
      v-model="title"
      class="title-input"
      type="text"
      placeholder="请输入文章标题..."
      @input="onTitleChange"
    />
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
.title-input {
  border: none;
  outline: none;
  font-size: 22px;
  font-weight: 600;
  padding: 20px 24px 12px;
  border-bottom: 1px solid #f0f0f0;
}
.editor-toolbar {
  display: flex;
  gap: 2px;
  padding: 8px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-wrap: wrap;
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
