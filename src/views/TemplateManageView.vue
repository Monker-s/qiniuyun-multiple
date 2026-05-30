<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { aiApi } from '@/api/ai'
import { filesApi } from '@/api/files'

interface Template {
  id?: number
  name: string
  icon: string
  description: string
  referenceType: string
  referenceSample?: string
  referenceImage?: string
  systemPrompt: string
  extractedStyle?: string
  isPreset: boolean
  useCount: number
}

const templates = ref<Template[]>([])
const showForm = ref(false)
const analyzing = ref(false)
const editing = ref<Template | null>(null)
const form = ref<Template>(emptyForm())
const imageInput = ref<HTMLInputElement | null>(null)

function emptyForm(): Template {
  return { name: '', icon: '📝', description: '', referenceType: 'TEXT',
    referenceSample: '', referenceImage: '', systemPrompt: '', isPreset: false, useCount: 0 }
}

const iconOptions = ['📝','🧠','🎮','✨','📰','🔥','💼','📊','💡','🎯']

async function loadTemplates() {
  try {
    const res: any = await aiApi.getTemplates()
    templates.value = res.data?.records || res.data || []
  } catch { /* ignore */ }
}

function openCreate() {
  editing.value = null
  form.value = emptyForm()
  showForm.value = true
}

function openEdit(t: Template) {
  editing.value = t
  form.value = { ...t }
  showForm.value = true
}

async function saveTemplate() {
  try {
    if (editing.value?.id) {
      await aiApi.updateTemplate(editing.value.id, form.value)
    } else {
      await aiApi.createTemplate(form.value)
    }
    showForm.value = false
    await loadTemplates()
  } catch (err: any) { alert('保存失败: ' + (err.message || '')) }
}

async function deleteTemplate(id: number) {
  if (!confirm('确定删除此模板？')) return
  try {
    await aiApi.deleteTemplate(id)
    await loadTemplates()
  } catch (err: any) { alert('删除失败: ' + (err.message || '')) }
}

async function analyzeText() {
  if (!form.value.referenceSample) { alert('请先粘贴参考文本'); return }
  analyzing.value = true
  try {
    const res: any = await aiApi.analyzeText(form.value.referenceSample)
    const data = res.data
    if (data.suggestedTemplateName) form.value.name = data.suggestedTemplateName
    if (data.generatedSystemPrompt) form.value.systemPrompt = data.generatedSystemPrompt
    if (data.tone) {
      form.value.extractedStyle = JSON.stringify({
        tone: data.tone, emojiDensity: data.emojiDensity,
        paragraphLength: data.paragraphLength, signaturePhrases: data.signaturePhrases,
        endingStyle: data.endingStyle, targetAudience: data.targetAudience
      })
    }
  } catch (err: any) { alert('分析失败: ' + (err.message || '')) }
  finally { analyzing.value = false }
}

async function onImageSelected(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  analyzing.value = true
  try {
    const uploadRes: any = await filesApi.upload(file)
    const imageUrl = uploadRes.data?.url || uploadRes.data?.data?.url
    if (imageUrl) {
      form.value.referenceImage = imageUrl
      const res: any = await aiApi.analyzeImage(imageUrl)
      const data = res.data
      if (data.suggestedTemplateName) form.value.name = data.suggestedTemplateName
      if (data.generatedSystemPrompt) form.value.systemPrompt = data.generatedSystemPrompt
      if (data.visualStyle) {
        form.value.extractedStyle = JSON.stringify({
          visualStyle: data.visualStyle, tone: data.tone,
          paragraphLength: data.paragraphLength, signaturePhrases: data.signaturePhrases
        })
      }
    }
  } catch (err: any) { alert('图片分析失败: ' + (err.message || '')) }
  finally { analyzing.value = false }
}

onMounted(loadTemplates)
</script>

