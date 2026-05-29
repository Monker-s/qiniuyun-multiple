import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'

const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

http.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    if (data.code !== 200) {
      console.error('API Error:', data.msg)
      return Promise.reject(new Error(data.msg || 'Request failed'))
    }
    return data
  },
  (error) => {
    console.error('Network Error:', error.message)
    return Promise.reject(error)
  }
)

export default http
