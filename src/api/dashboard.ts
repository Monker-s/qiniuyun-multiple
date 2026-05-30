import http from './request'

export const dashboardApi = {
  getStats() {
    return http.get('/dashboard/stats')
  },
  getHistory(params: { page?: number; size?: number; platform?: string; status?: string }) {
    return http.get('/dashboard/history', { params })
  },
  retract(publishLogId: number) {
    return http.post(`/publish/${publishLogId}/retract`)
  }
}
