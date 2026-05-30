import http from './request'

export const platformApi = {
  convert(data: { tiptapJson: string; platformCode: string; title: string }) {
    return http.post('/platform/convert', data)
  },
  getRules(platformCode: string) {
    return http.get(`/platform/${platformCode}/rules`)
  },
  getVideoLimits() {
    return http.get('/platform/video-limits')
  },
  publish(data: { contentId: number; platformCode: string; title: string; adaptedHtml: string }) {
    return http.post('/platform/publish', data)
  },
  batchPublish(data: { contentId: number; platforms: Array<{ platformCode: string; title: string; adaptedHtml: string }> }) {
    return http.post('/platform/batch-publish', data)
  },
  getPublishProgress(taskId: string) {
    return http.get(`/platform/publish/${taskId}/progress`)
  },
  retract(publishLogId: number) {
    return http.post(`/publish/${publishLogId}/retract`)
  },
  getLoginStatus(platformCode: string) {
    return http.get(`/platform/${platformCode}/login-status`)
  },
  triggerLogin(platformCode: string) {
    return http.post(`/platform/${platformCode}/login`)
  },
  getUserPlatforms() {
    return http.get('/user/platforms')
  },
  addUserPlatform(data: any) {
    return http.post('/user/platforms', data)
  },
  removeUserPlatform(id: number) {
    return http.delete(`/user/platforms/${id}`)
  }
}
