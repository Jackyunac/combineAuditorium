<template>
  <div class="page" v-if="isSystem">
    <header class="page-header">
      <div>
        <h1>AI 配置</h1>
        <p>仅 SYSTEM 可见，配置温度、TopK 与知识库召回条数。</p>
      </div>
      <button class="primary" @click="loadConfig">刷新</button>
    </header>

    <section class="form-card">
      <div class="form-grid">
        <label>
          <span>Temperature (0-1)</span>
          <input type="number" step="0.1" min="0" max="1" v-model.number="form.temperature" />
        </label>
        <label>
          <span>TopK</span>
          <input type="number" min="1" v-model.number="form.topK" />
        </label>
        <label>
          <span>知识召回条数</span>
          <input type="number" min="1" v-model.number="form.knowledgeLimit" />
        </label>
      </div>
      <div class="actions">
        <button class="primary" @click="save" :disabled="loading">保存</button>
      </div>
    </section>
  </div>
  <div v-else class="forbidden">无权限访问</div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getAIConfig, updateAIConfig } from '@/api/ai'

const userStore = useUserStore()
const isSystem = computed(() => userStore.userInfo?.role === 'SYSTEM')
const form = ref<any>({ temperature: 0.7, topK: 3, knowledgeLimit: 3 })
const loading = ref(false)

const loadConfig = async () => {
  if (!isSystem.value) return
  try {
    form.value = await getAIConfig()
  } catch (e) {
    ElMessage.error('加载配置失败')
  }
}

const save = async () => {
  loading.value = true
  try {
    await updateAIConfig(form.value)
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadConfig)
</script>

<style scoped>
.page {
  padding: 90px 60px 40px;
  background: #0f1115;
  color: #fff;
  min-height: 100vh;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.primary {
  background: #1c7bf1;
  border: none;
  color: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  cursor: pointer;
}
.form-card {
  background: #121520;
  border: 1px solid #1e2433;
  border-radius: 12px;
  padding: 16px;
  display: grid;
  gap: 12px;
  max-width: 680px;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}
label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  color: #cfd3dc;
}
input {
  background: #1b1f2a;
  border: 1px solid #2a3040;
  color: #fff;
  padding: 10px;
  border-radius: 8px;
}
.actions {
  display: flex;
  justify-content: flex-end;
}
.forbidden {
  padding: 100px 40px;
  color: #fff;
  text-align: center;
}
</style>
