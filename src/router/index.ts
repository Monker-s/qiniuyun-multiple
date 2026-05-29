import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'workspace',
      component: () => import('@/views/WorkspaceView.vue'),
      meta: { title: '工作台' }
    },
    {
      path: '/editor/:id?',
      name: 'editor',
      component: () => import('@/views/WorkspaceView.vue'),
      meta: { title: '内容编辑' }
    },
    {
      path: '/history',
      name: 'history',
      component: () => import('@/views/HistoryView.vue'),
      meta: { title: '发布历史' }
    },
    {
      path: '/accounts',
      name: 'accounts',
      component: () => import('@/views/AccountsView.vue'),
      meta: { title: '平台管理' }
    },
    {
      path: '/templates',
      name: 'templates',
      component: () => import('@/views/TemplateManageView.vue'),
      meta: { title: '模板管理' }
    }
  ]
})

export default router
