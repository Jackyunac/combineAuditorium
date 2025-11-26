<template>
  <div class="netflix-layout">
    <header class="navbar" :class="{ scrolled: isScrolled }">
      <div class="nav-left">
        <div class="logo" @click="$router.push('/')">
          <span class="brand-text">Lips</span>
        </div>
        <ul class="nav-links" v-if="userStore.token">
          <li :class="{ active: $route.path === '/' || $route.path === '/home' }"><router-link to="/">首页</router-link></li>
          <li :class="{ active: $route.path.startsWith('/videos') }"><router-link to="/videos">视频库</router-link></li>
          <li :class="{ active: $route.path.startsWith('/live') }"><router-link to="/live">直播</router-link></li>
          <li :class="{ active: $route.path.startsWith('/conference') }"><router-link to="/conference">会议</router-link></li>
          <li :class="{ active: $route.path.startsWith('/forum') }"><router-link to="/forum">论坛</router-link></li>
          <li v-if="userStore.userInfo?.role === 'SYSTEM'" :class="{ active: $route.path.startsWith('/ai-config') }"><router-link to="/ai-config">AI配置</router-link></li>
        </ul>
      </div>

      <div class="nav-right" v-if="userStore.token">
        <el-icon class="icon"><Search /></el-icon>
        <el-icon class="icon"><Bell /></el-icon>
        
        <div class="profile-menu">
          <img :src="userStore.userInfo?.avatar || defaultAvatar" class="avatar" />
          <el-icon class="caret"><CaretBottom /></el-icon>
          
          <div class="dropdown-menu">
            <div class="menu-item" @click="$router.push('/profile')">个人中心</div>
            <div class="menu-item" @click="$router.push('/upload')" v-if="userStore.userInfo?.role === 'SYSTEM'">上传视频</div>
            <div class="menu-item" @click="$router.push('/admin/videos')" v-if="userStore.userInfo?.role === 'SYSTEM'">视频管理</div>
            <div class="menu-item divider" @click="handleLogout">退出登录</div>
          </div>
        </div>
      </div>
      <div class="nav-right" v-else>
        <el-button type="primary" size="small" @click="$router.push('/login')">登录</el-button>
      </div>
    </header>

    <main class="main-view">
      <router-view />
    </main>
    
    <!-- AI 聊天助手 -->
    <AIChat v-if="userStore.token" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Search, Bell, CaretBottom } from '@element-plus/icons-vue'
import { getUserInfo } from '@/api/user'
import AIChat from '@/components/AIChat.vue'

const router = useRouter()
const userStore = useUserStore()
const isScrolled = ref(false)
const defaultAvatar = 'https://upload.wikimedia.org/wikipedia/commons/0/0b/Netflix-avatar.png'

const handleScroll = () => {
  isScrolled.value = window.scrollY > 0
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  window.addEventListener('scroll', handleScroll)
  if (userStore.token) {
    try {
      const res: any = await getUserInfo()
      userStore.userInfo = res
    } catch (e) { console.error(e) }
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.netflix-layout {
  min-height: 100vh;
  background-color: #141414;
}

.navbar {
  position: fixed;
  top: 0;
  width: 100%;
  height: 68px;
  padding: 0 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 1000;
  background: linear-gradient(to bottom, rgba(0,0,0,0.7) 10%, rgba(0,0,0,0));
  transition: background-color 0.3s;
  box-sizing: border-box;
}

.navbar.scrolled {
  background-color: #141414;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.brand-text {
  font-family: 'Great Vibes', cursive;
  font-size: 45px;
  color: #e50914;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  line-height: 1;
  padding-bottom: 5px;
}

.nav-links {
  display: flex;
  list-style: none;
  gap: 20px;
  margin: 0;
  padding: 0;
}

.nav-links a {
  color: #e5e5e5;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.nav-links a:hover, .nav-links li.active a {
  color: #fff;
  font-weight: 500;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
}

.icon {
  font-size: 20px;
  cursor: pointer;
}

.profile-menu {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  position: relative;
}

.profile-menu:hover .dropdown-menu {
  display: block;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 4px;
}

.dropdown-menu {
  display: none;
  position: absolute;
  top: 50px;
  right: 0;
  background-color: rgba(0,0,0,0.9);
  border: 1px solid rgba(255,255,255,0.15);
  padding: 10px 0;
  width: 150px;
}

.dropdown-menu::before {
  content: '';
  position: absolute;
  top: -20px;
  left: 0;
  width: 100%;
  height: 20px;
  background: transparent;
}

.menu-item {
  padding: 10px 20px;
  font-size: 13px;
  color: #fff;
}

.menu-item:hover {
  text-decoration: underline;
}

.menu-item.divider {
  border-top: 1px solid rgba(255,255,255,0.15);
  margin-top: 5px;
  padding-top: 15px;
}
</style>
