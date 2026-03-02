<template>
  <div class="container">
    <div class="left-panel">
      <div class="image-container">
        <img
            src="@/assets/images/1.jpeg"
            alt="管理员系统"
            class="full-image"
        >
        <div class="image-overlay"></div>
        <div class="image-content">
          <div style="font-size: 36px;font-weight: bold; margin-bottom: 15px;">管理员系统</div>
          <div style="font-size: 20px;opacity: 0.9; line-height: 1.5">后台管理系统<br>数据管理中心</div>
        </div>
      </div>
    </div>

    <div class="right-panel">
      <div style="text-align: center; width: 100%; max-width: 320px;">
        <div class="welcome-title">
          <div style="font-size: 28px;font-weight: bold; color: #333;">管理员面板</div>

        </div>

        <div class="actions-section">
          <div style="margin-top: 20px">
            <el-button @click="goToAddAdmin" style="width: 100%; height: 48px;" plain>
              <i class="el-icon-user" style="margin-right: 10px; font-size: 16px;"></i>
              <span style="font-size: 15px;">管理员管理</span>
            </el-button>
          </div>
          <div style="margin-top: 20px">
            <el-button @click="goToAddCourse" style="width: 100%; height: 48px;" plain>
              <i class="el-icon-notebook-2" style="margin-right: 10px; font-size: 16px;"></i>
              <span style="font-size: 15px;">课程管理</span>
            </el-button>
          </div>
        </div>

        <div style="margin-top: 40px; padding-top: 30px; border-top: 1px solid #eee;">

          <div>
            <el-button @click="logout" style="width: 100%; height: 46px;" type="danger">
              <i class="el-icon-switch-button" style="margin-right: 8px;"></i>
              退出登录
            </el-button>
          </div>
        </div>

        <div style="margin-top: 40px; color: #999; font-size: 12px;">
          <p>© 2023 学习平台 管理员系统</p>
          <p style="margin-top: 5px;">管理员版本: v1.0.0</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useStore } from "@/stores/index.js";
import { ElMessage } from "element-plus";

const router = useRouter()
const store = useStore()

const goToAddAdmin = () => {
  router.push('/add-admin')
}

const goToAddCourse = () => {
  router.push('/add-course')
}

const goBack = () => {
  router.push('/index')
}

const logout = () => {
  store.auth.admin = null
  ElMessage.success("已退出管理员登录")
  router.push('/admin-login')
}
</script>

<style scoped>
/* 容器样式，创建左右分栏 - 8:2比例 */
.container {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
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
  transform: scale(1.05);
  transition: transform 0.5s ease;
}

.image-container:hover .full-image {
  transform: scale(1.03);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg,
  rgba(0, 0, 0, 0.25) 0%,
  rgba(0, 0, 0, 0.15) 50%,
  rgba(0, 0, 0, 0.05) 100%);
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
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

/* 右侧内容区域 - 占20%，简约无框风格 */
.right-panel {
  flex: 1; /* 20%比例 */
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8fafc;
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
  #e0e6ed 15%,
  #e0e6ed 85%,
  transparent 100%);
}

.welcome-title {
  margin-bottom: 40px;
}

.actions-section {
  margin-bottom: 20px;
}

/* 按钮样式优化 */
.el-button {
  transition: all 0.3s ease;
  border-radius: 8px;
}

.el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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

  .image-content div:first-child {
    font-size: 28px;
    margin-bottom: 10px;
  }

  .image-content div:last-child {
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

  .right-panel > div {
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

  .image-content div:first-child {
    font-size: 42px;
  }

  .image-content div:last-child {
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

  .image-content div:first-child {
    font-size: 24px;
  }

  .image-content div:last-child {
    font-size: 14px;
  }

  .right-panel {
    padding: 30px 15px;
  }

  .welcome-title div:first-child {
    font-size: 24px;
  }
}

/* 超矮屏幕优化 */
@media (max-height: 500px) {
  .image-content {
    bottom: 15px;
  }

  .image-content div:first-child {
    font-size: 22px;
    margin-bottom: 8px;
  }

  .image-content div:last-child {
    font-size: 13px;
  }
}
</style>