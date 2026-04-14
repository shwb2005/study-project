<template>
  <div class="container">
    <div class="left-panel">
      <div class="animation-container">
        <div class="wheel-and-hamster">
          <div class="wheel">
            <div class="spoke"></div>
          </div>
          <div class="hamster">
            <div class="hamster__body">
              <div class="hamster__head">
                <div class="hamster__ear"></div>
                <div class="hamster__eye"></div>
                <div class="hamster__nose"></div>
              </div>
              <div class="hamster__limb--fr"></div>
              <div class="hamster__limb--fl"></div>
              <div class="hamster__limb--br"></div>
              <div class="hamster__limb--bl"></div>
              <div class="hamster__tail"></div>
            </div>
          </div>
        </div>
      </div>
      <div class="left-text">
        <div class="left-title">管理员系统</div>
        <div class="left-sub">后台管理系统<br>数据管理中心</div>
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
          <div style="margin-top: 20px">
            <el-button @click="goToCommunity" style="width: 100%; height: 48px;" plain>
              <i class="el-icon-chat-dot-round" style="margin-right: 10px; font-size: 16px;"></i>
              <span style="font-size: 15px;">社区管理</span>
            </el-button>
          </div>
          <div style="margin-top: 20px">
            <el-button @click="goToAnnouncement" style="width: 100%; height: 48px;" plain>
              <i class="el-icon-bell" style="margin-right: 10px; font-size: 16px;"></i>
              <span style="font-size: 15px;">公告管理</span>
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

const goToCommunity = () => {
  router.push('/admin-community')
}

const goToAnnouncement = () => {
  router.push('/admin-announcement')
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

/* 左侧区域 - 占80% */
.left-panel {
  flex: 4;
  position: relative;
  overflow: hidden;
  background: linear-gradient(160deg, #f5f5f7 0%, #e8e8ed 40%, #f0f0f2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 背景装饰 */
.left-panel::before {
  content: '';
  position: absolute;
  top: -80px;
  right: -80px;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0,113,227,0.06) 0%, transparent 70%);
  pointer-events: none;
}

.left-panel::after {
  content: '';
  position: absolute;
  bottom: 100px;
  left: -40px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,149,0,0.05) 0%, transparent 70%);
  pointer-events: none;
}

.animation-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.left-text {
  position: absolute;
  left: 40px;
  bottom: 40px;
  color: #1d1d1f;
  text-align: left;
  max-width: 500px;
  z-index: 2;
}

.left-title {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 15px;
}

.left-sub {
  font-size: 20px;
  opacity: 0.6;
  line-height: 1.5;
}

/* Hamster wheel animation */
.wheel-and-hamster {
  --dur: 1s;
  position: relative;
  width: 12em;
  height: 12em;
  font-size: 28px;
}

.wheel,
.hamster,
.hamster div,
.spoke {
  position: absolute;
}

