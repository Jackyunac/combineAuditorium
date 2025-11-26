<template>
  <div class="page">
    <header class="page-header">
      <div>
        <h1>论坛</h1>
        <p>分享心得、问题和经验，支持多人交流。</p>
      </div>
      <button class="primary" @click="showEditor = !showEditor">{{ showEditor ? '收起' : '发帖' }}</button>
    </header>

    <section v-if="showEditor" class="form-card">
      <input v-model="form.title" placeholder="帖子标题" />
      <textarea v-model="form.content" rows="5" placeholder="内容"></textarea>
      <div class="form-actions">
        <button class="primary" @click="createPost">发布</button>
      </div>
    </section>

    <section class="posts">
      <div v-for="post in posts" :key="post.id" class="card">
        <div class="card-head">
          <div>
            <h3>{{ post.title }}</h3>
            <p class="meta">作者：{{ post.nickname || ('用户' + post.userId) }} · {{ formatTime(post.updatedAt || post.createdAt) }}</p>
          </div>
          <button class="ghost" @click="openPost(post)">查看</button>
        </div>
        <p class="excerpt">{{ snippet(post.content) }}</p>
      </div>
      <p v-if="!posts.length" class="empty">暂无帖子，快来发布第一条吧。</p>
    </section>

    <div v-if="activePost" class="drawer">
      <div class="drawer-header">
        <div>
          <h3>{{ activePost.title }}</h3>
          <p class="meta">作者：{{ activePost.nickname || ('用户' + activePost.userId) }}</p>
        </div>
        <button class="ghost" @click="closeDrawer">关闭</button>
      </div>
      <p class="content">{{ activePost.content }}</p>

      <div class="comment-editor">
        <textarea v-model="commentContent" rows="3" placeholder="写下你的评论..." @keyup.enter="submitComment"></textarea>
        <button class="primary" @click="submitComment">发表评论</button>
      </div>
      <div class="comments">
        <div v-for="c in comments" :key="c.id" class="comment">
          <div class="comment-head">
            <span class="user">{{ c.nickname || ('用户' + c.userId) }}</span>
            <span class="time">{{ formatTime(c.createdAt) }}</span>
          </div>
          <p>{{ c.content }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listPosts, createPost as createPostApi, getPost, listComments, addComment } from '@/api/forum'

const posts = ref<any[]>([])
const showEditor = ref(false)
const form = ref({ title: '', content: '' })
const activePost = ref<any | null>(null)
const comments = ref<any[]>([])
const commentContent = ref('')

const loadPosts = async () => {
  try {
    posts.value = await listPosts(50)
  } catch (e) {
    ElMessage.error('加载帖子失败')
  }
}

const createPost = async () => {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  try {
    await createPostApi(form.value)
    ElMessage.success('发布成功')
    form.value = { title: '', content: '' }
    showEditor.value = false
    await loadPosts()
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

const openPost = async (post: any) => {
  activePost.value = await getPost(post.id)
  comments.value = await listComments(post.id)
}

const closeDrawer = () => {
  activePost.value = null
  comments.value = []
}

const submitComment = async () => {
  if (!activePost.value) return
  const content = commentContent.value.trim()
  if (!content) return
  try {
    await addComment(activePost.value.id, content)
    commentContent.value = ''
    comments.value = await listComments(activePost.value.id)
  } catch (e) {
    ElMessage.error('评论失败')
  }
}

const snippet = (text?: string) => {
  if (!text) return ''
  return text.length > 120 ? text.slice(0, 120) + '...' : text
}

const formatTime = (t?: string) => {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleString()
}

onMounted(loadPosts)
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
.ghost {
  background: transparent;
  border: 1px solid #2a3040;
  color: #fff;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}
.form-card {
  background: #121520;
  border: 1px solid #1e2433;
  padding: 14px;
  border-radius: 12px;
  margin-bottom: 14px;
  display: grid;
  gap: 10px;
}
input, textarea {
  background: #1b1f2a;
  border: 1px solid #2a3040;
  color: #fff;
  padding: 10px;
  border-radius: 8px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
}
.posts .card {
  background: #121520;
  border: 1px solid #1e2433;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 12px;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.meta {
  color: #9ea3b5;
  font-size: 12px;
}
.excerpt {
  color: #e5e7ef;
}
.empty {
  color: #9ea3b5;
}
.drawer {
  position: fixed;
  right: 0;
  top: 70px;
  width: 420px;
  height: calc(100vh - 70px);
  background: #11131a;
  border-left: 1px solid #1e2433;
  padding: 16px;
  overflow-y: auto;
  box-shadow: -8px 0 24px rgba(0,0,0,0.35);
}
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.comment-editor {
  margin: 14px 0;
  display: grid;
  gap: 10px;
}
.comments .comment {
  border-bottom: 1px solid #1e2433;
  padding: 8px 0;
}
.comment-head {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #9ea3b5;
}
.comment p {
  margin: 4px 0 0;
}
</style>
