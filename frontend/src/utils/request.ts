import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 Axios 实例
const service = axios.create({
  baseURL: '/api', // Vite 代理会自动转发到 http://localhost:8080
  timeout: 600000 // 请求超时时间 600秒，AI 生成需要较长时间
})

/**
 * 请求拦截器
 * 在发送请求前做统一处理，如添加 Token 到 Header
 */
service.interceptors.request.use(
  (config) => {
    // 从 LocalStorage 获取 Token
    const token = localStorage.getItem('token')
    if (token) {
      // 如果存在 Token，则添加到请求头 Authorization 字段
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 统一处理接口响应，如判断业务状态码、处理错误提示
 */
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 业务状态码 200 表示成功
    if (res.code !== 200) {
      // 业务失败，弹出错误提示
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    // 返回业务数据部分
    return res.data
  },
  (error) => {
    // 网络错误或 HTTP 状态码非 200
    ElMessage.error(error.message || 'Network Error')
    return Promise.reject(error)
  }
)

export default service
