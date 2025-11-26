import request from '@/utils/request'

export interface Comment {
  id: number
  videoId: number
  userId: number
  content: string
  createTime: string
  nickname?: string
  avatar?: string
}

export function addComment(data: { videoId: number, content: string }) {
  return request({
    url: '/comments',
    method: 'post',
    data
  })
}

export function listComments(videoId: number) {
  return request({
    url: '/comments',
    method: 'get',
    params: { videoId }
  })
}

export function deleteComment(id: number) {
  return request({
    url: `/comments/${id}`,
    method: 'delete'
  })
}

