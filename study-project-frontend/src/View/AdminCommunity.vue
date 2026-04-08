<template>
  <div class="page">
    <div class="bg"></div>
    <div class="bg-dim"
         :style="{
         backdropFilter: `blur(${scrollBlur}px) saturate(140%)`,
         WebkitBackdropFilter: `blur(${scrollBlur}px) saturate(140%)`,
         background: `rgba(240,246,252,${scrollOverlay})`
       }">
    </div>
    <header class="navbar">
      <button class="nav-btn" @click="goToHome">
        <svg width="9" height="16" viewBox="0 0 9 16" fill="none">
          <path d="M8 1L1 8L8 15" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回
      </button>
      <span class="navbar-title">社区管理</span>
      <span style="width:52px"></span>
    </header>
    <div class="all-courses">
      <div class="page-header">
        <div>
          <h2 class="page-title">社区管理</h2>
          <span class="page-sub">{{ reviews.length }} 条评价</span>
        </div>
      </div>

      <!-- 评价列表 -->
      <div class="review-list">
        <div v-for="review in reviews" :key="review.id" class="glass-card">
          <div class="review-head">
            <div class="review-avatar">
              {{ review.isAnonymous ? '?' : (review.username ? review.username.charAt(0).toUpperCase() : '?') }}
            </div>
            <div class="review-info">
              <div class="review-header-row">
                <span class="review-user">{{ review.isAnonymous ? '匿名用户' : review.username }}</span>
                <span class="review-date">{{ formatDate(review.createdAt) }}</span>
              </div>
              <div class="review-meta">
                <span class="course-tag">{{ review.courseName }}</span>
                <div class="stars">
                  <svg v-for="i in 5" :key="i" viewBox="0 0 16 16" width="13" height="13"
                       :fill="i <= review.rating ? '#ff9500' : 'none'"
                       :stroke="i <= review.rating ? '#ff9500' : '#d2d2d7'" stroke-width="1.2">
                    <path d="M8 1.5l1.85 4.17L14 6.34l-3 2.92.71 4.13L8 11.4l-3.71 2 .71-4.13-3-2.92 4.15-.67z"/>
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <p class="review-content">{{ review.review }}</p>

          <div class="review-stats">
            <span>{{ review.likeCount || 0 }} 赞</span>
            <span class="stat-dot"></span>
            <span>{{ review.dislikeCount || 0 }} 踩</span>
            <span class="stat-dot"></span>
            <span>{{ (repliesMap[review.id] || []).length }} 回复</span>
          </div>

          <!-- 展开/收起回复 -->
          <button v-if="!showReplies[review.id]" class="toggle-btn" @click="loadReplies(review.id)">
            <svg viewBox="0 0 20 20" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
              <path d="M4 7l6 6 6-6"/>
            </svg>
            查看回复
          </button>
          <button v-else class="toggle-btn" @click="showReplies[review.id] = false">
            <svg viewBox="0 0 20 20" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
              <path d="M4 13l6-6 6 6"/>
            </svg>
            收起回复
          </button>

          <!-- 回复列表 -->
          <div v-if="showReplies[review.id] && repliesMap[review.id]" class="replies-section">
            <div v-for="reply in repliesMap[review.id]" :key="reply.id" class="reply-item">
              <div class="reply-avatar">
                {{ reply.isAnonymous ? '?' : (reply.username ? reply.username.charAt(0).toUpperCase() : '?') }}
              </div>
              <div class="reply-body">
                <div class="reply-header-row">
                  <span class="reply-user">{{ reply.isAnonymous ? '匿名用户' : reply.username }}</span>
                  <span v-if="reply.isAuthorReply" class="author-badge">作者</span>
                  <span v-if="reply.replyToUsername" class="reply-to-text">回复 @{{ reply.replyToUsername }}</span>
                  <span class="reply-time">{{ formatDate(reply.createdAt) }}</span>
                </div>
                <p class="reply-content">{{ reply.content }}</p>
                <div class="reply-footer">
                  <span class="reply-likes">{{ reply.likeCount || 0 }} 赞 · {{ reply.dislikeCount || 0 }} 踩</span>
                  <button class="btn-delete-reply" @click="handleDeleteReply(review.id, reply.id)">
                    <svg viewBox="0 0 20 20" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                    </svg>
                    删除
                  </button>
                </div>
              </div>
            </div>
            <div v-if="repliesMap[review.id].length === 0" class="no-replies">暂无回复</div>
          </div>
        </div>
      </div>

      <div v-if="reviews.length === 0 && !loading" class="empty-state">
        <div class="empty-icon-wrap">
          <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
            <path d="M36 16c0-6.6-5.4-12-12-12S12 9.4 12 16c0 2.2.6 4.3 1.6 6L24 40l10.4-18c1-1.7 1.6-3.8 1.6-6z"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="24" cy="16" r="4" stroke-width="2"/>
          </svg>
        </div>
        <p class="empty-title">暂无评价</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from '@/stores/index.js'
