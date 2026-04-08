<script setup>
import {ref, onMounted, onUnmounted} from 'vue'
import {get, post} from "@/net"
import {ElMessage} from "element-plus"

const courses = ref([])
const myCourses = ref([])

// 正常轮询间隔 (毫秒)
const NORMAL_INTERVAL = 400
// 报错后暂停间隔 (毫秒)
const ERROR_PAUSE_INTERVAL = 10000
// 存储定时器 ID，用于清理
let pollingTimer = null

// 封装 API 为 Promise 以便 async/await 调用
const loadAllCourses = () => {
  return new Promise((resolve, reject) => {
    get('/api/course/list', resolve, reject)
  }).then(data => {
    courses.value = data || []
  })
}

const loadMyCourses = () => {
  return new Promise((resolve, reject) => {
    get('/api/course/my-courses', resolve, reject)
  }).then(data => {
    myCourses.value = data || []
  })
}

const isEnrolled = (courseId) => myCourses.value.some(c => c.courseId === courseId)

const enrollCourse = (courseId) => {
  post('/api/course/enroll', {courseId}, () => {
    ElMessage.success('报名成功')
    loadMyCourses()
  }, () => {
    ElMessage.error('报名失败')
  })
}

// 核心轮询逻辑
const pollingLoop = async () => {
  try {
    // 并发请求两个接口
    await Promise.all([
      loadAllCourses(),
      loadMyCourses()
    ])
    // 成功：按正常频率继续轮询
    pollingTimer = setTimeout(pollingLoop, NORMAL_INTERVAL)
  } catch (error) {
    // 失败：暂停 10 秒后重试，避免频繁报错
    console.error('轮询失败，10秒后重试', error)
    pollingTimer = setTimeout(pollingLoop, ERROR_PAUSE_INTERVAL)
  }
}

const startPolling = () => {
  pollingLoop()
}

const stopPolling = () => {
  if (pollingTimer) {
    clearTimeout(pollingTimer)
    pollingTimer = null
  }
}

onMounted(() => {
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div class="all-courses">
    <div class="tab-header">
      <h2 class="header-title">探索课程</h2>
      <span class="course-count">{{ courses.length }} 门课程</span>
    </div>

    <div class="course-grid">
      <div v-for="course in courses" :key="course.id" class="glass-card">

        <!-- 标题 -->
        <h3 class="course-name">{{ course.name || '未命名课程' }}</h3>

        <!-- 描述 -->
        <p class="course-desc">{{ course.description || '暂无课程描述' }}</p>

        <!-- 讲师 & 时长 -->
        <div class="meta-row">
          <span class="meta-item">
            <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="13" height="13">
              <path d="M9 9a3.5 3.5 0 100-7 3.5 3.5 0 000 7zM2.5 16.5s-.5-5 6.5-5 6.5 5 6.5 5" stroke-width="1.6"
                    stroke-linecap="round"/>
            </svg>
            {{ course.teacherName || '未知讲师' }}
          </span>
          <span class="meta-item">
            <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="13" height="13">
              <circle cx="9" cy="9" r="7.5" stroke-width="1.6"/>
              <path d="M9 5v4l2.5 2" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
            {{ course.duration || 0 }}h
          </span>
        </div>

        <!-- 统计数字 — 立体凹陷块 -->
        <div class="stats-inset">
          <div class="stat-item">
            <span class="stat-n">{{ course.studentsCount || 0 }}</span>
            <span class="stat-l">学员</span>
          </div>
          <div class="stat-sep"></div>
          <div class="stat-item">
            <span class="stat-n">{{ course.rating || '—' }}</span>
            <span class="stat-l">评分</span>
          </div>
          <div class="stat-sep"></div>
          <div class="stat-item">
            <span class="stat-n">{{ course.maxCheckInCount || 12 }}</span>
            <span class="stat-l">签到</span>
          </div>
        </div>

        <!-- 报名按钮 -->
        <button v-if="!isEnrolled(course.id)" class="enroll-btn" @click="enrollCourse(course.id)">
          立即报名
        </button>
        <button v-else class="enrolled-btn" disabled>
          <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" width="14" height="14">
            <path d="M3.5 9.5l4 4 7-8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          已报名
        </button>
      </div>
    </div>

    <div v-if="courses.length === 0" class="empty-state">
      <div class="empty-icon-wrap">
        <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
          <rect x="6" y="6" width="36" height="36" rx="6" stroke-width="2"/>
          <path d="M16 18h16M16 28h10" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <p class="empty-title">暂无课程</p>
    </div>
  </div>
</template>

<style scoped>
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.all-courses {
  width: 100%;
  padding: 8px;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* ── Header ── */
.tab-header {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin-bottom: 28px;
}

.header-title {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: #1d1d1f;
}

.course-count {
  font-size: 14px;
  color: #86868b;
}

/* ── Grid ── */
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

/* ── Glass card — 与 ProfileView 完全一致 ── */
.glass-card {
  border-radius: 20px;
  padding: 22px;
  background: rgba(255, 255, 255, 0.52);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 2px 32px rgba(0, 0, 0, 0.10),
  0 0.5px 0 rgba(255, 255, 255, 0.95) inset;
  display: flex;
  flex-direction: column;
  gap: 14px;
  transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.22s ease;
}

.glass-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 40px rgba(0, 0, 0, 0.13),
  0 0.5px 0 rgba(255, 255, 255, 0.95) inset;
}

/* ── Course name ── */
.course-name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #1d1d1f;
  line-height: 1.4;
}

/* ── Description ── */
.course-desc {
  font-size: 13px;
  color: #6e6e73;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

/* ── Meta ── */
.meta-row {
  display: flex;
  gap: 18px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #86868b;
  font-weight: 500;
}

/* ── 统计凹陷块（立体凹槽感）── */
.stats-inset {
  display: flex;
  align-items: center;
  border-radius: 12px;
  padding: 14px 0;
  /* 凹陷：内阴影顶部暗底部亮 */
  background: rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.07) inset,
  0 1px 0 rgba(255, 255, 255, 0.88);
  border: 0.5px solid rgba(0, 0, 0, 0.06);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}

.stat-n {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.04em;
  color: #1d1d1f;
  line-height: 1;
}

.stat-l {
  font-size: 11px;
  color: #86868b;
}

.stat-sep {
  width: 0.5px;
  height: 30px;
  background: rgba(0, 0, 0, 0.08);
}

/* ── Buttons ── */
.enroll-btn, .enrolled-btn {
  width: 100%;
  padding: 11px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  outline: none;
  font-family: inherit;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: -0.01em;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.enroll-btn {
  background: rgba(29, 29, 31, 0.88);
  backdrop-filter: blur(8px);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.22), 0 0.5px 0 rgba(255, 255, 255, 0.12) inset;
}

.enroll-btn:hover {
  background: rgba(0, 0, 0, 0.95);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.28);
}

.enroll-btn:active {
  transform: scale(0.98);
}

.enrolled-btn {
  background: rgba(0, 0, 0, 0.05);
  color: #aeaeb2;
  cursor: not-allowed;
  border: 0.5px solid rgba(0, 0, 0, 0.06);
}

/* ── Empty ── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 80px 20px;
  text-align: center;
}

.empty-icon-wrap {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.52);
  backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c7c7cc;
  margin-bottom: 6px;
}

.empty-title {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
}

@media (max-width: 768px) {
  .course-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .header-title {
    font-size: 22px;
  }

  .glass-card {
    padding: 18px 16px;
  }
}
</style>