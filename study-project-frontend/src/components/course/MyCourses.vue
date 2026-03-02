<script setup>
import { ref, onMounted } from 'vue'
import { get, post } from "@/net"
import { ElMessage, ElMessageBox, ElRate } from "element-plus"

const myCourses = ref([])

const loadMyCourses = () => {
  console.log('开始加载我的课程...')
  get('/api/course/my-courses', data => {
    console.log('我的课程原始数据:', data)

    const processedData = (data || []).map(item => {
      console.log('处理课程关系:', {
        id: item.id,
        courseId: item.courseId,
        checkInCount: item.checkInCount,
        maxCheckInCount: item.maxCheckInCount,
        progress: item.progress,
        course: item.course
      })

      return {
        id: item.id || 0,
        courseId: item.courseId || item.id,
        progress: item.progress || 0,
        totalStudyTime: item.totalStudyTime || 0,
        checkInCount: item.checkInCount || 0,
        maxCheckInCount: item.course?.maxCheckInCount ?? 12,
        rating: item.rating || 0,
        review: item.review || '',
        lastCheckInDate: item.lastCheckInDate || null, // 添加最后签到日期
        course: item.course || {
          id: item.courseId || item.id,
          name: `课程 ${item.courseId || item.id}`,
          description: '课程描述加载中...',
          teacherName: '未知讲师',
          duration: 0,
          studentsCount: 0,
          rating: item.rating || 0,
          status: 'ACTIVE',
          maxCheckInCount: item.course?.maxCheckInCount ?? 12
        }
      }
    })

    console.log('处理后的我的课程数据:', processedData)
    myCourses.value = processedData
  }, error => {
    console.error('加载我的课程失败:', error)
    myCourses.value = []
  })
}

const unenrollCourse = (courseId) => {
  console.log('取消报名课程:', courseId)
  post('/api/course/unenroll', {courseId}, () => {
    ElMessage.success('取消报名成功')
    loadMyCourses()
  }, error => {
    console.error('取消报名失败:', error)
    ElMessage.error('取消报名失败')
  })
}

// 签到功能
const checkIn = (relation) => {
  console.log('签到课程:', {
    courseId: relation.courseId,
    courseName: relation.course?.name,
    currentCheckIn: relation.checkInCount,
    maxCheckIn: relation.maxCheckInCount
  })

  post('/api/course/check-in', {courseId: relation.courseId}, (response) => {
    ElMessage.success('签到成功！')
    loadMyCourses()
  }, error => {
    console.error('签到失败:', error)
    ElMessage.error('签到失败')
  })
}

// 评分相关状态
const showRatingDialog = ref(false)
const currentRatingCourse = ref(null)
const selectedRating = ref(0)
const ratingReview = ref('')
const isReRating = ref(false)

// 打开评分弹窗
const openRatingDialog = (relation, reRating = false) => {
  currentRatingCourse.value = relation
  selectedRating.value = relation.rating || 0
  ratingReview.value = relation.review || ''
  isReRating.value = reRating
  showRatingDialog.value = true
}

// 提交评分
const submitRating = () => {
  if (selectedRating.value === 0) {
    ElMessage.warning('请先选择评分星级')
    return
  }

  const courseId = currentRatingCourse.value.courseId
  const rating = selectedRating.value
  const review = ratingReview.value.trim()

  post('/api/course/rate', {
    courseId: courseId,
    rating: rating,
    review: review
  }, () => {
    ElMessage.success(isReRating.value ? '重新评分成功' : '评分成功')
    showRatingDialog.value = false;
    loadMyCourses()
  }, error => {
    console.error('评分失败:', error)
    ElMessage.error('评分失败')
  })
}

// 重新评分功能
const reRateCourse = (relation) => {
  openRatingDialog(relation, true)
}

// 计算签到进度百分比
const getCheckInProgress = (relation) => {
  const maxCheckIns = relation.maxCheckInCount || 12
  return Math.round((relation.checkInCount / maxCheckIns) * 100)
}

