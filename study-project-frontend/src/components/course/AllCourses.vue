<script setup>
import { ref, onMounted, computed } from 'vue'
import { get, post } from "@/net"
import { ElMessage } from "element-plus"

const courses = ref([])
const myCourses = ref([]) // 添加我的课程列表用于检查报名状态

const loadAllCourses = () => {
  console.log('开始加载所有课程...')
  get('/api/course/list', data => {
    console.log('所有课程数据:', data)
    courses.value = data || []

    // 检查课程信息完整性
    courses.value.forEach(course => {
      console.log('课程详情:', {
        id: course.id,
        name: course.name,
        description: course.description,
        teacherName: course.teacherName,
        duration: course.duration,
        studentsCount: course.studentsCount,
        rating: course.rating,
        maxCheckInCount: course.maxCheckInCount,
        status: course.status
      })
    })
  }, error => {
    console.error('加载所有课程失败:', error)
    courses.value = []
  })
}

// 加载我的课程用于检查报名状态
const loadMyCourses = () => {
  get('/api/course/my-courses', data => {
    console.log('我的课程数据用于检查报名状态:', data)
    myCourses.value = data || []
  }, error => {
    console.error('加载我的课程失败:', error)
    myCourses.value = []
  })
}

// 检查是否已经报名
const isEnrolled = (courseId) => {
  return myCourses.value.some(course => course.courseId === courseId)
}

const enrollCourse = (courseId) => {
  console.log('报名课程:', courseId)
  post('/api/course/enroll', {courseId}, () => {
    ElMessage.success('报名成功')

    // 报名成功后重新加载我的课程列表
    loadMyCourses()
  }, error => {
    console.error('报名失败:', error)
    ElMessage.error('报名失败')
  })
}

onMounted(() => {
  loadAllCourses()
  loadMyCourses() // 同时加载我的课程
})
</script>

<template>
  <div class="all-courses">
    <div class="tab-header">
      <div class="header-content">
        <h2 class="header-title">探索课程</h2>
        <span class="course-count">{{ courses.length }} 门课程</span>
      </div>
    </div>

    <div class="course-grid">
      <div
          v-for="course in courses"
          :key="course.id"
          class="course-card"
      >
        <div class="card-inner">
          <div class="course-header">
            <h3 class="course-name">{{ course.name || '未命名课程' }}</h3>
          </div>

          <div class="course-content">
            <p class="course-desc">{{ course.description || '暂无课程描述' }}</p>

            <div class="divider"></div>

            <div class="course-meta">
              <div class="meta-item">
                <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" stroke-width="2"
                        stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>{{ course.teacherName || '未知讲师' }}</span>
              </div>

              <div class="meta-item">
                <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <circle cx="12" cy="12" r="10" stroke-width="2"/>
                  <path d="M12 6v6l4 2" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <span>{{ course.duration || 0 }}h</span>
              </div>
            </div>

            <div class="course-stats">
              <div class="stat-item">
                <span class="stat-value">{{ course.studentsCount || 0 }}</span>
                <span class="stat-label">学员</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ course.rating || 0 }}</span>
                <span class="stat-label">评分</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ course.maxCheckInCount || 12 }}</span>
                <span class="stat-label">签到</span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <button
                v-if="!isEnrolled(course.id)"
                class="enroll-btn"
                @click="enrollCourse(course.id)"
            >
              立即报名
            </button>
            <button
                v-else
                class="enrolled-btn"
                disabled
            >
              <svg class="check-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M5 13l4 4L19 7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              已报名
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="courses.length === 0" class="empty-state">
      <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <rect x="3" y="3" width="18" height="18" rx="2" stroke-width="2"/>
        <path d="M9 9h6M9 15h6" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <p class="empty-text">暂无课程</p>
    </div>
  </div>
</template>

<style scoped>
.all-courses {
  width: 100%;
  padding: 8px;
}

.tab-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.header-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  letter-spacing: -0.5px;
}

.course-count {
  font-size: 14px;
  color: #8e8e93;
  font-weight: 400;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.course-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #f0f0f0;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
  border-color: #e0e0e0;
}

.card-inner {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.course-header {
  min-height: 56px;
}

.course-name {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  line-height: 1.4;
  letter-spacing: -0.3px;
}

.course-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
}

.course-desc {
  color: #6e6e73;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 44px;
}

.divider {
  height: 1px;
  background: linear-gradient(90deg, #f0f0f0 0%, transparent 100%);
  margin: 4px 0;
}

.course-meta {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6e6e73;
  font-size: 13px;
  font-weight: 500;
}

.meta-icon {
  width: 16px;
  height: 16px;
  color: #8e8e93;
}

.course-stats {
  display: flex;
  gap: 24px;
  padding-top: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: -0.3px;
}

.stat-label {
  font-size: 12px;
  color: #8e8e93;
  font-weight: 400;
}

.card-footer {
  padding-top: 4px;
}

.enroll-btn,
.enrolled-btn {
  width: 100%;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  outline: none;
}

.enroll-btn {
  background: #1a1a1a;
  color: #ffffff;
}

.enroll-btn:hover {
  background: #000000;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.enroll-btn:active {
  transform: translateY(0);
}

.enrolled-btn {
  background: #f5f5f7;
  color: #6e6e73;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: not-allowed;
}

.check-icon {
  width: 16px;
  height: 16px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: #d1d1d6;
  stroke-width: 1.5;
}

.empty-text {
  font-size: 16px;
  color: #8e8e93;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .header-title {
    font-size: 24px;
  }

  .course-card {
    border-radius: 12px;
  }

  .card-inner {
    padding: 20px;
  }
}
</style>