import { get, post } from '@/net/index.js'

const router = useRouter()
const store = useStore()

const scrollBlur = ref(0)
const scrollOverlay = ref(0)
const handleScroll = () => {
  const progress = Math.min(window.scrollY / 380, 1)
  scrollBlur.value = progress * 48
  scrollOverlay.value = progress * 0.52
}

const loading = ref(false)
const reviews = ref([])
const repliesMap = ref({})
const showReplies = ref({})

const formatDate = (d) => {
  if (!d) return ''
  const date = d.replace('T', ' ').split(' ')[0]
  return date.replace(/-/g, '/')
}

const goToHome = () => {
  router.push('/admin')
}

const checkAdminLogin = () => {
  return new Promise((resolve) => {
    resolve(!!store.auth.admin)
  })
}

const loadReviews = () => {
  loading.value = true
  get('/api/course/community', (data) => {
    reviews.value = data || []
    loading.value = false
  }, () => {
    ElMessage.error('加载评价列表失败')
    loading.value = false
  })
}

const loadReplies = (reviewId) => {
  get(`/api/course/community/replies?reviewId=${reviewId}`, (data) => {
    repliesMap.value[reviewId] = data || []
    showReplies.value[reviewId] = true
  })
}

const handleDeleteReply = (reviewId, replyId) => {
  ElMessageBox.confirm('确定要删除这条回复吗？此操作不可恢复。', '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    post('/api/admin/community/reply/delete', { replyId }, () => {
      ElMessage.success('回复删除成功')
      repliesMap.value[reviewId] = repliesMap.value[reviewId].filter(r => r.id !== replyId)
    }, (message) => {
      ElMessage.error(message || '删除失败')
    })
  }).catch(() => {})
}