// 格式化签到次数显示
const formatCheckInCount = (relation) => {
  const maxCheckIns = relation.maxCheckInCount || 12
  return `${relation.checkInCount || 0}/${maxCheckIns}`
}

// 检查是否已完成所有签到
const isCheckInCompleted = (relation) => {
  const maxCheckIns = relation.maxCheckInCount || 12
  return relation.checkInCount >= maxCheckIns
}

// 检查今天是否已经签到过
const isTodayCheckedIn = (relation) => {
  if (!relation.lastCheckInDate) {
    return false
  }

  // 解析最后签到日期
  const lastCheckInDate = new Date(relation.lastCheckInDate)
  const today = new Date()

  // 比较年月日是否相同
  return lastCheckInDate.getFullYear() === today.getFullYear() &&
      lastCheckInDate.getMonth() === today.getMonth() &&
      lastCheckInDate.getDate() === today.getDate()
}

// 获取按钮状态文本
const getCheckInButtonText = (relation) => {
  if (isCheckInCompleted(relation)) {
    return '已完成'
  } else if (isTodayCheckedIn(relation)) {
    return '今日已签到'
  } else {
    return '签到'
  }
}

// 获取按钮是否禁用
const isCheckInDisabled = (relation) => {
  return isCheckInCompleted(relation) || isTodayCheckedIn(relation)
}

// 获取评分描述
const getRatingText = (value) => {
  const ratingTexts = {
    1: '很差',
    2: '较差',
    3: '一般',
    4: '较好',
    5: '很好'
  }
  return ratingTexts[value] || '未评分'
}

onMounted(() => {
  loadMyCourses()
})
</script>

