<template>
  <div class="ai-chat-container">
    <!-- 悬浮按钮 -->
    <div class="chat-trigger" @click="toggleChat" v-show="!isOpen">
      <div class="trigger-icon">
        <el-icon><ChatDotRound /></el-icon>
      </div>
    </div>

    <!-- 聊天窗口 -->
    <div class="chat-window" v-show="isOpen">
      <div class="chat-header">
        <div class="header-title">
          <el-icon><Cpu /></el-icon>
          <span>AI Assistant</span>
        </div>
        <div class="header-actions">
          <el-icon class="close-icon" @click="toggleChat"><Close /></el-icon>
        </div>
      </div>

      <div class="chat-messages" ref="messagesRef">
        <div v-for="(msg, index) in messages" :key="index" class="message" :class="msg.role">
          <div class="avatar">
            <el-icon v-if="msg.role === 'ai'"><Cpu /></el-icon>
            <el-icon v-else><User /></el-icon>
          </div>
          <div class="content">
            {{ msg.content }}
          </div>
        </div>
        <div v-if="isLoading" class="message ai loading">
          <div class="avatar"><el-icon><Cpu /></el-icon></div>
          <div class="content">Thinking...</div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          placeholder="Ask something..."
          @keyup.enter="sendMessage"
          :disabled="isLoading"
        >
          <template #suffix>
            <el-icon class="send-icon" @click="sendMessage" :class="{ disabled: !inputMessage || isLoading }">
              <Position />
            </el-icon>
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { ChatDotRound, Close, Position, Cpu, User } from '@element-plus/icons-vue'
import { chatWithAI } from '@/api/ai'

const isOpen = ref(false)
const inputMessage = ref('')
const isLoading = ref(false)
const messagesRef = ref<HTMLElement | null>(null)

interface Message {
  role: 'user' | 'ai'
  content: string
}

const messages = ref<Message[]>([
  { role: 'ai', content: 'Hello! How can I help you today?' }
])

const toggleChat = () => {
  isOpen.value = !isOpen.value
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMsg = inputMessage.value.trim()
  messages.value.push({ role: 'user', content: userMsg })
  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    const response: any = await chatWithAI(userMsg)
    messages.value.push({ role: 'ai', content: response })
  } catch (error) {
    messages.value.push({ role: 'ai', content: 'Sorry, I encountered an error. Please try again.' })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.ai-chat-container {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 2000;
}

.chat-trigger {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e50914, #b20710);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.chat-trigger:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(229, 9, 20, 0.6);
}

.trigger-icon {
  font-size: 30px;
  color: white;
}

.chat-window {
  width: 350px;
  height: 500px;
  background-color: #1f1f1f;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #333;
}

.chat-header {
  padding: 15px;
  background-color: #141414;
  border-bottom: 1px solid #333;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.close-icon {
  cursor: pointer;
  font-size: 20px;
  color: #999;
  transition: color 0.3s;
}

.close-icon:hover {
  color: white;
}

.chat-messages {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message {
  display: flex;
  gap: 10px;
  max-width: 85%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.ai {
  align-self: flex-start;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.message.user .avatar {
  background-color: #e50914;
}

.message.ai .avatar {
  background-color: #2a2a2a;
  border: 1px solid #444;
}

.content {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message.user .content {
  background-color: #e50914;
  color: white;
  border-bottom-right-radius: 2px;
}

.message.ai .content {
  background-color: #2a2a2a;
  color: #e5e5e5;
  border: 1px solid #333;
  border-bottom-left-radius: 2px;
}

.chat-input {
  padding: 15px;
  background-color: #141414;
  border-top: 1px solid #333;
}

.send-icon {
  cursor: pointer;
  color: #e50914;
  font-size: 18px;
  transition: opacity 0.3s;
}

.send-icon.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Scrollbar styling */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #1f1f1f;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>

