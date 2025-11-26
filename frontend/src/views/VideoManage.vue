<template>
  <div class="video-manage-page">
    <div class="header">
      <h1>Video Management</h1>
      <el-button type="primary" @click="$router.push('/upload')">Upload New Video</el-button>
    </div>

    <el-table :data="videos" style="width: 100%" class="dark-table">
      <el-table-column label="Cover" width="120">
        <template #default="{ row }">
          <img :src="row.coverUrl || 'https://via.placeholder.com/100x60'" class="table-cover" />
        </template>
      </el-table-column>
      
      <el-table-column prop="title" label="Title" min-width="200" />
      
      <el-table-column prop="category" label="Category" width="100" />
      
      <el-table-column label="Stats" width="150">
        <template #default="{ row }">
          <div><el-icon><View /></el-icon> {{ row.viewCount || 0 }}</div>
          <div><el-icon><ChatDotSquare /></el-icon> {{ row.commentCount || 0 }}</div>
        </template>
      </el-table-column>
      
      <el-table-column label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="Hot" width="80">
        <template #default="{ row }">
          <el-switch
            v-model="row.isHot"
            :active-value="1"
            :inactive-value="0"
            @change="handleHotChange(row)"
          />
        </template>
      </el-table-column>
      
      <el-table-column label="Actions" width="250" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">Edit</el-button>
          
          <el-button 
            size="small" 
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? 'Off Shelf' : 'On Shelf' }}
          </el-button>
          
          <el-button size="small" type="danger" @click="handleDelete(row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Edit Dialog -->
    <el-dialog v-model="editVisible" title="Edit Video" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="Title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="Desc">
          <el-input v-model="editForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="Category">
          <el-select v-model="editForm.category">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitEdit">Confirm</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminVideos, updateVideo, deleteVideo } from '@/api/video'
import { View, ChatDotSquare } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const videos = ref<any[]>([])
const editVisible = ref(false)
const editForm = ref<any>({})
const categories = ['Action', 'Comedy', 'Drama', 'Sci-Fi', 'Horror', 'Documentary', 'Anime']

const fetchVideos = async () => {
  try {
    const res: any = await getAdminVideos()
    videos.value = res
  } catch (e) { console.error(e) }
}

onMounted(fetchVideos)

const getStatusType = (status: number) => {
  if (status === 1) return 'success'
  if (status === 0) return 'warning' // Transcoding
  if (status === 2) return 'danger'  // Failed
  return 'info' // Off shelf (3)
}

const getStatusText = (status: number) => {
  const map: any = { 0: 'Processing', 1: 'Normal', 2: 'Failed', 3: 'Off Shelf' }
  return map[status] || 'Unknown'
}

const handleHotChange = async (row: any) => {
  try {
    await updateVideo(row.id, { isHot: row.isHot })
    ElMessage.success('Updated hot status')
  } catch (e) {
    row.isHot = row.isHot === 1 ? 0 : 1 // revert
    ElMessage.error('Failed to update')
  }
}

const toggleStatus = async (row: any) => {
  const newStatus = row.status === 1 ? 3 : 1
  try {
    await updateVideo(row.id, { status: newStatus })
    row.status = newStatus
    ElMessage.success('Updated status')
  } catch (e) {
    ElMessage.error('Failed to update')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('Are you sure to delete this video?', 'Warning', {
    confirmButtonText: 'Delete',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(async () => {
    await deleteVideo(row.id)
    ElMessage.success('Deleted')
    fetchVideos()
  })
}

const openEdit = (row: any) => {
  editForm.value = { ...row }
  editVisible.value = true
}

const submitEdit = async () => {
  try {
    await updateVideo(editForm.value.id, {
      title: editForm.value.title,
      description: editForm.value.description,
      category: editForm.value.category
    })
    ElMessage.success('Updated')
    editVisible.value = false
    fetchVideos()
  } catch (e) {
    ElMessage.error('Failed to update')
  }
}
</script>

<style scoped>
.video-manage-page {
  padding: 80px 40px;
  min-height: 100vh;
  background: #141414;
  color: #fff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.dark-table {
  --el-table-bg-color: #1f1f1f;
  --el-table-tr-bg-color: #1f1f1f;
  --el-table-header-bg-color: #141414;
  --el-table-text-color: #fff;
  --el-table-header-text-color: #999;
  --el-table-row-hover-bg-color: #333;
  --el-table-border-color: #333;
}

.table-cover {
  width: 100px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}
</style>

