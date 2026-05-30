<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { platformApi } from '@/api/platform'

interface UserPlatform {
  id?: number
  platformCode: string
  platformName: string
  loginUrl: string
  editorUrl: string
  isBuiltin: boolean
  isActive: boolean
  loginStatus?: string
  lastLoginAt?: string
}

const platforms = ref<UserPlatform[]>([])
const showAdd = ref(false)
const newPlatform = ref<UserPlatform>(emptyPlatform())

const builtinPlatforms = [
  { code: 'WECHAT', name: '微信公众号', loginUrl: 'https://mp.weixin.qq.com/', editorUrl: 'https://mp.weixin.qq.com/cgi-bin/appmsg?t=media/appmsg_edit&action=edit' },
  { code: 'ZHIHU', name: '知乎', loginUrl: 'https://www.zhihu.com/signin', editorUrl: 'https://zhuanlan.zhihu.com/write' },
  { code: 'BILIBILI', name: 'B站专栏', loginUrl: 'https://passport.bilibili.com/login', editorUrl: 'https://member.bilibili.com/platform/upload/text' },
  { code: 'XHS', name: '小红书', loginUrl: 'https://creator.xiaohongshu.com/login', editorUrl: 'https://creator.xiaohongshu.com/publish/publish' },
  { code: 'TOUTIAO', name: '今日头条', loginUrl: 'https://mp.toutiao.com/login/', editorUrl: 'https://mp.toutiao.com/profile_v4/graphic/publish' },
]

function emptyPlatform(): UserPlatform {
  return { platformCode: '', platformName: '', loginUrl: '', editorUrl: '', isBuiltin: false, isActive: true }
}

async function loadPlatforms() {
  try {
    const res: any = await platformApi.getUserPlatforms()
    platforms.value = res.data || []
  } catch { /* use builtin list as fallback */ }
}

async function addPlatform() {
  if (!newPlatform.value.platformCode || !newPlatform.value.platformName) {
    alert('请填写平台信息'); return
  }
  try {
    await platformApi.addUserPlatform(newPlatform.value)
    showAdd.value = false
    newPlatform.value = emptyPlatform()
    await loadPlatforms()
  } catch (err: any) { alert('添加失败: ' + (err.message || '')) }
}

async function removePlatform(id: number) {
  if (!confirm('确定删除此平台？')) return
  try {
    await platformApi.removeUserPlatform(id)
    await loadPlatforms()
  } catch (err: any) { alert('删除失败: ' + (err.message || '')) }
}

async function triggerLogin(platformCode: string) {
  try {
    const res: any = await platformApi.triggerLogin(platformCode)
    alert('已触发登录: ' + (res.data?.status || '请在桌面弹出的浏览器窗口中完成登录'))
  } catch (err: any) { alert('触发登录失败: ' + (err.message || '')) }
}

function selectBuiltin(code: string) {
  const b = builtinPlatforms.find(p => p.code === code)
  if (b) {
    newPlatform.value = { platformCode: b.code, platformName: b.name,
      loginUrl: b.loginUrl, editorUrl: b.editorUrl, isBuiltin: true, isActive: true }
  }
}

onMounted(loadPlatforms)
</script>

