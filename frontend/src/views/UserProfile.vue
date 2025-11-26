<template>
  <div class="profile-page">
    <div class="profile-container">
      <h1>Edit Profile</h1>
      <hr />
      
      <div class="profile-content">
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="triggerFileInput">
            <img :src="userInfo.avatar || defaultAvatar" alt="Profile" />
            <div class="avatar-overlay">
              <el-icon><Edit /></el-icon>
            </div>
          </div>
          <input 
            type="file" 
            ref="fileInput" 
            style="display: none" 
            accept="image/*"
            @change="handleFileChange"
          />
        </div>

        <div class="info-section">
          <div class="form-group">
            <label>Username</label>
            <input type="text" v-model="userInfo.username" disabled class="netflix-input" />
          </div>
          
          <div class="form-group">
            <label>UID</label>
            <div class="static-text">{{ userInfo.id }}</div>
          </div>

          <div class="form-group">
            <label>Role</label>
            <div class="role-badge">{{ userInfo.role }}</div>
          </div>

          <div class="section-title">My Uploads</div>
          <div class="uploads-grid" v-if="videos.length > 0">
            <div class="upload-item" v-for="video in videos" :key="video.id" @click="$router.push(`/videos/${video.id}`)">
              <img :src="video.coverUrl" />
              <div class="video-title">{{ video.title }}</div>
            </div>
          </div>
          <div class="empty-state" v-else>
            No uploads yet.
          </div>

          <div class="actions">
            <button class="btn-save" @click="$router.push('/')">Done</button>
            <button class="btn-cancel" @click="handleLogout">Sign Out</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateAvatar } from '@/api/user'
import { getVideos } from '@/api/video'
import { Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const userInfo = ref<any>({})
const videos = ref<any[]>([]) // 这里暂时获取所有视频，实际应获取"我的视频"
const fileInput = ref<HTMLInputElement | null>(null)
const defaultAvatar = 'https://upload.wikimedia.org/wikipedia/commons/0/0b/Netflix-avatar.png'

onMounted(async () => {
  try {
    const res: any = await getUserInfo()
    userInfo.value = res
    
    // 暂时 mock 获取我的视频逻辑，实际需后端支持 /api/user/videos
    // 这里简单复用 getVideos 并不准确，仅作展示
    const vRes: any = await getVideos()
    // 简单过滤下属于当前用户的视频 (假设前端过滤)
    videos.value = vRes.filter((v: any) => v.userId === res.id)
  } catch (e) {
    console.error(e)
  }
})

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileChange = async (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (!files || files.length === 0) return

  const formData = new FormData()
  formData.append('file', files[0])

  try {
    await updateAvatar(formData)
    ElMessage.success('Profile photo updated.')
    const res: any = await getUserInfo()
    userInfo.value = res
    userStore.userInfo = res // 更新全局 Store
  } catch (e) {
    ElMessage.error('Failed to update photo.')
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.profile-page {
  padding-top: 100px; /* Navbar height + spacing */
  min-height: 100vh;
  display: flex;
  justify-content: center;
  background-color: #141414;
  color: #fff;
}

.profile-container {
  width: 100%;
  max-width: 800px;
  padding: 0 20px;
}

h1 {
  font-size: 48px;
  font-weight: 500;
  margin-bottom: 20px;
}

hr {
  border: 1px solid #333;
  margin-bottom: 30px;
}

.profile-content {
  display: flex;
  gap: 40px;
}

.avatar-section {
  width: 150px;
}

.avatar-wrapper {
  width: 150px;
  height: 150px;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.avatar-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 30px;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-section {
  flex: 1;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  font-size: 14px;
  color: #999;
  margin-bottom: 5px;
  text-transform: uppercase;
}

.netflix-input {
  width: 100%;
  padding: 10px;
  background: #333;
  border: none;
  color: #fff;
  font-size: 16px;
  border-radius: 4px;
}

.static-text {
  font-size: 18px;
  color: #fff;
}

.role-badge {
  display: inline-block;
  background: #e50914;
  padding: 2px 8px;
  border-radius: 2px;
  font-size: 12px;
  font-weight: bold;
}

.section-title {
  font-size: 24px;
  margin-top: 40px;
  margin-bottom: 20px;
  color: #e5e5e5;
  border-bottom: 1px solid #333;
  padding-bottom: 10px;
}

.uploads-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

.upload-item {
  cursor: pointer;
  transition: transform 0.2s;
}

.upload-item:hover {
  transform: scale(1.05);
}

.upload-item img {
  width: 100%;
  aspect-ratio: 16/9;
  object-fit: cover;
  border-radius: 4px;
}

.video-title {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.actions {
  margin-top: 40px;
  display: flex;
  gap: 20px;
}

.btn-save {
  background: #fff;
  color: #000;
  border: none;
  padding: 10px 30px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  border-radius: 2px;
}

.btn-save:hover {
  background: #c00;
  color: #fff;
}

.btn-cancel {
  background: transparent;
  border: 1px solid #999;
  color: #999;
  padding: 10px 30px;
  font-size: 16px;
  cursor: pointer;
}

.btn-cancel:hover {
  border-color: #fff;
  color: #fff;
}
</style>