<template>
  <div class="my-courses">
    <div class="tab-header">
      <div class="header-content">
        <h2 class="header-title">我的课程</h2>
        <span class="course-count">{{ myCourses.length }} 门课程</span>
      </div>
    </div>

    <div class="course-grid">
      <div
          v-for="relation in myCourses"
          :key="relation.id"
          class="course-card"
      >
        <div class="card-inner">
          <!-- 课程头部 -->
          <div class="course-header">
            <h3 class="course-name">{{ relation.course?.name || `课程 ${relation.courseId}` }}</h3>
            <div class="header-badge">
              <span v-if="relation.rating > 0" class="rating-badge">
                <svg class="star-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path
                      d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                </svg>
                {{ relation.rating }}
              </span>
            </div>
          </div>

          <!-- 进度条区域 -->
          <div class="progress-section">
            <div class="progress-header">
              <span class="progress-label">学习进度</span>
              <span class="progress-value">{{ relation.progress }}%</span>
            </div>
            <div class="progress-bar-container">
              <div class="progress-bar" :style="{ width: relation.progress + '%' }"></div>
            </div>
          </div>

          <!-- 签到区域 -->
          <div class="checkin-section">
            <div class="checkin-header">
              <div class="checkin-info">
                <svg class="checkin-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M9 11l3 3L22 4" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" stroke-width="2"
                        stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span class="checkin-label">签到进度</span>
              </div>
              <span class="checkin-count">{{ formatCheckInCount(relation) }}</span>
            </div>
            <div class="checkin-bar-container">
              <div
                  class="checkin-bar"
                  :class="{ 'completed': isCheckInCompleted(relation) }"
                  :style="{ width: getCheckInProgress(relation) + '%' }"
              ></div>
            </div>
          </div>

          <!-- 评价区域 -->
          <div v-if="relation.rating > 0 && relation.review" class="review-section">
            <div class="review-header">
              <svg class="quote-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M6 17h3l2-4V7H5v6h3zm8 0h3l2-4V7h-6v6h3z" opacity="0.3"/>
              </svg>
              <span class="review-label">我的评价</span>
            </div>
            <p class="review-text">{{ relation.review }}</p>
          </div>

          <!-- 操作按钮区域 -->
          <div class="actions-section">
            <button
                class="action-btn primary-btn"
                @click="checkIn(relation)"
                :disabled="isCheckInDisabled(relation)"
                :class="{ 'disabled': isCheckInDisabled(relation) }"
            >
              <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M9 11l3 3L22 4" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              {{ getCheckInButtonText(relation) }}
            </button>

            <div class="secondary-actions">
              <button
                  class="action-btn secondary-btn"
                  @click="openRatingDialog(relation, relation.rating > 0)"
              >
                <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
                        stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                {{ relation.rating > 0 ? '重新评分' : '评分' }}
              </button>

              <button
                  class="action-btn danger-btn"
                  @click="unenrollCourse(relation.courseId)"
              >
                <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M18 6L6 18M6 6l12 12" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                退出
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="myCourses.length === 0" class="empty-state">
      <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path
            d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
            stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <p class="empty-title">还没有报名课程</p>
      <p class="empty-desc">前往"探索课程"开始学习之旅</p>
    </div>

    <!-- 评分弹窗 -->
    <el-dialog
        :title="isReRating ? '重新评分' : '课程评分'"
        v-model="showRatingDialog"
        width="500px"
        :close-on-click-modal="false"
        class="rating-dialog-wrapper"
    >
      <div v-if="currentRatingCourse" class="rating-dialog">
        <div class="rating-course-info">
          <h3 class="dialog-course-name">{{
              currentRatingCourse.course?.name || `课程 ${currentRatingCourse.courseId}`
            }}</h3>
        </div>

        <div class="rating-section">
          <div class="rating-title">评分</div>
          <div class="rating-stars">
            <el-rate
                v-model="selectedRating"
                :colors="['#8e8e93', '#1a1a1a', '#1a1a1a']"
                show-score
                :score-template="`${selectedRating} 分 · ${getRatingText(selectedRating)}`"
            />
          </div>
          <div class="rating-hint">
            请根据课程质量和学习体验进行评分
          </div>
        </div>

        <div class="review-section-dialog">
          <div class="review-title">评价 <span class="optional-tag">选填</span></div>
          <el-input
              v-model="ratingReview"
              type="textarea"
              :rows="4"
              placeholder="分享您的学习感受和建议..."
              maxlength="500"
              show-word-limit
              class="review-textarea"
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <button class="dialog-btn cancel-btn" @click="showRatingDialog = false">
            取消
          </button>
          <button
              class="dialog-btn confirm-btn"
              @click="submitRating"
              :disabled="selectedRating === 0"
              :class="{ 'disabled': selectedRating === 0 }"
          >
            {{ isReRating ? '确认修改' : '提交评分' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.my-courses {
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
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
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
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.course-name {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  line-height: 1.4;
  letter-spacing: -0.3px;
  flex: 1;
}

.header-badge {
  display: flex;
  gap: 8px;
}

.rating-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #1a1a1a;
  color: #ffffff;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.star-icon {
  width: 14px;
  height: 14px;
}

/* 进度条样式 */
.progress-section {
  background: #f8f8f8;
  padding: 16px;
  border-radius: 12px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.progress-label {
  font-size: 13px;
  color: #6e6e73;
  font-weight: 500;
}

.progress-value {
  font-size: 16px;
  color: #1a1a1a;
  font-weight: 600;
  letter-spacing: -0.3px;
}

.progress-bar-container {
  height: 6px;
  background: #e5e5e5;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #1a1a1a 0%, #4a4a4a 100%);
  border-radius: 3px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 签到区域样式 */
.checkin-section {
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
}

.checkin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.checkin-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.checkin-icon {
  width: 16px;
  height: 16px;
  color: #6e6e73;
}

.checkin-label {
  font-size: 13px;
  color: #6e6e73;
  font-weight: 500;
}

.checkin-count {
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 600;
}

.checkin-bar-container {
  height: 6px;
  background: #e5e5e5;
  border-radius: 3px;
  overflow: hidden;
}

.checkin-bar {
  height: 100%;
  background: linear-gradient(90deg, #666666 0%, #1a1a1a 100%);
  border-radius: 3px;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.checkin-bar.completed {
  background: linear-gradient(90deg, #34c759 0%, #30a14e 100%);
}

/* 评价区域 */
.review-section {
  padding: 16px;
  background: #f8f8f8;
  border-radius: 12px;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.quote-icon {
  width: 16px;
  height: 16px;
  color: #8e8e93;
}

.review-label {
  font-size: 12px;
  color: #8e8e93;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.review-text {
  font-size: 14px;
  color: #6e6e73;
  line-height: 1.6;
  margin: 0;
  font-style: italic;
}

/* 操作按钮区域 */
.actions-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  outline: none;
}

.btn-icon {
  width: 16px;
  height: 16px;
}

.primary-btn {
  background: #1a1a1a;
  color: #ffffff;
}

.primary-btn:hover:not(.disabled) {
  background: #000000;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.primary-btn.disabled {
  background: #f5f5f7;
  color: #8e8e93;
  cursor: not-allowed;
}

.secondary-actions {
  display: flex;
  gap: 8px;
}

.secondary-btn {
  flex: 1;
  background: #ffffff;
  color: #1a1a1a;
  border: 1px solid #e0e0e0;
}

.secondary-btn:hover {
  background: #f8f8f8;
  border-color: #d0d0d0;
}

.danger-btn {
  background: #ffffff;
  color: #ff3b30;
  border: 1px solid #ffcccb;
}

.danger-btn:hover {
  background: #fff5f5;
  border-color: #ffb3b0;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: #d1d1d6;
  stroke-width: 1.5;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 8px 0 0 0;
}

.empty-desc {
  font-size: 14px;
  color: #8e8e93;
  margin: 0;
}

/* 评分弹窗样式 */
:deep(.rating-dialog-wrapper .el-dialog) {
  border-radius: 16px;
}

:deep(.rating-dialog-wrapper .el-dialog__header) {
  padding: 24px 24px 0;
  border-bottom: none;
}

:deep(.rating-dialog-wrapper .el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

:deep(.rating-dialog-wrapper .el-dialog__body) {
  padding: 20px 24px;
}

.rating-dialog {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.rating-course-info {
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-course-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.rating-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rating-title,
.review-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.optional-tag {
  font-size: 11px;
  font-weight: 400;
  color: #8e8e93;
  background: #f5f5f7;
  padding: 2px 8px;
  border-radius: 4px;
}

.rating-stars {
  display: flex;
  align-items: center;
}

:deep(.rating-stars .el-rate) {
  height: auto;
}

:deep(.rating-stars .el-rate__icon) {
  font-size: 28px;
  margin-right: 8px;
}

:deep(.rating-stars .el-rate__text) {
  font-size: 14px;
  color: #6e6e73;
  margin-left: 12px;
}

.rating-hint {
  font-size: 12px;
  color: #8e8e93;
  line-height: 1.5;
}

.review-section-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

:deep(.review-textarea .el-textarea__inner) {
  border-radius: 10px;
  border-color: #e0e0e0;
  font-size: 14px;
  line-height: 1.6;
}

:deep(.review-textarea .el-textarea__inner:focus) {
  border-color: #1a1a1a;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.dialog-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  outline: none;
}

.cancel-btn {
  background: #f5f5f7;
  color: #1a1a1a;
}

.cancel-btn:hover {
  background: #e8e8ea;
}

.confirm-btn {
  background: #1a1a1a;
  color: #ffffff;
}

.confirm-btn:hover:not(.disabled) {
  background: #000000;
}

.confirm-btn.disabled {
  background: #d1d1d6;
  cursor: not-allowed;
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

  .secondary-actions {
    flex-direction: column;
  }

  .secondary-btn {
    flex: auto;
  }
}
</style>
