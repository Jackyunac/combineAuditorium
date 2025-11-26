<template>
  <div class="live-room-page">
    <div class="room-container">
      <div class="video-area">
        <video
          ref="videoPlayer"
          class="video-js vjs-big-play-centered"
          controls
          preload="auto"
        ></video>
        <div class="danmu-overlay">
          <div v-for="msg in latestDanmu" :key="`overlay-${msg.id}`" class="overlay-item">
            {{ msg.nickname || `Áî®Êà∑${msg.userId}` }}Ôºö{{ msg.content }}
          </div>
        </div>
      </div>

      <div class="chat-area">
        <div class="chat-header">
          <div>Áõ¥Êí≠ÂºπÂπï / Á§ºÁâ©</div>
          <div class="room-pill">{{ roomCode }}</div>
        </div>
        <div class="chat-messages" ref="chatBox">
          <div v-for="msg in messages" :key="`${msg.type}-${msg.id}`" class="message" :class="msg.type">
            <div class="avatar" v-if="msg.avatar">
              <img :src="msg.avatar" alt="avatar" />
            </div>
            <div class="bubble">
              <div class="meta">
                <span class="user">{{ msg.nickname || `Áî®Êà∑${msg.userId}` }}</span>
                <span class="time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <div v-if="msg.type === 'danmu'" class="content">{{ msg.content }}</div>
              <div v-else class="content gift">
                ÈÄÅÂá∫ {{ msg.giftCount || 1 }} √ó {{ renderGiftName(msg.giftType) }}
                <span v-if="msg.message" class="gift-note">‚Äú{{ msg.message }}‚Ä?/span>
              </div>
            </div>
          </div>
        </div>
        <div class="chat-input">
          <input v-model="danmuInput" placeholder="Âèë‰∏ÄÊù°ÂºπÂπ?.." @keyup.enter="sendDanmuMessage" />
          <button @click="sendDanmuMessage">ÂèëÈÄ?/button>
        </div>
        <div class="gift-panel">
          <div v-for="gift in giftOptions" :key="gift.type" class="gift-card">
            <div class="gift-body">
              <div class="gift-name">{{ gift.label }}</div>
              <div class="gift-count">x{{ gift.count }}</div>
            </div>
            <button class="gift-btn" @click="sendGift(gift)">ÈÄÅÁ§º</button>
          </div>
        </div>
      </div>
    </div>

    <div class="info-area" v-if="roomInfo">
      <div class="room-meta">
        <h1>{{ roomInfo.title }}</h1>
        <div class="host-info">Êàø‰∏ªÔºö{{ roomInfo.userId }}</div>
      </div>
      
      <div class="anchor-controls" v-if="isAnchor">
        <h3>Êé®ÊµÅÂèÇÊï∞</h3>
        <div class="key-box">
          <div class="label">Êé®ÊµÅÂú∞ÂùÄ</div>
          <div class="value">rtmp://localhost:1935/live</div>
        </div>
        <div class="key-box">
          <div class="label">Stream Key</div>
          <div class="value">{{ roomInfo.roomCode }}?secret={{ roomInfo.streamKey }}</div>
        </div>
        <div class="status-indicator">
          Áä∂ÊÄ? <span :class="{ active: roomInfo.status === 1 }">{{ roomInfo.status === 1 ? 'Áõ¥Êí≠‰∏? : 'Êú™ÂºÄÊí? }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLiveRoom, fetchDanmu, fetchGifts, sendDanmu as sendDanmuApi, sendGift as sendGiftApi } from '@/api/live'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'

interface LiveEvent {
  id: number
  type: 'danmu' | 'gift'
  userId: number
  nickname?: string
  avatar?: string
  content?: string
  giftType?: string
  giftCount?: number
  message?: string
  createdAt?: string
}

const route = useRoute()
const roomCode = route.params.code as string
const isAnchor = route.query.isAnchor === 'true'
const roomInfo = ref<any>(null)
const videoPlayer = ref<HTMLVideoElement | null>(null)
const player = ref<any>(null)

