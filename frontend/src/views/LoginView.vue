<template>
  <div class="login-wrapper">
    <div class="login-header">
      <div class="logo">
        <img src="/Lips.jpg" alt="Lips" />
      </div>
    </div>
    
    <div class="login-body">
      <div class="login-box">
        <h1>{{ isLogin ? 'Sign In' : 'Sign Up' }}</h1>
        
        <form @submit.prevent="handleSubmit">
          <div class="input-group">
            <input v-model="form.username" type="text" required placeholder="Email or phone number" />
          </div>
          
          <div class="input-group">
            <input v-model="form.password" type="password" required placeholder="Password" />
          </div>
          
          <div class="input-group" v-if="!isLogin">
            <input v-model="form.confirmPassword" type="password" required placeholder="Confirm Password" />
          </div>
          
          <button type="submit" class="btn-submit" :disabled="loading">
            {{ loading ? 'Loading...' : (isLogin ? 'Sign In' : 'Sign Up') }}
          </button>
        </form>
        
        <div class="login-footer">
          <div class="signup-now">
            {{ isLogin ? 'New to Netflix?' : 'Already have an account?' }}
            <a href="#" @click.prevent="toggleMode">{{ isLogin ? 'Sign up now' : 'Sign in' }}.</a>
          </div>
          <div class="recaptcha">
            This page is protected by Google reCAPTCHA to ensure you're not a bot.
          </div>
        </div>
      </div>
    </div>
    
    <div class="login-bg-overlay"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login, register } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const isLogin = ref(true)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
}

const handleSubmit = async () => {
  if (!isLogin.value && form.password !== form.confirmPassword) {
    ElMessage.error('Passwords do not match')
    return
  }

  loading.value = true
  try {
    if (isLogin.value) {
      const res: any = await login({ username: form.username, password: form.password })
      userStore.setToken(res.token)
      router.push('/')
    } else {
      await register({ username: form.username, password: form.password, confirmPassword: form.confirmPassword })
      ElMessage.success('Registration successful, please sign in.')
      isLogin.value = true
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  background-image: url('https://assets.nflxext.com/ffe/siteui/vlv3/9d3533b2-0e2b-40b2-95e0-ecd7979cc92b/55875711-e062-4485-8887-c7c201472169/JP-en-20240311-popsignuptwoweeks-perspective_alpha_website_large.jpg');
  background-size: cover;
  background-position: center;
  position: relative;
}

.login-bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  z-index: 0;
}

.login-header {
  position: relative;
  z-index: 10;
  padding: 20px 60px;
}

.logo {
  cursor: pointer;
}

.logo img {
  height: 60px;
  object-fit: contain;
}

.login-body {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: center;
  min-height: calc(100vh - 90px);
  align-items: center;
}

.login-box {
  background: rgba(0,0,0,0.75);
  border-radius: 4px;
  padding: 60px 68px 40px;
  width: 450px;
  box-sizing: border-box;
}

h1 {
  color: #fff;
  font-size: 32px;
  margin-bottom: 28px;
}

.input-group {
  margin-bottom: 16px;
}

input {
  width: 100%;
  height: 50px;
  background: #333;
  border: none;
  border-radius: 4px;
  color: #fff;
  padding: 0 20px;
  font-size: 16px;
  box-sizing: border-box;
}

input:focus {
  background: #454545;
  outline: none;
}

.btn-submit {
  width: 100%;
  height: 48px;
  background: #e50914;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 24px;
}

.btn-submit:disabled {
  opacity: 0.5;
}

.login-footer {
  margin-top: 16px;
  color: #737373;
}

.signup-now {
  font-size: 16px;
  margin-bottom: 10px;
}

.signup-now a {
  color: #fff;
  text-decoration: none;
  margin-left: 5px;
}

.signup-now a:hover {
  text-decoration: underline;
}

.recaptcha {
  font-size: 13px;
  margin-top: 10px;
}
</style>
