import http from './request'
import type { Content, ContentQuery, PageResult } from '@/types/content'

export const contentApi = {
  save(data: { id?: number; title: string; tiptapJson: string; coverImage?: string }) {
    return http.post('/content', data)
  },
  getById(id: number) {
    return http.get(`/content/${id}`)
  },
  list(params: ContentQuery) {
    return http.get('/content', { params })
  },
  delete(id: number) {
    return http.delete(`/content/${id}`)
  }
}
