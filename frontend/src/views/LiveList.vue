<template>
  <div class="live-list-page">
    <div class="page-header">
      <h1>Live Streaming</h1>
      <button class="btn-go-live" @click="handleStartLive">Go Live</button>
    </div>

    <div class="live-grid" v-if="rooms.length > 0">
      <div class="live-card" v-for="room in rooms" :key="room.id" @click="enterRoom(room.roomCode)">
        <div class="thumbnail">
          <img :src="room.coverUrl || `https://picsum.photos/400/225?random=${room.id}`" />
          <div class="live-badge">LIVE</div>
          <div class="viewer-count">1.2k viewers</div>
        </div>
        <div class="room-info">
          <h3>{{ room.title }}</h3>
          <p>Host: {{ room.userId }}</p>
        </div>
      </div>
    </div>
    
    <div class="empty-state" v-else>
      <p>No live streams at the moment.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getLiveRooms, getMyRoom } from '@/api/live'

const router = useRouter()
const rooms = ref<any[]>([])

onMounted(async () => {
  try {
    const res: any = await getLiveRooms()
    rooms.value = res
  } catch (e) {
    console.error(e)
  }
})

const handleStartLive = async () => {
  // 打开新标签页进行直播设置
  const route = router.resolve({ name: 'live-dashboard' })
  window.open(route.href, '_blank')
}

const enterRoom = (code: string) => {
  router.push(`/live/${code}`)
}
</script>

<style scoped>
.live-list-page {
  padding: 100px 60px 40px;
  background: #141414;
  min-height: 100vh;
  color: #fff;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

h1 {
  font-size: 32px;
  font-weight: 500;
}

.btn-go-live {
  background: #e50914;
  color: #fff;
  border: none;
  padding: 10px 20px;
  font-weight: bold;
  cursor: pointer;
  border-radius: 2px;
}

.live-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.live-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.live-card:hover {
  transform: scale(1.05);
  z-index: 10;
}

.thumbnail {
  position: relative;
  aspect-ratio: 16/9;
  border-radius: 4px;
  overflow: hidden;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.live-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #e50914;
  color: white;
  padding: 2px 6px;
  font-size: 12px;
  font-weight: bold;
  border-radius: 2px;
}

.viewer-count {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(0,0,0,0.6);
  padding: 2px 6px;
  font-size: 12px;
  border-radius: 2px;
}

.room-info {
  margin-top: 10px;
}

.room-info h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.room-info p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.empty-state {
  text-align: center;
  color: #666;
  margin-top: 100px;
}
</style>
