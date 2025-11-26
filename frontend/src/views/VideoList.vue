<template>
  <div class="video-list-page">
    <div class="filters-container">
      <!-- Sort Order -->
      <div class="filter-row">
        <span class="filter-label">排序:</span>
        <div class="filter-options">
          <span :class="{ active: filters.sort === 'hottest' }" @click="setFilter('sort', 'hottest')">最热</span>
          <span :class="{ active: filters.sort === 'newest' }" @click="setFilter('sort', 'newest')">最新</span>
          <span :class="{ active: filters.sort === 'upcoming' }" @click="setFilter('sort', 'upcoming')">即将上线</span>
        </div>
      </div>

      <!-- Video Type -->
      <div class="filter-row">
        <span class="filter-label">类型:</span>
        <div class="filter-options">
          <span :class="{ active: filters.videoType === '' }" @click="setFilter('videoType', '')">全部</span>
          <span v-for="type in options.videoTypes" :key="type" 
                :class="{ active: filters.videoType === type }" @click="setFilter('videoType', type)">
            {{ type }}
          </span>
        </div>
      </div>

      <!-- Region -->
      <div class="filter-row">
        <span class="filter-label">地区:</span>
        <div class="filter-options">
          <span :class="{ active: filters.region === '' }" @click="setFilter('region', '')">全部</span>
          <span v-for="region in options.regions" :key="region" 
                :class="{ active: filters.region === region }" @click="setFilter('region', region)">
            {{ region }}
          </span>
        </div>
      </div>

      <!-- Genre (Category) -->
      <div class="filter-row">
        <span class="filter-label">风格:</span>
        <div class="filter-options">
          <span :class="{ active: filters.category === '' }" @click="setFilter('category', '')">全部</span>
          <span v-for="genre in options.genres" :key="genre" 
                :class="{ active: filters.category === genre }" @click="setFilter('category', genre)">
            {{ genre }}
          </span>
        </div>
      </div>

      <!-- Year -->
      <div class="filter-row">
        <span class="filter-label">年份:</span>
        <div class="filter-options">
          <span :class="{ active: filters.year === '' }" @click="setFilter('year', '')">全部</span>
          <span v-for="year in options.years" :key="year" 
                :class="{ active: filters.year === year }" @click="setFilter('year', year)">
            {{ year }}
          </span>
        </div>
      </div>
    </div>

    <div class="video-grid" v-if="videos.length > 0">
      <div class="video-card" v-for="video in videos" :key="video.id" @click="playVideo(video.id)">
        <img :src="video.coverUrl || `https://picsum.photos/300/170?random=${video.id}`" />
        <div class="hover-info">
          <h4>{{ video.title }}</h4>
          <div class="meta">
            <span class="match">98% 匹配</span>
            <span class="age">{{ video.rating || 'G' }}</span>
            <span>{{ formatDuration(video.duration) }}</span>
          </div>
          <div class="tags">
            {{ video.category || '常规' }}
            <span v-if="video.videoType"> • {{ video.videoType }}</span>
            <span v-if="video.region"> • {{ video.region }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="empty-state" v-else>
      <p>暂无符合条件的视频。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getVideos } from '@/api/video'

const router = useRouter()
const route = useRoute()
const videos = ref<any[]>([])

const filters = reactive({
  sort: 'newest',
  videoType: '',
  region: '',
  category: '',
  year: '' as string | number
})

const options = {
  videoTypes: ['电影', '电视剧', '综艺', '纪录片', '动漫'],
  regions: ['美国', '英国', '韩国', '日本', '泰国', '内地', '中国香港', '中国台湾', '其他'],
  genres: ['剧情', '喜剧', '动作', '冒险', '爱情', '动画', '歌舞', '治愈', '科幻', '奇幻', '悬疑', '惊悚', '恐怖', '犯罪', '历史', '战争', '灾难', '美食', '真人秀', '脱口秀'],
  years: [2025, 2024, 2023, 2022, 2021, 2020, 2019, 2018]
}

const fetchVideos = async () => {
  try {
    const params: any = { ...filters }
    if (route.query.q) params.q = route.query.q
    // Convert empty strings to undefined or handle in backend (backend handles empty strings by ignoring)
    
    const res: any = await getVideos(params)
    videos.value = res
  } catch (e) { console.error(e) }
}

const setFilter = (key: string, value: any) => {
  (filters as any)[key] = value
  fetchVideos()
}

// Watch for route query changes (search)
watch(() => route.query.q, () => {
  fetchVideos()
})

onMounted(() => {
  if (route.query.category) filters.category = route.query.category as string
  fetchVideos()
})

const playVideo = (id: number) => router.push(`/videos/${id}`)

const formatDuration = (s: number) => {
  if (!s) return '0m'
  const m = Math.floor(s / 60)
  return `${m}m`
}
</script>

<style scoped>
.video-list-page {
  padding: 80px 40px;
  min-height: 100vh;
  background: #141414;
  color: #fff;
}

.filters-container {
  margin-bottom: 40px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  background: rgba(0,0,0,0.3);
  padding: 20px;
  border-radius: 8px;
}

.filter-row {
  display: flex;
  align-items: center;
}

.filter-label {
  width: 80px;
  color: #777;
  font-size: 14px;
  font-weight: bold;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  flex: 1;
}

.filter-options span {
  font-size: 14px;
  color: #b3b3b3;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.filter-options span:hover {
  color: #fff;
}

.filter-options span.active {
  color: #409eff; /* Blue highlight like the screenshot */
  background: rgba(64, 158, 255, 0.1);
  font-weight: bold;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 15px;
  row-gap: 40px;
}

.video-card {
  position: relative;
  aspect-ratio: 16/9;
  cursor: pointer;
  transition: z-index 0.3s, transform 0.3s;
  border-radius: 4px;
}

.video-card:hover {
  transform: scale(1.1);
  z-index: 100;
  box-shadow: 0 4px 10px rgba(0,0,0,0.5);
}

.video-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.hover-info {
  display: none;
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: #181818;
  padding: 10px;
  border-bottom-left-radius: 4px;
  border-bottom-right-radius: 4px;
  box-sizing: border-box;
  box-shadow: 0 4px 10px rgba(0,0,0,0.5);
  z-index: 101;
}

.video-card:hover .hover-info {
  display: block;
}

.hover-info h4 {
  margin: 0 0 5px 0;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta {
  display: flex;
  gap: 8px;
  font-size: 11px;
  margin-bottom: 5px;
  align-items: center;
}

.match {
  color: #46d369;
  font-weight: bold;
}

.age {
  border: 1px solid #999;
  padding: 0 2px;
  font-size: 10px;
  color: #ccc;
}

.tags {
  font-size: 11px;
  color: #777;
}

.empty-state {
  text-align: center;
  margin-top: 100px;
  color: #666;
  font-size: 18px;
}
</style>
