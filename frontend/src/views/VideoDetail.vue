<template>
  <div class="watch-page">
    <div class="main-content">
      <div class="left-column">
        <div class="player-wrapper">
          <video
            ref="videoPlayer"
            class="video-js vjs-big-play-centered"
            controls
            preload="auto"
          ></video>
        </div>

        <div class="video-info-card" v-if="videoInfo">
          <h1 class="video-title">{{ videoInfo.title }}</h1>
          <div class="video-meta">
            <span class="badge type" v-if="videoInfo.videoType">{{ videoInfo.videoType }}</span>
            <span class="meta-item">{{ videoInfo.releaseYear || '未知年份' }}</span>
            <span class="meta-item">{{ videoInfo.region || '未知地区' }}</span>
            <span class="meta-item">{{ videoInfo.category }}</span>
          </div>
          
          <div class="action-bar">
            <div class="stat-item">
              <el-icon><View /></el-icon> 
              <span>{{ videoInfo.viewCount || 0 }} 播放</span>
            </div>
            <div class="stat-item">
              <el-icon><ChatDotSquare /></el-icon>
              <span>{{ comments.length }} 评论</span>
            </div>
            <div class="action-buttons">
              <el-button type="primary" text bg circle><el-icon><Star /></el-icon></el-button>
              <el-button type="primary" text bg circle><el-icon><Share /></el-icon></el-button>
            </div>
          </div>

          <div class="description-box">
            <p>{{ videoInfo.description || '暂无简介' }}</p>
          </div>
        </div>

        <div class="tabs-section">
          <div class="tab-header">
            <span :class="{ active: activeTab === 'detail' }" @click="activeTab = 'detail'">简介</span>
            <span :class="{ active: activeTab === 'comment' }" @click="activeTab = 'comment'">评论 ({{ comments.length }})</span>
          </div>

          <div class="tab-content" v-if="activeTab === 'detail'">
            <div class="detail-content" v-if="videoInfo">
              <p><strong>导演：</strong>暂无数据</p>
              <p><strong>主演：</strong>暂无数据</p>
              <p><strong>剧情：</strong>{{ videoInfo.description }}</p>
            </div>
          </div>

          <div class="tab-content" v-if="activeTab === 'comment'">
            <div class="comment-input">
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="2"
                placeholder="发一条友善的评论..."
              />
              <div class="post-btn-wrapper">
                <el-button type="primary" size="small" @click="submitComment" :disabled="!newComment.trim()">发布</el-button>
              </div>
            </div>

            <div class="comment-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <img :src="comment.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" class="avatar" />
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="username">{{ comment.nickname || 'User' }}</span>
                    <span class="time">{{ formatDate(comment.createTime) }}</span>
                  </div>
                  <p>{{ comment.content }}</p>
                </div>
                <el-icon 
                  v-if="userStore.userInfo?.id === comment.userId || userStore.userInfo?.role === 'SYSTEM'" 
                  class="delete-icon"
                  @click="handleDeleteComment(comment.id)"
                ><Delete /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-column">
        <div class="sidebar-section">
          <h3>相关推荐</h3>
          <div class="rec-list">
            <div v-for="rec in relatedVideos" :key="rec.id" class="rec-item" @click="playVideo(rec.id)">
              <div class="rec-cover">
                <img :src="rec.coverUrl || `https://picsum.photos/300/170?random=${rec.id}`" />
                <span class="rec-duration">{{ formatDuration(rec.duration) }}</span>
              </div>
              <div class="rec-info">
                <div class="rec-title">{{ rec.title }}</div>
                <div class="rec-meta">{{ rec.releaseYear }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getVideoDetail, getVideos } from '@/api/video'
import { listComments, addComment, deleteComment, type Comment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, View, ChatDotSquare, Star, Share, Delete } from '@element-plus/icons-vue'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const videoInfo = ref<any>(null)
const videoPlayer = ref<HTMLVideoElement | null>(null)
const player = ref<any>(null)
const comments = ref<Comment[]>([])
const relatedVideos = ref<any[]>([])
const newComment = ref('')
const activeTab = ref('comment')

const loadData = async (id: number) => {
  try {
    // 1. Get Detail
    const res: any = await getVideoDetail(id)
    videoInfo.value = res
    
    // 2. Init Player
    if (res.videoUrl) {
      initPlayer(res.videoUrl, res.coverUrl)
    }
    
    // 3. Fetch Comments
    fetchComments(id)
    
    // 4. Fetch Related
    fetchRelated(res.category)
  } catch (e) {
    console.error(e)
  }
}

const fetchRelated = async (category: string) => {
  try {
    const res: any = await getVideos({ category: category || '' })
    // Filter out current video
    relatedVideos.value = res.filter((v: any) => v.id !== videoInfo.value.id).slice(0, 6)
  } catch (e) { console.error(e) }
}

