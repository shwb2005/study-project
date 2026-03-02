<template>
  <div class="container">
    <div class="left-panel">
      <div class="image-container">
        <img
            src="@/assets/images/1.jpeg"
            alt="学习平台"
            class="full-image"
        >
        <div class="image-overlay"></div>
        <div class="image-content">
          <div class="main-title">学习平台</div>
          <div class="subtitle">智慧学习<br>成就未来</div>
        </div>
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
          <button class="action-btn primary" @click="goToCourses">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M2 3h6a4 4 0 014 4v14a3 3 0 00-3-3H2z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M22 3h-6a4 4 0 00-4 4v14a3 3 0 013-3h7z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>浏览课程</span>
          </button>

          <button class="action-btn primary" @click="goToProfile">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="7" r="4" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>个人资料</span>
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
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { get } from "@/net";
import { ElMessage } from "element-plus";
import { useStore } from "@/stores";
import { reactive } from 'vue';

const store = useStore()
const router = useRouter()

const form = reactive({
  remember: false
})

const logout = () => {
  get('/api/auth/logout', (message) => {
    ElMessage.success(message)
    store.auth.user = null
    router.push('/')
  })
}

const goToCourses = () => {
  router.push('/courses')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToIndex = () => {
  router.push('/index')
}
</script>

<style scoped>
/* 容器样式，创建左右分栏 - 8:2比例 */
.container {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
  background: #fafafa;
}

/* 左侧图片区域 - 占80% */
.left-panel {
  flex: 4; /* 80%比例 (4:1 = 80%:20%) */
  position: relative;
  overflow: hidden;
}

.image-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.full-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transform: scale(1.02);
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.image-container:hover .full-image {
  transform: scale(1);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg,
  rgba(0, 0, 0, 0.4) 0%,
  rgba(0, 0, 0, 0.2) 50%,
  rgba(0, 0, 0, 0.1) 100%);
}

/* 文字放到左下角 */
.image-content {
  position: absolute;
  left: 40px;
  bottom: 40px;
  color: white;
  text-align: left;
  max-width: 500px;
  z-index: 2;
}

.main-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 15px;
  letter-spacing: -0.5px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.subtitle {
  font-size: 20px;
  opacity: 0.95;
  line-height: 1.5;
  font-weight: 400;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

/* 右侧内容区域 - 占20%，简约无框风格 */
.right-panel {
  flex: 1; /* 20%比例 */
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
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
  #e5e5e5 15%,
  #e5e5e5 85%,
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
  color: #1a1a1a;
  margin: 0 0 20px 0;
  letter-spacing: -0.5px;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #6e6e73;
  font-size: 15px;
  font-weight: 500;
}

.user-icon {
  width: 18px;
  height: 18px;
  color: #8e8e93;
}

.username {
  color: #1a1a1a;
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
  background: #f5f5f7;
  color: #1a1a1a;
  border: 1px solid #e5e5e5;
}

.action-btn.primary:hover {
  background: #1a1a1a;
  color: #ffffff;
  border-color: #1a1a1a;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
}

.action-btn.primary:active {
  transform: translateY(0);
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(90deg,
  transparent 0%,
  #e5e5e5 20%,
  #e5e5e5 80%,
  transparent 100%);
  margin: 32px 0;
}

/* 退出登录区域 */
.logout-section {
  margin-bottom: 40px;
}

.action-btn.danger {
  background: #ffffff;
  color: #ff3b30;
  border: 1px solid #ffcccb;
}

.action-btn.danger:hover {
  background: #ff3b30;
  color: #ffffff;
  border-color: #ff3b30;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(255, 59, 48, 0.25);
}

.action-btn.danger:active {
  transform: translateY(0);
}

/* 底部信息 */
.footer-info {
  color: #8e8e93;
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

  /* 移动端文字居中显示 */
  .image-content {
    left: 50%;
    bottom: 30px;
    transform: translateX(-50%);
    text-align: center;
    width: 90%;
  }

  .main-title {
    font-size: 28px;
    margin-bottom: 10px;
  }

  .subtitle {
    font-size: 16px;
  }

  .full-image {
    transform: scale(1);
  }

  .right-panel {
    flex: none;
    width: 100%;
    min-height: calc(100vh - 300px);
    padding: 40px 20px;
  }

  .right-panel::before {
    display: none;
  }

  .content-wrapper {
    max-width: 400px;
    margin: 0 auto;
  }
}

/* 超大屏幕优化 */
@media (min-width: 1600px) {
  .image-content {
    left: 60px;
    bottom: 60px;
  }

  .main-title {
    font-size: 42px;
  }

  .subtitle {
    font-size: 22px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 480px) {
  .left-panel {
    height: 250px;
  }

  .image-content {
    bottom: 20px;
  }

  .main-title {
    font-size: 24px;
  }

  .subtitle {
    font-size: 14px;
  }

  .right-panel {
    padding: 30px 15px;
  }

  .welcome-title {
    font-size: 24px;
  }
}

/* 超矮屏幕优化 */
@media (max-height: 500px) {
  .image-content {
    bottom: 15px;
  }

  .main-title {
    font-size: 22px;
    margin-bottom: 8px;
  }

  .subtitle {
    font-size: 13px;
  }
}
</style>