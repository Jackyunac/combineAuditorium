<template>
  <div class="upload-page">
    <div class="upload-container">
      <h1>上传视频</h1>
      
      <div class="upload-box" @dragover.prevent @drop.prevent="handleDrop">
        <div class="upload-area" v-if="!file">
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <p>拖拽视频文件到此处上传</p>
          <button class="btn-select" @click="triggerFileInput">选择文件</button>
          <input type="file" ref="fileInput" style="display: none" accept="video/*" @change="handleFileSelect" />
        </div>

        <div class="file-preview" v-else>
          <div class="file-info">
            <el-icon><VideoCamera /></el-icon>
            <span>{{ file.name }}</span>
            <el-icon class="close-icon" @click="file = null"><Close /></el-icon>
          </div>
          
          <div class="form-section">
            <div class="input-group">
              <label>标题</label>
              <input v-model="form.title" class="netflix-input" placeholder="输入标题" />
            </div>
            
            <div class="input-group">
              <label>简介</label>
              <textarea v-model="form.description" class="netflix-input" rows="4" placeholder="向观众介绍你的视频"></textarea>
            </div>

            <div class="input-group">
              <label>封面图片 (可选)</label>
              <div class="cover-upload" @click="triggerCoverInput">
                <img v-if="coverPreview" :src="coverPreview" class="cover-preview-img" />
                <div v-else class="cover-placeholder">
                  <el-icon><Picture /></el-icon>
                  <span>点击选择封面</span>
                </div>
              </div>
              <input type="file" ref="coverInput" style="display: none" accept="image/*" @change="handleCoverSelect" />
            </div>

            <div class="input-row">
              <div class="input-group half">
                <label>视频类型</label>
                <select v-model="form.videoType" class="netflix-input">
                  <option value="电影">电影</option>
                  <option value="电视剧">电视剧</option>
                  <option value="综艺">综艺</option>
                  <option value="纪录片">纪录片</option>
                  <option value="动漫">动漫</option>
                </select>
              </div>
              <div class="input-group half">
                <label>地区</label>
                <select v-model="form.region" class="netflix-input">
                  <option value="美国">美国</option>
                  <option value="英国">英国</option>
                  <option value="韩国">韩国</option>
                  <option value="日本">日本</option>
                  <option value="泰国">泰国</option>
                  <option value="内地">内地</option>
                  <option value="中国香港">中国香港</option>
                  <option value="中国台湾">中国台湾</option>
                  <option value="其他">其他</option>
                </select>
              </div>
            </div>

            <div class="input-row">
              <div class="input-group half">
                <label>风格</label>
                <select v-model="form.category" class="netflix-input">
                  <option value="剧情">剧情</option>
                  <option value="喜剧">喜剧</option>
                  <option value="动作">动作</option>
                  <option value="冒险">冒险</option>
                  <option value="爱情">爱情</option>
                  <option value="动画">动画</option>
                  <option value="歌舞">歌舞</option>
                  <option value="治愈">治愈</option>
                  <option value="科幻">科幻</option>
                  <option value="奇幻">奇幻</option>
                  <option value="悬疑">悬疑</option>
                  <option value="惊悚">惊悚</option>
                  <option value="恐怖">恐怖</option>
                  <option value="犯罪">犯罪</option>
                  <option value="历史">历史</option>
                  <option value="战争">战争</option>
                  <option value="灾难">灾难</option>
                  <option value="美食">美食</option>
                  <option value="真人秀">真人秀</option>
                  <option value="脱口秀">脱口秀</option>
                </select>
              </div>
              <div class="input-group half">
                <label>年份</label>
                <input v-model="form.releaseYear" type="number" class="netflix-input" placeholder="2025" />
              </div>
            </div>

            <div class="input-group">
              <label>分级</label>
              <select v-model="form.rating" class="netflix-input">
                <option value="G">G (大众级)</option>
                <option value="PG">PG (辅导级)</option>
                <option value="PG-13">PG-13 (特别辅导级)</option>
                <option value="R">R (限制级)</option>
                <option value="NC-17">NC-17 (17岁以下禁止观看)</option>
              </select>
            </div>

            <div class="actions">
              <button class="btn-upload" @click="submitUpload" :disabled="loading">
                {{ loading ? '上传中...' : '发布' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { uploadVideo } from '@/api/video'
import { useUserStore } from '@/stores/user'
import { UploadFilled, VideoCamera, Close, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const fileInput = ref<HTMLInputElement | null>(null)
const coverInput = ref<HTMLInputElement | null>(null)
const file = ref<File | null>(null)
const coverFile = ref<File | null>(null)
const coverPreview = ref<string | null>(null)
const loading = ref(false)

  onMounted(() => {
  if (userStore.userInfo?.role !== 'SYSTEM') {
    ElMessage.warning('仅限管理员上传视频')
    router.push('/')
  }
})

const form = reactive({
  title: '',
  description: '',
  category: '剧情',
  rating: 'G',
  videoType: '电影',
  region: '内地',
  releaseYear: 2025
})

const triggerFileInput = () => fileInput.value?.click()
const triggerCoverInput = () => coverInput.value?.click()

const handleFileSelect = (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (files && files.length > 0) file.value = files[0]
}

const handleCoverSelect = (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (files && files.length > 0) {
    coverFile.value = files[0]
    coverPreview.value = URL.createObjectURL(files[0])
  }
}

const handleDrop = (e: DragEvent) => {
  const files = e.dataTransfer?.files
  if (files && files.length > 0) file.value = files[0]
}

const submitUpload = async () => {
  if (!file.value) {
    ElMessage.warning('请先选择视频文件')
    return
  }
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }

  loading.value = true
  const formData = new FormData()
  formData.append('file', file.value)
  if (coverFile.value) {
    formData.append('cover', coverFile.value)
  }
  formData.append('title', form.title)
  formData.append('description', form.description)
  formData.append('category', form.category)
  formData.append('rating', form.rating)
  formData.append('videoType', form.videoType)
  formData.append('region', form.region)
  formData.append('releaseYear', form.releaseYear.toString())

  try {
    await uploadVideo(formData)
    ElMessage.success('上传成功，转码处理中')
    router.push('/')
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.upload-page {
  padding-top: 80px;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  background: #141414;
  color: #fff;
}

.upload-container {
  width: 800px;
  text-align: center;
}

h1 {
  margin-bottom: 30px;
  font-weight: 500;
}

.upload-box {
  background: #181818;
  border-radius: 8px;
  padding: 40px;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  color: #999;
}

.upload-icon {
  font-size: 64px;
  color: #333;
}

.btn-select {
  background: #e50914;
  color: #fff;
  border: none;
  padding: 10px 24px;
  font-weight: bold;
  cursor: pointer;
  border-radius: 2px;
}

.file-preview {
  width: 100%;
  text-align: left;
}

.file-info {
  background: #222;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-radius: 4px;
  margin-bottom: 30px;
}

.close-icon {
  margin-left: auto;
  cursor: pointer;
}

.input-group {
  margin-bottom: 20px;
}

.input-row {
  display: flex;
  gap: 20px;
}

.input-group.half {
  flex: 1;
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

.cover-upload {
  background: #333;
  border-radius: 4px;
  min-height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 2px dashed #555;
  transition: border-color 0.3s;
  overflow: hidden;
}

.cover-upload:hover {
  border-color: #999;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #999;
}

.cover-placeholder .el-icon {
  font-size: 32px;
}

.cover-preview-img {
  width: 100%;
  height: auto;
  max-height: 300px;
  object-fit: cover;
}

.btn-upload {
  width: 100%;
  padding: 14px;
  background: #e50914;
  color: #fff;
  border: none;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  margin-top: 10px;
}

.btn-upload:disabled {
  opacity: 0.5;
}
</style>
