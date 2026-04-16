<template>
  <div class="container">
    <div class="left-panel">
      <div class="books-container">
        <div class="book book-1">
          <div class="book-cover"></div>
          <div class="book-spine"></div>
          <div class="book-pages"></div>
        </div>
        <div class="book book-2">
          <div class="book-cover"></div>
          <div class="book-spine"></div>
          <div class="book-pages"></div>
        </div>
        <div class="book book-3">
          <div class="book-cover"></div>
          <div class="book-spine"></div>
          <div class="book-pages"></div>
        </div>
      </div>
      <div class="image-content">
        <div class="main-title">学习平台</div>
        <div class="subtitle">智慧学习<br>成就未来</div>
      </div>
    </div>

    <div class="right-panel">
      <div class="content-wrapper">
        <div class="welcome-section">
          <h1 class="welcome-title">欢迎回来</h1>
          <div class="user-info">
            <svg class="user-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="7" r="4" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="username">{{ store.auth.user?.username }}</span>
          </div>
        </div>

        <div class="actions-section">
          <button class="action-btn primary" @click="goToProfile">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="7" r="4" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>个人中心</span>
          </button>

          <button class="action-btn primary" @click="goToCourses">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>课程中心</span>
          </button>

          <button class="action-btn primary" @click="router.push('/study-plan')">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke-width="2"/>
              <line x1="16" y1="2" x2="16" y2="6" stroke-width="2" stroke-linecap="round"/>
              <line x1="8" y1="2" x2="8" y2="6" stroke-width="2" stroke-linecap="round"/>
              <line x1="3" y1="10" x2="21" y2="10" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span>学习计划</span>
          </button>

          <button class="action-btn primary" @click="router.push('/announcements')" style="position: relative;">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M13.73 21a2 2 0 01-3.46 0" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>通知公告</span>
            <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
          </button>
        </div>

        <div class="divider"></div>

        <div class="logout-section">
          <button class="action-btn danger" @click="logout">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <polyline points="16 17 21 12 16 7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="21" y1="12" x2="9" y2="12" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>退出登录</span>
          </button>
        </div>

        <div class="footer-info">
          <p class="copyright">© 2023 学习平台 版权所有</p>
          <p class="version">版本号: v1.0.0</p>
        </div>
      </div>
    </div>
    <!-- 新公告弹窗 -->
    <div class="notice-overlay" v-if="showNoticeDialog" @click.self="onNoticeDialogClose">
      <div class="brutalist-card">
        <div class="brutalist-card__header">
          <div class="brutalist-card__icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
          </div>
          <div class="brutalist-card__alert">新通知</div>
        </div>
        <div class="brutalist-card__message" v-if="latestAnnouncement">
          <div style="font-size:1rem;font-weight:900;margin-bottom:4px;">{{ latestAnnouncement.title }}</div>
          <div>{{ latestAnnouncement.content }}</div>
          <div style="font-size:0.75rem;color:#555;margin-top:4px;">{{ formatNoticeDate(latestAnnouncement.createdAt) }}</div>
        </div>
        <div class="brutalist-card__actions">
          <button class="brutalist-card__button brutalist-card__button--read" @click="onNoticeDialogClose">已读</button>
          <button class="brutalist-card__button brutalist-card__button--mark" @click="router.push('/announcements'); onNoticeDialogClose()">查看全部公告</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { get } from "@/net";
import { ElMessage } from "element-plus";
import { useStore } from "@/stores";
import { ref, computed } from 'vue';

const store = useStore()
const router = useRouter()

const announcements = ref([])
const unreadCount = ref(0)
const showNoticeDialog = ref(false)

const lastSeenKey = computed(() => 'announcement_last_seen_' + (store.auth.user?.id || 'anon'))

const unseenAnnouncements = computed(() => {
  const lastSeen = localStorage.getItem(lastSeenKey.value)
  if (!lastSeen) return announcements.value
  const seen = new Date(lastSeen).getTime()
  return announcements.value.filter(a => new Date(a.createdAt).getTime() > seen)
})

const latestAnnouncement = computed(() => unseenAnnouncements.value[0] || null)

const formatNoticeDate = (s) => {
  if (!s) return ''
  try { return new Date(s).toLocaleString('zh-CN') } catch { return s }
}

const loadAnnouncements = () => {
  get('/api/announcement/all', data => {
    announcements.value = data || []
    const lastSeen = localStorage.getItem(lastSeenKey.value)
    if (lastSeen) {
      const seen = new Date(lastSeen).getTime()
      unreadCount.value = announcements.value.filter(a => new Date(a.createdAt).getTime() > seen).length
    } else {
      unreadCount.value = announcements.value.length
    }
    if (unreadCount.value > 0) {
      showNoticeDialog.value = true
    }
  }, () => { announcements.value = [] })
}

const onNoticeDialogClose = () => {
  showNoticeDialog.value = false
  unreadCount.value = 0
  localStorage.setItem(lastSeenKey.value, new Date().toISOString())
}

loadAnnouncements()