onMounted(async () => {
  const isLoggedIn = await checkAdminLogin()
  if (!isLoggedIn) {
    ElMessage.warning('请先登录管理员账户')
    router.push('/admin-login')
    return
  }
  loadReviews()
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.page {
  min-height: 100vh;
  position: relative;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  color: #1d1d1f;
}
.bg {
  position: fixed; inset: 0; z-index: 0;
  background-image: url('@/assets/images/2.jpeg');
  background-size: cover; background-position: center;
}
.bg-dim {
  position: fixed; inset: 0; z-index: 1;
  transition: backdrop-filter 0.1s linear, -webkit-backdrop-filter 0.1s linear, background 0.1s linear;
}
.navbar {
  position: sticky; top: 0; z-index: 100;
  height: 52px;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 20px;
  background: rgba(255,255,255,0.65);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border-bottom: 0.5px solid rgba(255,255,255,0.65);
  box-shadow: 0 1px 0 rgba(0,0,0,0.05);
}
.nav-btn {
  display: flex; align-items: center; gap: 5px;
  background: none; border: none; cursor: pointer;
  font-family: inherit; font-size: 15px;
  color: #0071e3; padding: 0; transition: opacity 0.1s;
}
.nav-btn:hover { opacity: 0.7; }
.navbar-title { font-size: 16px; font-weight: 600; letter-spacing: -0.02em; }

.all-courses {
  position: relative; z-index: 2;
  max-width: 900px;
  margin: 0 auto;
  padding: 28px 20px 72px;
}

/* Header */
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 28px; gap: 16px;
}
.page-title { font-size: 26px; font-weight: 700; letter-spacing: -0.03em; color: #ffffff; line-height: 1.2; }
.page-sub { font-size: 13px; color: #86868b; }

/* Review list */
.review-list { display: flex; flex-direction: column; gap: 16px; }

/* Glass card */
.glass-card {
  border-radius: 20px;
  padding: 22px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.10), 0 0.5px 0 rgba(255,255,255,0.95) inset;
  display: flex; flex-direction: column; gap: 14px;
  transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.22s ease;
}
.glass-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 40px rgba(0,0,0,0.13), 0 0.5px 0 rgba(255,255,255,0.95) inset;
}

/* Review head */
.review-head { display: flex; align-items: center; gap: 12px; }
.review-avatar {
  width: 42px; height: 42px; border-radius: 12px; flex-shrink: 0;
  background: rgba(29,29,31,0.82);
  backdrop-filter: blur(8px);
  display: flex; align-items: center; justify-content: center;
  color: rgba(255,255,255,0.9);
  font-size: 17px; font-weight: 700;
  box-shadow: 0 4px 16px rgba(0,0,0,0.18), 0 0.5px 0 rgba(255,255,255,0.1) inset;
}
.review-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 5px; }
.review-header-row { display: flex; align-items: center; gap: 10px; }
.review-user { font-size: 15px; font-weight: 700; letter-spacing: -0.01em; color: #1d1d1f; }
.review-date { font-size: 12px; color: #aeaeb2; }
.review-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.course-tag {
  display: inline-flex; align-items: center;
  padding: 2px 10px; border-radius: 20px;
  background: rgba(0,0,0,0.06);
  font-size: 11px; font-weight: 500; color: #6e6e73;
  white-space: nowrap;
}
.stars { display: inline-flex; align-items: center; gap: 2px; }

.review-content { font-size: 14px; color: #1d1d1f; line-height: 1.65; }

/* Stats */
.review-stats {
  display: flex; align-items: center; gap: 8px;
  font-size: 12px; color: #86868b; font-weight: 500;
}
.stat-dot {
  width: 3px; height: 3px; border-radius: 50%;
  background: #d2d2d7;
}

/* Toggle button */
.toggle-btn {
  display: inline-flex; align-items: center; gap: 5px;
  background: none; border: none; cursor: pointer;
  font-family: inherit; font-size: 13px; font-weight: 500;
  color: #0071e3; padding: 0; transition: opacity 0.1s;
}
.toggle-btn:hover { opacity: 0.7; }

/* Replies section */
.replies-section {
  background: rgba(0,0,0,0.03);
  border-radius: 14px;
  padding: 14px;
  display: flex; flex-direction: column; gap: 12px;
}

.reply-item {
  display: flex; gap: 10px;
}

.reply-avatar {
  width: 30px; height: 30px; border-radius: 8px;
  background: rgba(29,29,31,0.65);
  color: rgba(255,255,255,0.9);
  font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.reply-body {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column; gap: 4px;
}

.reply-header-row {
  display: flex; align-items: center; gap: 6px; flex-wrap: wrap;
}
.reply-user { font-size: 13px; font-weight: 600; color: #1d1d1f; }
.author-badge {
  display: inline-flex; align-items: center;
  padding: 1px 6px; border-radius: 10px;
  background: linear-gradient(135deg, #0071e3, #5ac8fa);
  color: #fff; font-size: 10px; font-weight: 600;
}
.reply-to-text { font-size: 11px; color: #0071e3; font-weight: 500; }
.reply-time { font-size: 11px; color: #aeaeb2; margin-left: auto; }
.reply-content { font-size: 13px; color: #1d1d1f; line-height: 1.5; }

.reply-footer {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 2px;
}
.reply-likes { font-size: 11px; color: #86868b; }

.btn-delete-reply {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 3px 10px; border-radius: 14px;
  border: none; background: rgba(255,59,48,0.08);
  color: #ff3b30; font-size: 11px; font-weight: 600;
  font-family: inherit; cursor: pointer;
  transition: all 0.15s;
}
.btn-delete-reply:hover {
  background: rgba(255,59,48,0.18);
}

.no-replies { font-size: 12px; color: #aeaeb2; text-align: center; padding: 8px; }

/* Empty */
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 80px 20px; }
.empty-icon-wrap {
  width: 72px; height: 72px; border-radius: 20px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 20px rgba(0,0,0,0.08);
  display: flex; align-items: center; justify-content: center;
  color: #c7c7cc; margin-bottom: 6px;
}
.empty-title { font-size: 17px; font-weight: 600; color: #1d1d1f; }

@media (max-width: 768px) {
  .page-title { font-size: 22px; }
  .glass-card { padding: 18px 16px; }
  .review-avatar { width: 36px; height: 36px; font-size: 15px; }
}
</style>
