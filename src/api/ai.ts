import http from './request'

export const aiApi = {
  getTemplates() {
    return http.get('/templates')
  },
  createTemplate(data: any) {
    return http.post('/templates', data)
  },
  updateTemplate(id: number, data: any) {
    return http.put(`/templates/${id}`, data)
  },
  deleteTemplate(id: number) {
    return http.delete(`/templates/${id}`)
  },
  analyzeText(text: string) {
    return http.post('/templates/analyze-text', { text })
  },
  analyzeImage(imageUrl: string) {
    return http.post('/templates/analyze-image', { imageUrl })
  },
  applyTemplate(data: { contentId: number; templateId: number; targetPlatformCode: string }) {
    return http.post('/ai/apply-template', data)
  },
  customAdapt(data: { contentId: number; targetPlatformCode: string; systemPrompt: string; userPrompt: string }) {
    return http.post('/ai/custom-adapt', data)
  }
}