const messages = ref<LiveEvent[]>([])
const danmuInput = ref('')
const chatBox = ref<HTMLDivElement | null>(null)
const socket = ref<WebSocket | null>(null)
const giftOptions = [
  { label: 'ÁÇπËµû', type: 'like', count: 1 },
  { label: 'ËçßÂÖâÊ£?, type: 'glowstick', count: 5 },
  { label: 'ÁÅ´ÁÆ≠', type: 'rocket', count: 1 }
]

const latestDanmu = computed(() => messages.value.filter(m => m.type === 'danmu').slice(-3))

onMounted(async () => {
  try {
    const res: any = await getLiveRoom(roomCode)
    roomInfo.value = res
    const hlsUrl = `http://localhost:8080/live/${roomCode}.m3u8`
    initPlayer(hlsUrl)
    await loadHistory()
    connectWebSocket()
  } catch (e: any) {
    console.error(e)
    ElMessage.error(e?.message || 'Âä†ËΩΩÁõ¥Êí≠Èó¥Â§±Ë¥?)
  }
})

const initPlayer = (url: string) => {
  if (!videoPlayer.value) return
  player.value = videojs(videoPlayer.value, {
    sources: [{ src: url, type: 'application/x-mpegURL' }],
    fluid: true,
    autoplay: true,
    controlBar: {
      children: ['playToggle', 'volumePanel', 'fullscreenToggle']
    }
  })
}

const loadHistory = async () => {
  try {
    const [danmuList, giftList]: any = await Promise.all([
      fetchDanmu(roomCode, { limit: 50 }),
      fetchGifts(roomCode, { limit: 30 })
    ])
    const normalized: LiveEvent[] = [
      ...danmuList.map((d: any) => ({ ...d, type: 'danmu' as const })),
      ...giftList.map((g: any) => ({ ...g, type: 'gift' as const }))
    ].sort((a, b) => {
      const timeA = new Date(a.createdAt || '').getTime() || 0
      const timeB = new Date(b.createdAt || '').getTime() || 0
      return timeA - timeB
    })
    messages.value = normalized.slice(-100)
    await nextTick()
    scrollToBottom()
  } catch (e) {
    console.warn('Âä†ËΩΩÂéÜÂè≤ÂºπÂπïÂ§±Ë¥•', e)
  }
}

const connectWebSocket = () => {
  const token = localStorage.getItem('token') || ''
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
  const wsUrl = `${protocol}://${location.host}/ws/live?roomCode=${roomCode}&token=${encodeURIComponent(token)}`
  const ws = new WebSocket(wsUrl)
  socket.value = ws

  ws.onopen = () => {
    console.info('Â∑≤ËøûÊé•ÂºπÂπïÈÄöÈÅì')
  }
  ws.onmessage = (evt) => {
    try {
      const data = JSON.parse(evt.data)
      if (data.type === 'danmu' && data.data) {
        pushMessage({ ...data.data, type: 'danmu' })
      } else if (data.type === 'gift' && data.data) {
        pushMessage({ ...data.data, type: 'gift' })
      } else if (data.type === 'error') {
        ElMessage.error(data.message || 'ÂèëÈÄÅÂ§±Ë¥?)
      }
    } catch (err) {
      console.warn('Ëß£ÊûêÊ∂àÊÅØÂ§±Ë¥•', err)
    }
  }
  ws.onerror = () => {
    ElMessage.warning('…–Œ¥¡¨Ω”µØƒªÕ®µ¿£¨…‘∫Û÷ÿ ‘')
  }
  ws.onclose = () => {
    socket.value = null
  }
}

const pushMessage = async (msg: LiveEvent) => {
  messages.value.push(msg)
  if (messages.value.length > 200) {
    messages.value.shift()
  }
  await nextTick()
  scrollToBottom()
}

const scrollToBottom = () => {
  if (chatBox.value) {
    chatBox.value.scrollTop = chatBox.value.scrollHeight
  }
}

const sendDanmuMessage = async () => {
  const text = danmuInput.value.trim()
  if (!text) return
  if (socket.value && socket.value.readyState === WebSocket.OPEN) {
    socket.value.send(JSON.stringify({ type: 'danmu', content: text }))
  } else {
    try {
      const data: any = await sendDanmuApi(roomCode, text)
      pushMessage({ ...data, type: 'danmu' })
    } catch (e) {
      ElMessage.error('∑¢ÀÕµØƒª ß∞‹')
    }
  }
  danmuInput.value = ''
}

const sendGift = async (gift: { type: string; count: number }) => {
  if (socket.value && socket.value.readyState === WebSocket.OPEN) {
    socket.value.send(JSON.stringify({ type: 'gift', giftType: gift.type, giftCount: gift.count }))
  } else {
    try {
      const data: any = await sendGiftApi(roomCode, { giftType: gift.type, giftCount: gift.count })
      pushMessage({ ...data, type: 'gift' })
    } catch (e) {
      ElMessage.warning('…–Œ¥¡¨Ω”µØƒªÕ®µ¿£¨…‘∫Û÷ÿ ‘')
    }
  }
}

const formatTime = (time?: string) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const renderGiftName = (giftType?: string) => {
  if (!giftType) return 'Á§ºÁâ©'
  const map: Record<string, string> = { like: 'ÁÇπËµû', glowstick: 'ËçßÂÖâÊ£?, rocket: 'ÁÅ´ÁÆ≠' }
  return map[giftType] || giftType
}

onUnmounted(() => {
  if (player.value) player.value.dispose()
  if (socket.value) socket.value.close()
})
</script>

<style scoped>
.live-room-page {
  padding-top: 70px;
  min-height: 100vh;
  background: #0f1115;
  color: #fff;
}

.room-container {
  display: flex;
  height: calc(100vh - 70px);
}

.video-area {
  flex: 1;
  background: #000;
  position: relative;
  display: flex;
  align-items: center;
}

.danmu-overlay {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  pointer-events: none;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
}

.overlay-item {
  background: rgba(0, 0, 0, 0.4);
  padding: 6px 10px;
  border-radius: 12px;
  font-size: 13px;
}

.chat-area {
  width: 360px;
  border-left: 1px solid #1e1f25;
  display: flex;
  flex-direction: column;
  background: #11131a;
}

.chat-header {
  padding: 14px 16px;
  border-bottom: 1px solid #1e1f25;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-pill {
  padding: 4px 8px;
  background: #1c7bf1;
  border-radius: 12px;
  font-size: 12px;
}

.chat-messages {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}

.message {
  display: flex;
  gap: 10px;
  padding: 10px 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
  margin-bottom: 8px;
  border: 1px solid transparent;
}

.message.danmu {
  border-color: rgba(76, 201, 240, 0.2);
}

.message.gift {
  border-color: rgba(250, 82, 82, 0.2);
  background: linear-gradient(90deg, rgba(250, 82, 82, 0.15), rgba(17, 19, 26, 0.6));
}

.avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.bubble {
  flex: 1;
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #aaa;
  margin-bottom: 4px;
}

.user {
  color: #fff;
  font-weight: 600;
}

.content {
  font-size: 14px;
  line-height: 1.5;
}

.content.gift {
  color: #ffd166;
  font-weight: 600;
}

.gift-note {
  color: #ddd;
  margin-left: 4px;
}

.chat-input {
  padding: 12px;
  border-top: 1px solid #1e1f25;
  display: flex;
  gap: 8px;
}

.chat-input input {
  flex: 1;
  background: #1b1f2a;
  border: 1px solid #2a3040;
  padding: 10px 12px;
  color: #fff;
  border-radius: 10px;
  outline: none;
}

.chat-input button {
  background: #1c7bf1;
  border: none;
  color: #fff;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
}

.gift-panel {
  padding: 12px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  border-top: 1px solid #1e1f25;
}

.gift-card {
  background: #1b1f2a;
  border: 1px solid #2a3040;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.gift-body {
  display: flex;
  justify-content: space-between;
}

.gift-name {
  font-weight: 600;
}

.gift-count {
  color: #ccc;
}

.gift-btn {
  background: #fa5252;
  border: none;
  color: #fff;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
}

.info-area {
  padding: 20px;
  border-top: 1px solid #1e1f25;
}

.anchor-controls {
  background: #161922;
  padding: 20px;
  margin-top: 20px;
  border-radius: 8px;
}

.key-box {
  margin-bottom: 10px;
}

.key-box .label {
  font-size: 12px;
  color: #999;
}

.key-box .value {
  font-family: monospace;
  background: #0f1115;
  padding: 8px;
  border-radius: 4px;
  word-break: break-all;
}

.status-indicator span.active {
  color: #46d369;
  font-weight: bold;
}
</style>

