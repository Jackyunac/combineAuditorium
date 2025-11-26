<template>
  <div class="conference-page">
    <div class="conference-container">
      <div class="icon-wrapper">
        <el-icon><VideoCamera /></el-icon>
      </div>
      <h1>Video Conference</h1>
      <p>Connect with your team anytime, anywhere.</p>
      
      <div class="actions">
        <button class="btn-primary">New Meeting</button>
        <button class="btn-secondary">Join Meeting</button>
      </div>
      
      <div class="coming-soon">
        Feature coming soon.
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { VideoCamera } from '@element-plus/icons-vue'
</script>

<style scoped>
.conference-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #141414;
  color: #fff;
}

.conference-container {
  text-align: center;
  max-width: 500px;
}

.icon-wrapper {
  font-size: 80px;
  color: #e50914;
  margin-bottom: 20px;
}

h1 {
  font-size: 48px;
  margin-bottom: 15px;
}

p {
  font-size: 20px;
  color: #999;
  margin-bottom: 40px;
}

.actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}

button {
  padding: 12px 30px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 2px;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: #e50914;
  color: #fff;
}

.btn-secondary {
  background: #333;
  color: #fff;
}

.coming-soon {
  margin-top: 30px;
  font-size: 14px;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 2px;
}
</style>
