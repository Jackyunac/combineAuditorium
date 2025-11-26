import request from '@/utils/request'

export const chatWithAI = (message: string) => {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: { message }
  })
}

export const getAIConfig = () => {
  return request({
    url: '/ai/config',
    method: 'get'
  })
}

export const updateAIConfig = (data: { temperature?: number; topK?: number; knowledgeLimit?: number }) => {
  return request({
    url: '/ai/config',
    method: 'post',
    data
  })
}

