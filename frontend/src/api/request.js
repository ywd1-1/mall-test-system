import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { authState, clearAuth } from '../stores/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  if (authState.token) {
    config.headers.Authorization = `Bearer ${authState.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code === 200) {
      return body.data
    }
    const message = body?.message || '请求失败'
    return Promise.reject(new Error(message))
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      clearAuth()
      router.push('/login')
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