<template>
  <AppLayout>
    <div class="template-page">
      <div class="page-header">
        <h2>模板管理</h2>
        <button class="btn-primary" @click="openCreate">+ 新建模板</button>
      </div>

      <div class="template-grid">
        <div v-for="t in templates" :key="t.id" class="template-card" :class="{ preset: t.isPreset }">
          <div class="card-icon">{{ t.icon }}</div>
          <div class="card-body">
            <div class="card-title">{{ t.name }}
              <span v-if="t.isPreset" class="badge-preset">内置</span>
            </div>
            <div class="card-desc">{{ t.description }}</div>
            <div v-if="t.extractedStyle" class="card-style">
              风格: {{ t.extractedStyle.substring(0, 80) }}{{ t.extractedStyle.length > 80 ? '...' : '' }}
            </div>
            <div class="card-meta">使用 {{ t.useCount }} 次</div>
          </div>
          <div class="card-actions">
            <button class="btn-small" @click="openEdit(t)">编辑</button>
            <button v-if="!t.isPreset" class="btn-small btn-danger" @click="deleteTemplate(t.id!)">删除</button>
          </div>
        </div>
      </div>

      <!-- Form Modal -->
      <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
        <div class="modal-content">
          <h3>{{ editing ? '编辑模板' : '新建模板' }}</h3>

          <div class="form-group">
            <label>模板名称</label>
            <input v-model="form.name" class="input" placeholder="如: 我的小红书爆款风" />
          </div>
          <div class="form-row">
            <div class="form-group" style="width:80px">
              <label>图标</label>
              <select v-model="form.icon" class="input">
                <option v-for="ic in iconOptions" :key="ic" :value="ic">{{ ic }}</option>
              </select>
            </div>
            <div class="form-group" style="flex:1">
              <label>描述</label>
              <input v-model="form.description" class="input" placeholder="简要描述模板风格" />
            </div>
          </div>

          <div class="form-group">
            <label>参考来源类型</label>
            <select v-model="form.referenceType" class="input" style="width:150px">
              <option value="TEXT">文本样本</option>
              <option value="IMAGE">图片样本</option>
            </select>
          </div>

          <div v-if="form.referenceType === 'TEXT'" class="form-group">
            <label>参考文本样本</label>
            <textarea v-model="form.referenceSample" class="textarea" rows="5"
              placeholder="粘贴一段你喜欢的风格文案..."></textarea>
            <button class="btn-secondary" :disabled="analyzing" @click="analyzeText">
              {{ analyzing ? '分析中...' : 'AI 分析样本 → 提取风格' }}
            </button>
          </div>

          <div v-else class="form-group">
            <label>参考图片</label>
            <div>
              <button class="btn-secondary" @click="imageInput?.click()">选择图片上传</button>
              <input ref="imageInput" type="file" accept="image/*" style="display:none" @change="onImageSelected" />
            </div>
            <img v-if="form.referenceImage" :src="form.referenceImage" class="ref-image" />
          </div>

          <div class="form-group">
            <label>System Prompt (给AI的风格指令)</label>
            <textarea v-model="form.systemPrompt" class="textarea" rows="4"
              placeholder="描述AI应该如何改写内容..."></textarea>
          </div>

          <div class="form-group" v-if="form.extractedStyle">
            <label>提取的风格特征</label>
            <pre class="style-preview">{{ form.extractedStyle }}</pre>
          </div>

          <div class="form-actions">
            <button class="btn-primary" :disabled="!form.name" @click="saveTemplate">保存模板</button>
            <button class="btn-cancel" @click="showForm = false">取消</button>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<style scoped>
.template-page { padding: 24px; max-width: 900px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; }
.template-grid { display: flex; flex-direction: column; gap: 10px; }
.template-card {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 18px; background: #fff; border-radius: 8px;
  border: 1px solid #e8e8e8;
}
.template-card.preset { border-left: 4px solid #4a90d9; }
.card-icon { font-size: 28px; width: 40px; text-align: center; }
.card-body { flex: 1; }
.card-title { font-weight: 600; font-size: 15px; }
.badge-preset { font-size: 10px; background: #e6f0ff; color: #4a90d9; padding: 1px 6px; border-radius: 3px; margin-left: 6px; }
.card-desc { font-size: 12px; color: #888; margin: 2px 0; }
.card-style { font-size: 11px; color: #aaa; }
.card-meta { font-size: 11px; color: #bbb; }
.card-actions { display: flex; gap: 6px; }
.btn-small { padding: 3px 10px; font-size: 12px; border: 1px solid #ddd; background: #fff; border-radius: 3px; cursor: pointer; }
.btn-danger { border-color: #e8a0a0; color: #c0392b; }
.btn-primary { padding: 8px 18px; background: #4a90d9; color: #fff; border: none; border-radius: 5px; cursor: pointer; font-size: 13px; }
.btn-primary:disabled { background: #ccc; cursor: not-allowed; }
.btn-secondary { padding: 6px 14px; background: #f5f5f5; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-size: 12px; margin-top: 6px; }
.btn-cancel { padding: 8px 18px; background: #fff; border: 1px solid #ddd; border-radius: 5px; cursor: pointer; font-size: 13px; }
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 100;
}
.modal-content {
  background: #fff; border-radius: 12px; padding: 24px; width: 560px; max-height: 80vh; overflow-y: auto;
}
.modal-content h3 { margin: 0 0 16px; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 12px; color: #666; margin-bottom: 4px; font-weight: 500; }
.form-row { display: flex; gap: 12px; }
.input { width: 100%; padding: 7px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; box-sizing: border-box; }
.textarea { width: 100%; padding: 8px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; resize: vertical; box-sizing: border-box; }
.form-actions { display: flex; gap: 10px; margin-top: 18px; }
.ref-image { max-width: 200px; max-height: 150px; margin-top: 8px; border-radius: 4px; border: 1px solid #eee; }
.style-preview { font-size: 11px; background: #f9f9f9; padding: 8px; border-radius: 4px; white-space: pre-wrap; word-break: break-all; max-height: 100px; overflow-y: auto; }
</style>
