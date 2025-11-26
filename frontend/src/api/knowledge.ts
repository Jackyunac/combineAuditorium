import request from '@/utils/request'

export function searchKnowledge(params: { q?: string; tag?: string; limit?: number }) {
  return request({
    url: '/knowledge',
    method: 'get',
    params
  })
}

export function createKnowledge(data: { title: string; content: string; tags?: string; source?: string }) {
  return request({
    url: '/knowledge',
    method: 'post',
    data
  })
}

export function deleteKnowledge(id: number) {
  return request({
    url: `/knowledge/${id}`,
    method: 'delete'
  })
}
