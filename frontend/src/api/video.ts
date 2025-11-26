import request from '@/utils/request'

export function uploadVideo(data: FormData) {
  return request({
    url: '/videos/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

export function getVideos(params?: any) {
  return request({
    url: '/videos',
    method: 'get',
    params
  })
}

export function getVideoDetail(id: number) {
  return request({
    url: `/videos/${id}`,
    method: 'get'
  })
}

// Admin APIs
export function getAdminVideos() {
  return request({
    url: '/videos/admin/list',
    method: 'get'
  })
}

export function updateVideo(id: number, data: any) {
  return request({
    url: `/videos/${id}`,
    method: 'put',
    data
  })
}

export function deleteVideo(id: number) {
  return request({
    url: `/videos/${id}`,
    method: 'delete'
  })
}