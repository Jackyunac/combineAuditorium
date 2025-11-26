import request from '@/utils/request'

export function listPosts(limit = 50) {
  return request({
    url: '/forum/posts',
    method: 'get',
    params: { limit }
  })
}

export function getPost(id: number) {
  return request({
    url: `/forum/posts/${id}`,
    method: 'get'
  })
}

export function createPost(data: { title: string; content: string }) {
  return request({
    url: '/forum/posts',
    method: 'post',
    data
  })
}

export function deletePost(id: number) {
  return request({
    url: `/forum/posts/${id}`,
    method: 'delete'
  })
}

export function addComment(postId: number, content: string) {
  return request({
    url: `/forum/posts/${postId}/comments`,
    method: 'post',
    data: { content }
  })
}

export function listComments(postId: number) {
  return request({
    url: `/forum/posts/${postId}/comments`,
    method: 'get'
  })
}

export function deleteComment(id: number) {
  return request({
    url: `/forum/comments/${id}`,
    method: 'delete'
  })
}