const logout = () => {
  get('/api/auth/logout', (message) => {
    ElMessage.success(message)
    store.auth.user = null
    store.auth.admin = null
    localStorage.removeItem('admin_auth')
    router.push('/')
  })
}

const goToCourses = () => {
  router.push('/courses')
}

const goToProfile = () => {
  router.push('/profile')
}
</script>

<style scoped>
/* 容器样式，创建左右分栏 - 8:2比例 */
.container {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #FFF8E7 0%, #F5E6D3 100%);
}

/* 左侧区域 - 占80% */
.left-panel {
  flex: 4;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #FFF8E7 0%, #F5E6D3 50%, #E8D5C4 100%);
}

.space-bg {
  position: relative;
  width: 100%;
  height: 100%;
}

/* 文字放到左下角 */
.image-content {
  position: absolute;
  left: 40px;
  bottom: 40px;
  color: #2C1810;
  text-align: left;
  max-width: 500px;
  z-index: 20;
}

.main-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 15px;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 10px rgba(44, 24, 16, 0.1);
}

.subtitle {
  font-size: 20px;
  opacity: 0.85;
  line-height: 1.5;
  font-weight: 400;
  color: #5C4033;
}

/* ===== Books Animation ===== */
.books-container {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
}

.book {
  position: relative;
  width: 120px;
  height: 165px;
  transform-style: preserve-3d;
  animation: float 5s ease-in-out infinite;
}

.book-1 {
  animation-delay: 0s;
  --rotate: -15deg;
}

.book-2 {
  animation-delay: 0.5s;
  --rotate: 5deg;
}

.book-3 {
  animation-delay: 1s;
  --rotate: -5deg;
}

.book-cover {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 4px 8px 8px 4px;
  box-shadow: 0 10px 30px rgba(212, 165, 116, 0.3);
}

.book-1 .book-cover {
  background: linear-gradient(135deg, #D4A574 0%, #C4956A 100%);
}

.book-2 .book-cover {
  background: linear-gradient(135deg, #8B7355 0%, #6B5344 100%);
}

.book-3 .book-cover {
  background: linear-gradient(135deg, #A0826D 0%, #8B6B5A 100%);
}

.book-spine {
  position: absolute;
  left: -12px;
  top: 0;
  width: 12px;
  height: 100%;
  border-radius: 6px 0 0 6px;
}

.book-1 .book-spine {
  background: linear-gradient(180deg, #B8956A 0%, #A8855A 100%);
}

.book-2 .book-spine {
  background: linear-gradient(180deg, #7B6345 0%, #6B5335 100%);
}

.book-3 .book-spine {
  background: linear-gradient(180deg, #90725D 0%, #80624D 100%);
}

.book-pages {
  position: absolute;
  right: -10px;
  top: 12px;
  width: 10px;
  height: calc(100% - 24px);
  background: repeating-linear-gradient(
    180deg,
    #FFF8E7 0px,
    #FFF8E7 2px,
    #F5E6D3 2px,
    #F5E6D3 4px
  );
  border-radius: 0 5px 5px 0;
  box-shadow: inset -2px 0 4px rgba(0, 0, 0, 0.1);
}

.book::before {
  content: '';
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 8px;
  background: radial-gradient(ellipse, rgba(0, 0, 0, 0.15) 0%, transparent 70%);
  filter: blur(4px);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(var(--rotate, 0deg));
  }
  50% {
    transform: translateY(-35px) rotate(var(--rotate, 0deg));
  }
}

/* 右侧内容区域 - 玻璃态效果 */
.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 248, 231, 0.65);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  overflow-y: auto;
  padding: 30px 20px;
  position: relative;
}

/* 右侧区域的细线装饰 */
.right-panel::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 1px;
  height: 70%;
  background: linear-gradient(to bottom,
  transparent 0%,
  rgba(212, 165, 116, 0.3) 15%,
  rgba(212, 165, 116, 0.3) 85%,
  transparent 100%);
}

.content-wrapper {
  text-align: center;
  width: 100%;
  max-width: 320px;
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 48px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  color: #2C1810;
  margin: 0 0 20px 0;
  letter-spacing: -0.5px;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #8B7355;
  font-size: 15px;
  font-weight: 500;
}

.user-icon {
  width: 18px;
  height: 18px;
  color: #A0826D;
}

.username {
  color: #2C1810;
  font-weight: 600;
}

/* 操作按钮区域 */
.actions-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 40px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  height: 48px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  outline: none;
}

.btn-icon {
  width: 18px;
  height: 18px;
}

.action-btn.primary {
  background: rgba(255, 255, 255, 0.7);
  color: #2C1810;
  border: 1px solid rgba(212, 165, 116, 0.3);
}

.action-btn.primary:hover {
  background: #D4A574;
  color: #ffffff;
  border-color: #D4A574;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(212, 165, 116, 0.4);
}

.action-btn.primary:active {
  transform: translateY(0);
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(90deg,
  transparent 0%,
  rgba(212, 165, 116, 0.25) 20%,
  rgba(212, 165, 116, 0.25) 80%,
  transparent 100%);
  margin: 32px 0;
}

/* 退出登录区域 */
.logout-section {
  margin-bottom: 40px;
}

.action-btn.danger {
  background: rgba(255, 59, 48, 0.08);
  color: #E57373;
  border: 1px solid rgba(255, 59, 48, 0.2);
}

.action-btn.danger:hover {
  background: rgba(255, 59, 48, 0.15);
  color: #FF5252;
  border-color: rgba(255, 59, 48, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 59, 48, 0.25);
}

.action-btn.danger:active {
  transform: translateY(0);
}

/* 底部信息 */
.footer-info {
  color: #A0826D;
  font-size: 12px;
  line-height: 1.6;
}

.copyright {
  margin: 0 0 4px 0;
}

.version {
  margin: 0;
  opacity: 0.7;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .container {
    flex-direction: column;
  }

  .left-panel {
    flex: none;
    height: 300px;
    width: 100%;
  }

  .image-content {
    left: 50%;
    bottom: 30px;
    transform: translateX(-50%);
    text-align: center;
    width: 90%;
  }

  .main-title { font-size: 28px; margin-bottom: 10px; }
  .subtitle { font-size: 16px; }

  .books-container {
    transform: scale(0.8);
  }

  .right-panel {
    flex: none;
    width: 100%;
    min-height: calc(100vh - 300px);
    padding: 40px 20px;
  }

  .right-panel::before { display: none; }
  .content-wrapper { max-width: 400px; margin: 0 auto; }
}

@media (min-width: 1600px) {
  .image-content { left: 60px; bottom: 60px; }
  .main-title { font-size: 42px; }
  .subtitle { font-size: 22px; }
}

@media (max-width: 480px) {
  .left-panel { height: 250px; }
  .image-content { bottom: 20px; }
  .main-title { font-size: 24px; }
  .subtitle { font-size: 14px; }
  .books-container { transform: scale(0.65); }
  .right-panel { padding: 30px 15px; }
  .welcome-title { font-size: 24px; }
}

@media (max-height: 500px) {
  .image-content { bottom: 15px; }
  .main-title { font-size: 22px; margin-bottom: 8px; }
  .subtitle { font-size: 13px; }
  .books-container { transform: scale(0.6); }
}

/* Badge */
.badge {
  position: absolute; top: 4px; right: 4px;
  min-width: 18px; height: 18px; border-radius: 9px;
  background: #ff3b30; color: #fff; font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; padding: 0 4px;
  line-height: 1;
}

/* Notice overlay */
.notice-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
}