const fetchComments = async (videoId: number) => {
  try {
    const res: any = await listComments(videoId)
    comments.value = res
  } catch (e) { console.error(e) }
}

const initPlayer = (url: string, poster: string) => {
  if (player.value) {
    player.value.dispose()
    player.value = null
  }
  
  if (!videoPlayer.value) return
  
  player.value = videojs(videoPlayer.value, {
    sources: [{
      src: url,
      type: url.endsWith('.m3u8') ? 'application/x-mpegURL' : 'video/mp4'
    }],
    poster: poster,
    fluid: true,
    autoplay: false,
    controls: true
  })
}

const playVideo = (id: number) => {
  router.push(`/videos/${id}`)
}

// Watch route change to reload data (when clicking related video)
watch(() => route.params.id, (newId) => {
  if (newId) loadData(Number(newId))
})

onMounted(() => {
  loadData(Number(route.params.id))
})

onUnmounted(() => {
  if (player.value) player.value.dispose()
})

const formatDuration = (s: number) => {
  if (!s) return '0:00'
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${sec.toString().padStart(2, '0')}`
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString()
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await addComment({ videoId: videoInfo.value.id, content: newComment.value })
    ElMessage.success('评论已发布')
    newComment.value = ''
    fetchComments(videoInfo.value.id)
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

const handleDeleteComment = async (id: number) => {
  try {
    await deleteComment(id)
    ElMessage.success('已删除')
    fetchComments(videoInfo.value.id)
  } catch (e) {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.watch-page {
  padding-top: 80px;
  min-height: 100vh;
  background: #141414;
  color: #e5e5e5;
  display: flex;
  justify-content: center;
}

.main-content {
  display: flex;
  width: 100%;
  max-width: 1400px;
  gap: 20px;
  padding: 0 20px;
}

.left-column {
  flex: 1;
  min-width: 0; /* prevent overflow */
}

.right-column {
  width: 320px;
  flex-shrink: 0;
}

/* Player */
.player-wrapper {
  width: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.5);
}

/* Info Card */
.video-info-card {
  background: #1f1f1f;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.video-title {
  font-size: 24px;
  color: #fff;
  margin-bottom: 10px;
}

.video-meta {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #999;
  margin-bottom: 15px;
  align-items: center;
}

.badge.type {
  background: #e50914;
  color: #fff;
  padding: 2px 6px;
  border-radius: 2px;
  font-size: 12px;
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 15px;
  border-bottom: 1px solid #333;
  margin-bottom: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #ccc;
  margin-right: 20px;
}

.description-box {
  font-size: 14px;
  line-height: 1.6;
  color: #ccc;
}

/* Tabs */
.tabs-section {
  background: #1f1f1f;
  padding: 20px;
  border-radius: 8px;
}

.tab-header {
  display: flex;
  gap: 30px;
  border-bottom: 1px solid #333;
  margin-bottom: 20px;
}

.tab-header span {
  padding-bottom: 10px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  color: #999;
  border-bottom: 2px solid transparent;
}

.tab-header span.active {
  color: #e50914;
  border-bottom-color: #e50914;
}

/* Comments */
.comment-input {
  margin-bottom: 30px;
}

.post-btn-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.comment-item {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  border-bottom: 1px solid #333;
  padding-bottom: 20px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  gap: 10px;
  margin-bottom: 5px;
  align-items: baseline;
}

.username {
  font-weight: bold;
  color: #fff;
}

.time {
  font-size: 12px;
  color: #666;
}

.delete-icon {
  cursor: pointer;
  color: #666;
  margin-left: 10px;
}

.delete-icon:hover {
  color: #e50914;
}

/* Right Sidebar */
.sidebar-section {
  background: #1f1f1f;
  padding: 15px;
  border-radius: 8px;
}

.sidebar-section h3 {
  font-size: 16px;
  margin-bottom: 15px;
  color: #fff;
  border-left: 3px solid #e50914;
  padding-left: 10px;
}

.rec-item {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  cursor: pointer;
}

.rec-item:hover .rec-title {
  color: #e50914;
}

.rec-cover {
  position: relative;
  width: 120px;
  height: 70px;
  flex-shrink: 0;
}

.rec-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.rec-duration {
  position: absolute;
  bottom: 2px;
  right: 4px;
  background: rgba(0,0,0,0.7);
  color: #fff;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
}

.rec-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.rec-title {
  font-size: 13px;
  color: #e5e5e5;
  line-height: 1.4;
  margin-bottom: 5px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.rec-meta {
  font-size: 12px;
  color: #666;
}
</style>