.wheel,
.spoke {
  border-radius: 50%;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.wheel {
  background: radial-gradient(100% 100% at center, hsla(0,0%,60%,0) 47.8%, hsl(0,0%,60%) 48%);
  z-index: 2;
}

.hamster {
  animation: hamster var(--dur) ease-in-out infinite;
  top: 50%;
  left: calc(50% - 3.5em);
  width: 7em;
  height: 3.75em;
  transform: rotate(4deg) translate(-0.8em,1.85em);
  transform-origin: 50% 0;
  z-index: 1;
}

.hamster__head {
  animation: hamsterHead var(--dur) ease-in-out infinite;
  background: hsl(30,90%,55%);
  border-radius: 70% 30% 0 100% / 40% 25% 25% 60%;
  box-shadow: 0 -0.25em 0 hsl(30,90%,80%) inset,
    0.75em -1.55em 0 hsl(30,90%,90%) inset;
  top: 0;
  left: -2em;
  width: 2.75em;
  height: 2.5em;
  transform-origin: 100% 50%;
}

.hamster__ear {
  animation: hamsterEar var(--dur) ease-in-out infinite;
  background: hsl(0,90%,85%);
  border-radius: 50%;
  box-shadow: -0.25em 0 hsl(30,90%,55%) inset;
  top: -0.25em;
  right: -0.25em;
  width: 0.75em;
  height: 0.75em;
  transform-origin: 50% 75%;
}

.hamster__eye {
  animation: hamsterEye var(--dur) linear infinite;
  background-color: hsl(0,0%,0%);
  border-radius: 50%;
  top: 0.375em;
  left: 1.25em;
  width: 0.5em;
  height: 0.5em;
}

.hamster__nose {
  background: hsl(0,90%,75%);
  border-radius: 35% 65% 85% 15% / 70% 50% 50% 30%;
  top: 0.75em;
  left: 0;
  width: 0.2em;
  height: 0.25em;
}

.hamster__body {
  animation: hamsterBody var(--dur) ease-in-out infinite;
  background: hsl(30,90%,90%);
  border-radius: 50% 30% 50% 30% / 15% 60% 40% 40%;
  box-shadow: 0.1em 0.75em 0 hsl(30,90%,55%) inset,
    0.15em -0.5em 0 hsl(30,90%,80%) inset;
  top: 0.25em;
  left: 2em;
  width: 4.5em;
  height: 3em;
  transform-origin: 17% 50%;
  transform-style: preserve-3d;
}

.hamster__limb--fr,
.hamster__limb--fl {
  clip-path: polygon(0 0,100% 0,70% 80%,60% 100%,0% 100%,40% 80%);
  top: 2em;
  left: 0.5em;
  width: 1em;
  height: 1.5em;
  transform-origin: 50% 0;
}

.hamster__limb--fr {
  animation: hamsterFRLimb var(--dur) linear infinite;
  background: linear-gradient(hsl(30,90%,80%) 80%,hsl(0,90%,75%) 80%);
  transform: rotate(15deg) translateZ(-1px);
}

.hamster__limb--fl {
  animation: hamsterFLLimb var(--dur) linear infinite;
  background: linear-gradient(hsl(30,90%,90%) 80%,hsl(0,90%,85%) 80%);
  transform: rotate(15deg);
}

.hamster__limb--br,
.hamster__limb--bl {
  border-radius: 0.75em 0.75em 0 0;
  clip-path: polygon(0 0,100% 0,100% 30%,70% 90%,70% 100%,30% 100%,40% 90%,0% 30%);
  top: 1em;
  left: 2.8em;
  width: 1.5em;
  height: 2.5em;
  transform-origin: 50% 30%;
}

.hamster__limb--br {
  animation: hamsterBRLimb var(--dur) linear infinite;
  background: linear-gradient(hsl(30,90%,80%) 90%,hsl(0,90%,75%) 90%);
  transform: rotate(-25deg) translateZ(-1px);
}

.hamster__limb--bl {
  animation: hamsterBLLimb var(--dur) linear infinite;
  background: linear-gradient(hsl(30,90%,90%) 90%,hsl(0,90%,85%) 90%);
  transform: rotate(-25deg);
}

.hamster__tail {
  animation: hamsterTail var(--dur) linear infinite;
  background: hsl(0,90%,85%);
  border-radius: 0.25em 50% 50% 0.25em;
  box-shadow: 0 -0.2em 0 hsl(0,90%,75%) inset;
  top: 1.5em;
  right: -0.5em;
  width: 1em;
  height: 0.5em;
  transform: rotate(30deg) translateZ(-1px);
  transform-origin: 0.25em 0.25em;
}

.spoke {
  animation: spoke var(--dur) linear infinite;
  background: radial-gradient(100% 100% at center,hsl(0,0%,60%) 4.8%,hsla(0,0%,60%,0) 5%),
    linear-gradient(hsla(0,0%,55%,0) 46.9%,hsl(0,0%,65%) 47% 52.9%,hsla(0,0%,65%,0) 53%) 50% 50% / 99% 99% no-repeat;
}

@keyframes hamster {
  from, to { transform: rotate(4deg) translate(-0.8em,1.85em); }
  50% { transform: rotate(0) translate(-0.8em,1.85em); }
}
@keyframes hamsterHead {
  from, 25%, 50%, 75%, to { transform: rotate(0); }
  12.5%, 37.5%, 62.5%, 87.5% { transform: rotate(8deg); }
}
@keyframes hamsterEye {
  from, 90%, to { transform: scaleY(1); }
  95% { transform: scaleY(0); }
}
@keyframes hamsterEar {
  from, 25%, 50%, 75%, to { transform: rotate(0); }
  12.5%, 37.5%, 62.5%, 87.5% { transform: rotate(12deg); }
}
@keyframes hamsterBody {
  from, 25%, 50%, 75%, to { transform: rotate(0); }
  12.5%, 37.5%, 62.5%, 87.5% { transform: rotate(-2deg); }
}
@keyframes hamsterFRLimb {
  from, 25%, 50%, 75%, to { transform: rotate(50deg) translateZ(-1px); }
  12.5%, 37.5%, 62.5%, 87.5% { transform: rotate(-30deg) translateZ(-1px); }
}
@keyframes hamsterFLLimb {
  from, 25%, 50%, 75%, to { transform: rotate(-30deg); }
  12.5%, 37.5%, 62.5%, 87.5% { transform: rotate(50deg); }
}
@keyframes hamsterBRLimb {
  from, 25%, 50%, 75%, to { transform: rotate(-60deg) translateZ(-1px); }
  12.5%, 37.5%, 62.5%, 87.5% { transform: rotate(20deg) translateZ(-1px); }
}
@keyframes hamsterBLLimb {
  from, 25%, 50%, 75%, to { transform: rotate(20deg); }
  12.5%, 37.5%, 62.5%, 87.5% { transform: rotate(-60deg); }
}
@keyframes hamsterTail {
  from, 25%, 50%, 75%, to { transform: rotate(30deg) translateZ(-1px); }
  12.5%, 37.5%, 62.5%, 87.5% { transform: rotate(10deg) translateZ(-1px); }
}
@keyframes spoke {
  from { transform: rotate(0); }
  to { transform: rotate(-1turn); }
}

/* 右侧内容区域 - 占20%，简约无框风格 */
.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8fafc;
  overflow-y: auto;
  padding: 30px 20px;
  position: relative;
}

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

  .left-text {
    left: 50%;
    bottom: 30px;
    transform: translateX(-50%);
    text-align: center;
    width: 90%;
  }

  .left-title { font-size: 28px; margin-bottom: 10px; }
  .left-sub { font-size: 16px; }

  .right-panel {
    flex: none;
    width: 100%;
    min-height: calc(100vh - 300px);
    padding: 40px 20px;
  }

  .right-panel::before { display: none; }
  .right-panel > div { max-width: 400px; margin: 0 auto; }
}

@media (min-width: 1600px) {
  .left-text { left: 60px; bottom: 60px; }
  .left-title { font-size: 42px; }
  .left-sub { font-size: 22px; }
}

@media (max-width: 480px) {
  .left-panel { height: 250px; }
  .left-text { bottom: 20px; }
  .left-title { font-size: 24px; }
  .left-sub { font-size: 14px; }
  .right-panel { padding: 30px 15px; }
  .welcome-title div:first-child { font-size: 24px; }
}

@media (max-height: 500px) {
  .left-text { bottom: 15px; }
  .left-title { font-size: 22px; margin-bottom: 8px; }
  .left-sub { font-size: 13px; }
}
</style>