/* Brutalist card */
.brutalist-card {
  width: 380px;
  max-height: 80vh;
  overflow-y: auto;
  border: 4px solid #000;
  background-color: #fff;
  padding: 1.5rem;
  box-shadow: 10px 10px 0 #000;
  font-family: "Arial", sans-serif;
}
.brutalist-card__header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  border-bottom: 2px solid #000;
  padding-bottom: 1rem;
}
.brutalist-card__icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #000;
  padding: 0.5rem;
}
.brutalist-card__icon svg { height: 1.5rem; width: 1.5rem; fill: #fff; }
.brutalist-card__alert { font-weight: 900; color: #000; font-size: 1.5rem; text-transform: uppercase; }
.brutalist-card__message {
  margin-top: 1rem;
  color: #000;
  font-size: 0.9rem;
  line-height: 1.4;
  border-bottom: 2px solid #000;
  padding-bottom: 1rem;
  font-weight: 600;
}
.brutalist-card__message:last-of-type { border-bottom: none; }
.brutalist-card__actions { margin-top: 1rem; }
.brutalist-card__button {
  display: block; width: 100%; padding: 0.75rem;
  text-align: center; font-size: 1rem; font-weight: 700;
  text-transform: uppercase; border: 3px solid #000;
  background-color: #fff; color: #000; position: relative;
  transition: all 0.2s ease; box-shadow: 5px 5px 0 #000;
  overflow: hidden; text-decoration: none; margin-bottom: 1rem;
  cursor: pointer; font-family: inherit;
}
.brutalist-card__button--read { background-color: #000; color: #fff; }
.brutalist-card__button::before {
  content: ""; position: absolute; top: 0; left: -100%;
  width: 100%; height: 100%;
  background: linear-gradient(120deg, transparent, rgba(255,255,255,0.3), transparent);
  transition: all 0.6s;
}
.brutalist-card__button:hover::before { left: 100%; }
.brutalist-card__button:hover { transform: translate(-2px, -2px); box-shadow: 7px 7px 0 #000; }
.brutalist-card__button--mark:hover { background-color: #296fbb; border-color: #296fbb; color: #fff; box-shadow: 7px 7px 0 #004280; }
.brutalist-card__button--read:hover { background-color: #ff0000; border-color: #ff0000; color: #fff; box-shadow: 7px 7px 0 #800000; }
.brutalist-card__button:active { transform: translate(5px, 5px); box-shadow: none; }
</style>