<template>
  <div class="dashboard-page">
    <div class="dashboard-container">
      <h1>Live Settings</h1>
      
      <div class="form-box">
        <div class="input-group">
          <label>Room Title</label>
          <input v-model="form.title" class="netflix-input" placeholder="Enter a catchy title" />
        </div>
        
        <div class="input-group">
          <label>Cover Image URL</label>
          <input v-model="form.coverUrl" class="netflix-input" placeholder="https://..." />
          <div class="cover-preview" v-if="form.coverUrl">
            <img :src="form.coverUrl" />
          </div>
        </div>

        <div class="actions">
          <button class="btn-save" @click="saveSettings" :disabled="loading">
            {{ loading ? 'Saving...' : 'Start Streaming' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyRoom, updateRoom } from '@/api/live'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  title: '',
  coverUrl: ''
})
const roomCode = ref('')

onMounted(async () => {
  try {
    const res: any = await getMyRoom()
    form.title = res.title
    form.coverUrl = res.coverUrl
    roomCode.value = res.roomCode
  } catch (e) { console.error(e) }
})

const saveSettings = async () => {
  if (!form.title) {
    ElMessage.warning('Please enter a title')
    return
  }
  
  loading.value = true
  try {
    await updateRoom(form)
    // 跳转到直播间页面 (主播模式)
    router.push(`/live/${roomCode.value}?isAnchor=true`)
  } catch (e) {
    ElMessage.error('Failed to update settings')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dashboard-page {
  padding-top: 80px;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  background: #141414;
  color: #fff;
}

.dashboard-container {
  width: 600px;
}

h1 {
  margin-bottom: 30px;
  font-weight: 500;
  text-align: center;
}

.form-box {
  background: #181818;
  padding: 40px;
  border-radius: 8px;
}

.input-group {
  margin-bottom: 20px;
}

label {
  display: block;
  color: #999;
  margin-bottom: 8px;
  font-size: 14px;
}

.netflix-input {
  width: 100%;
  background: #333;
  border: none;
  padding: 12px;
  color: #fff;
  border-radius: 2px;
  font-size: 16px;
  box-sizing: border-box;
}

.netflix-input:focus {
  background: #444;
  outline: none;
}

.cover-preview {
  margin-top: 10px;
  aspect-ratio: 16/9;
  overflow: hidden;
  border-radius: 4px;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.actions {
  margin-top: 30px;
}

.btn-save {
  width: 100%;
  padding: 14px;
  background: #e50914;
  color: #fff;
  border: none;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
}

.btn-save:disabled {
  opacity: 0.5;
}
</style>

