import request from '@/utils/request'

/**
 * 登录接口
 * @param data { username, password }
 */
export function login(data: any) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 注册接口
 * @param data { username, password, confirmPassword }
 */
export function register(data: any) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}
