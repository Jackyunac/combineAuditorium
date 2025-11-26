import request from '@/utils/request'

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function updateAvatar(data: FormData) {
  return request({
    url: '/user/avatar',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}
