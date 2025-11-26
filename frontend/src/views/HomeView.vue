<template>
  <div class="home-view">
    <!-- Hero Banner Carousel -->
    <el-carousel 
      v-if="hotVideos.length > 0"
      height="80vh" 
      indicator-position="none" 
      arrow="always"
      :interval="5000"
      class="hero-carousel"
    >
      <el-carousel-item v-for="video in hotVideos" :key="video.id">
        <div class="hero-banner" :style="{ backgroundImage: `url(${video.coverUrl})` }">
          <div class="hero-mask">
            <div class="hero-content">
              <h1 class="hero-title">{{ video.title }}</h1>
              <p class="hero-desc">{{ video.description }}</p>
              <div class="hero-actions">
                <button class="btn btn-play" @click="playVideo(video.id)">
                  <el-icon><CaretRight /></el-icon> 播放
                </button>
                <button class="btn btn-info" @click="playVideo(video.id)">
                  <el-icon><InfoFilled /></el-icon> 更多信息
                </button>
              </div>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- Fallback Hero if no hot videos -->
    <div v-else class="hero-banner" :style="{ backgroundImage: `url(${featuredVideo.coverUrl || 'https://image.tmdb.org/t/p/original/x2LSRK2Cm7MZhjluni1qNbBro3F.jpg'})` }">
      <div class="hero-mask">
        <div class="hero-content">
          <h1 class="hero-title">{{ featuredVideo.title || '欢迎来到 Lips' }}</h1>
          <p class="hero-desc">{{ featuredVideo.description || '探索海量高清视频内容' }}</p>
          <div class="hero-actions" v-if="featuredVideo.id">
            <button class="btn btn-play" @click="playVideo(featuredVideo.id)">
              <el-icon><CaretRight /></el-icon> 播放
            </button>
            <button class="btn btn-info">
              <el-icon><InfoFilled /></el-icon> 更多信息
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Horizontal Lists -->
    <div class="rows-container">
      <div class="row-section">
        <h3>热门推荐</h3>
        <div class="video-row">
          <div class="video-card" v-for="video in hotVideos" :key="video.id" @click="playVideo(video.id)">
            <img :src="video.coverUrl || `https://picsum.photos/300/170?random=${video.id}`" />
          </div>
        </div>
      </div>

      <div class="row-section">
        <h3>最新上传</h3>
        <div class="video-row">
          <div class="video-card" v-for="video in recentVideos" :key="video.id" @click="playVideo(video.id)">
            <img :src="video.coverUrl || `https://picsum.photos/300/170?random=${video.id}`" />
          </div>
        </div>
      </div>

      <div class="row-section">
        <h3>正在直播</h3>
        <div class="video-row">
          <div class="video-card live" v-for="room in liveRooms" :key="room.id" @click="enterRoom(room.roomCode)">
            <img :src="room.coverUrl || `https://picsum.photos/300/170?random=live${room.id}`" />
            <div class="live-badge">LIVE</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { CaretRight, InfoFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getVideos } from '@/api/video'
import { getLiveRooms } from '@/api/live'

const router = useRouter()
const hotVideos = ref<any[]>([])
const recentVideos = ref<any[]>([])
const liveRooms = ref<any[]>([])
const featuredVideo = ref<any>({})

onMounted(async () => {
  try {
    // Fetch Hot Videos
    const hotRes: any = await getVideos({ isHot: true })
    hotVideos.value = hotRes

    // Fetch All Videos (for recent)
    const vRes: any = await getVideos({ sort: 'newest' })
    recentVideos.value = vRes
    
    // Fallback featured if no hot videos
    if (vRes.length > 0) {
      featuredVideo.value = vRes[0]
    }

    const lRes: any = await getLiveRooms()
    liveRooms.value = lRes
  } catch (e) {
    console.error(e)
  }
})

const playVideo = (id: number) => {
  if(id) router.push(`/videos/${id}`)
}

const enterRoom = (code: string) => {
  router.push(`/live/${code}`)
}
</script>

<style scoped>
.hero-carousel {
  /* Ensure carousel takes full space */
}

.hero-banner {
  height: 80vh;
  background-size: cover;
  background-position: center;
  position: relative;
}

.hero-mask {
  width: 100%;
  height: 100%;
  background: linear-gradient(to top, #141414 10%, transparent 50%);
  display: flex;
  align-items: center;
}

.hero-content {
  padding-left: 60px;
  max-width: 600px;
  margin-top: 100px;
}

.hero-title {
  font-size: 60px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.5);
  color: white;
}

.hero-desc {
  font-size: 18px;
  line-height: 1.5;
  margin-bottom: 30px;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.5);
  color: #ddd;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hero-actions {
  display: flex;
  gap: 15px;
}

.btn {
  border: none;
  border-radius: 4px;
  padding: 10px 24px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: opacity 0.2s;
}

.btn:hover {
  opacity: 0.8;
}

.btn-play {
  background-color: white;
  color: black;
}

.btn-info {
  background-color: rgba(109, 109, 110, 0.7);
  color: white;
}

.rows-container {
  margin-top: -100px;
  position: relative;
  z-index: 10;
  padding-bottom: 50px;
}

.row-section {
  margin-bottom: 40px;
  padding-left: 60px;
}

.row-section h3 {
  margin-bottom: 15px;
  font-size: 20px;
  font-weight: 500;
  color: #e5e5e5;
}

.video-row {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-right: 60px;
  padding-bottom: 20px;
}

.video-row::-webkit-scrollbar {
  display: none; /* Hide scrollbar */
}

.video-card {
  min-width: 250px;
  height: 140px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s;
  position: relative;
}

.video-card:hover {
  transform: scale(1.1);
  z-index: 100;
}

.video-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.live-badge {
  position: absolute;
  top: 5px;
  right: 5px;
  background: #e50914;
  color: white;
  padding: 2px 6px;
  font-size: 10px;
  border-radius: 2px;
  font-weight: bold;
}

/* Element Plus Carousel Override */
:deep(.el-carousel__arrow) {
  background-color: rgba(0,0,0,0.3);
  font-size: 24px;
}
:deep(.el-carousel__arrow:hover) {
  background-color: rgba(0,0,0,0.6);
}
</style>
