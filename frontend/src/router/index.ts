import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/layout/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'home',
          component: () => import('../views/HomeView.vue')
        },
        {
          path: 'videos',
          name: 'videos',
          component: () => import('../views/VideoList.vue')
        },
        {
          path: 'videos/:id',
          name: 'video-detail',
          component: () => import('../views/VideoDetail.vue')
        },
        {
          path: 'upload',
          name: 'upload',
          component: () => import('../views/VideoUpload.vue')
        },
        {
          path: 'admin/videos',
          name: 'video-manage',
          component: () => import('../views/VideoManage.vue')
        },
        {
          path: 'live',
          name: 'live-list',
          component: () => import('../views/LiveList.vue')
        },
        {
          path: 'live/dashboard',
          name: 'live-dashboard',
          component: () => import('../views/LiveDashboard.vue')
        },
        {
          path: 'live/:code',
          name: 'live-room',
          component: () => import('../views/LiveRoom.vue')
        },
        {
          path: 'conference',
          name: 'conference',
          component: () => import('../views/ConferenceView.vue')
        },
        {
          path: 'ai-config',
          name: 'ai-config',
          component: () => import('../views/AIConfig.vue')
        },
        {
          path: 'forum',
          name: 'forum',
          component: () => import('../views/ForumView.vue')
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/UserProfile.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.name !== 'login' && !userStore.token) {
    next({ name: 'login' })
  } else {
    next()
  }
})

export default router