<template>
  <AppLayout>
    <div class="accounts-page">
      <div class="page-header">
        <h2>平台管理</h2>
        <button class="btn-primary" @click="showAdd = true">+ 新增平台</button>
      </div>

      <div class="platform-list">
        <div v-for="p in platforms" :key="p.id" class="platform-card">
          <div class="card-left">
            <span class="status-dot" :class="{
              active: p.loginStatus === 'VALID',
              expired: p.loginStatus === 'EXPIRED',
              never: p.loginStatus !== 'VALID' && p.loginStatus !== 'EXPIRED'
            }"></span>
            <div>
              <div class="card-name">{{ p.platformName }}
                <span v-if="p.isBuiltin" class="badge-builtin">内置</span>
              </div>
              <div class="card-info">
                {{ p.loginStatus === 'VALID' ? '已登录' : p.loginStatus === 'EXPIRED' ? '登录过期' : '未登录' }}
                <span v-if="p.lastLoginAt"> · 上次: {{ p.lastLoginAt?.substring(0, 10) }}</span>
              </div>
            </div>
          </div>
          <div class="card-actions">
            <button class="btn-small" @click="triggerLogin(p.platformCode)">
              {{ p.loginStatus === 'VALID' ? '重新登录' : '登录' }}
            </button>
            <button v-if="!p.isBuiltin" class="btn-small btn-danger" @click="removePlatform(p.id!)">删除</button>
            <span v-else class="hint">系统内置</span>
          </div>
        </div>
      </div>

      <!-- Add Platform Modal -->
      <div v-if="showAdd" class="modal-overlay" @click.self="showAdd = false">
        <div class="modal-content">
          <h3>新增平台</h3>

          <div class="form-group">
            <label>选择系统预置平台</label>
            <select class="input" @change="selectBuiltin(($event.target as HTMLSelectElement).value)">
              <option value="">自定义...</option>
              <option v-for="b in builtinPlatforms" :key="b.code" :value="b.code">{{ b.name }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>平台代码</label>
            <input v-model="newPlatform.platformCode" class="input" placeholder="如: CSDN" />
          </div>
          <div class="form-group">
            <label>平台名称</label>
            <input v-model="newPlatform.platformName" class="input" placeholder="如: CSDN博客" />
          </div>
          <div class="form-group">
            <label>登录页 URL</label>
            <input v-model="newPlatform.loginUrl" class="input" placeholder="https://..." />
          </div>
          <div class="form-group">
            <label>编辑器 URL</label>
            <input v-model="newPlatform.editorUrl" class="input" placeholder="https://..." />
          </div>

          <div class="form-actions">
            <button class="btn-primary" @click="addPlatform">添加平台</button>
            <button class="btn-cancel" @click="showAdd = false">取消</button>
          </div>

          <div class="todo-note">
            TODO: 添加后需完成登录 — 后端将打开本地 Chrome 窗口，请在桌面浏览器中扫码/输入密码完成登录
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<style scoped>
.accounts-page { padding: 24px; max-width: 700px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; }
.platform-list { display: flex; flex-direction: column; gap: 8px; }
.platform-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 18px; background: #fff; border-radius: 8px; border: 1px solid #e8e8e8;
}
.card-left { display: flex; align-items: center; gap: 12px; }
.status-dot { width: 10px; height: 10px; border-radius: 50%; }
.status-dot.active { background: #52c41a; }
.status-dot.expired { background: #faad14; }
.status-dot.never { background: #d9d9d9; }
.card-name { font-weight: 600; font-size: 15px; }
.badge-builtin { font-size: 10px; background: #e6f0ff; color: #4a90d9; padding: 1px 6px; border-radius: 3px; margin-left: 6px; }
.card-info { font-size: 12px; color: #888; margin-top: 2px; }
.card-actions { display: flex; gap: 6px; align-items: center; }
.hint { font-size: 11px; color: #ccc; }
.btn-primary { padding: 8px 18px; background: #4a90d9; color: #fff; border: none; border-radius: 5px; cursor: pointer; font-size: 13px; }
.btn-small { padding: 4px 12px; font-size: 12px; border: 1px solid #ddd; background: #fff; border-radius: 3px; cursor: pointer; }
.btn-danger { border-color: #e8a0a0; color: #c0392b; }
.btn-cancel { padding: 8px 18px; background: #fff; border: 1px solid #ddd; border-radius: 5px; cursor: pointer; font-size: 13px; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-content { background: #fff; border-radius: 12px; padding: 24px; width: 480px; max-height: 80vh; overflow-y: auto; }
.modal-content h3 { margin: 0 0 16px; }
.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 12px; color: #666; margin-bottom: 4px; }
.input { width: 100%; padding: 7px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; box-sizing: border-box; }
.form-actions { display: flex; gap: 10px; margin-top: 16px; }
.todo-note { margin-top: 12px; font-size: 11px; color: #faad14; background: #fffbe6; padding: 8px; border-radius: 4px; }
</style>
