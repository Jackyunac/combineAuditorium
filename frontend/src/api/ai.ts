import request from '@/utils/request'

export const chatWithAI = (message: string) => {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: { message }
  })
}

