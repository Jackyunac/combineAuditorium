import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 用户状态管理 Store (Pinia)
 * 负责存储当前用户的 Token 和基本信息
 */
export const useUserStore = defineStore('user', () => {
  // 从 LocalStorage 初始化 Token，实现持久化
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  /**
   * 设置 Token 并保存到 LocalStorage
   * @param newToken 后端返回的 JWT Token
   */
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 退出登录，清除 Token 和用户信息
   */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return { token, userInfo, setToken, logout }
})
