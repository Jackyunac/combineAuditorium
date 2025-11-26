import request from '@/utils/request'

export function getMyRoom() {
  return request({
    url: '/live/my-room',
    method: 'get'
  })
}

export function getLiveRooms() {
  return request({
    url: '/live/list',
    method: 'get'
  })
}

export function getLiveRoom(code: string) {
  return request({
    url: `/live/room/${code}`,
    method: 'get'
  })
}

export function updateRoom(data: any) {
  return request({
    url: '/live/update',
    method: 'post',
    data
  })
}

export function fetchDanmu(roomCode: string, params: { sinceId?: number; limit?: number } = {}) {
  return request({
    url: `/live/${roomCode}/danmu`,
    method: 'get',
    params
  })
}

export function fetchGifts(roomCode: string, params: { sinceId?: number; limit?: number } = {}) {
  return request({
    url: `/live/${roomCode}/gifts`,
    method: 'get',
    params
  })
}

export function sendDanmu(roomCode: string, content: string) {
  return request({
    url: `/live/${roomCode}/danmu`,
    method: 'post',
    data: { content }
  })
}

export function sendGift(roomCode: string, data: { giftType: string; giftCount?: number; message?: string }) {
  return request({
    url: `/live/${roomCode}/gifts`,
    method: 'post',
    data
  })
}
