<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {get, post} from "@/net"
import {ElMessage, ElRate} from "element-plus"
import ConfirmDeleteModal from '@/components/common/ConfirmDeleteModal.vue'

const router = useRouter()
const myCourses = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 9
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

const loadMyCourses = () => {
  const params = `page=${page.value}&pageSize=${pageSize}`
  get('/api/course/my-courses?' + params, data => {
    const list = data?.list || data || []
    total.value = data?.total ?? list.length
    myCourses.value = list.map(item => ({
      id: item.id || 0,
      courseId: item.courseId || item.id,
      progress: item.progress || 0,
      totalStudyTime: item.totalStudyTime || 0,
      checkInCount: item.checkInCount || 0,
      maxCheckInCount: item.course?.maxCheckInCount ?? 12,
      rating: item.rating || 0,
      review: item.review || '',
      lastCheckInDate: item.lastCheckInDate || null,
      course: item.course || {
        id: item.courseId || item.id,
        name: `课程 ${item.courseId || item.id}`,
        description: '课程描述加载中...',
        teacherName: '未知讲师',
        duration: 0,
        studentsCount: 0,
        rating: item.rating || 0,
        status: 'ACTIVE',
        maxCheckInCount: 12
      }
    }))
  }, () => {
    myCourses.value = []
  })
}

const goToPage = (p) => {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  loadMyCourses()
}

const pageNumbers = computed(() => {
  const t = totalPages.value, c = page.value
  const pages = []
  const start = Math.max(1, c - 2), end = Math.min(t, c + 2)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const unenrollCourse = (courseId, courseName) => {
  unenrollTarget.value = courseId
  unenrollMessage.value = '确定要退出课程「' + courseName + '」吗？退出后将删除你的学习记录。'
  showUnenrollModal.value = true
}

const showUnenrollModal = ref(false)
const unenrollTarget = ref(null)
const unenrollMessage = ref('')
const handleUnenrollConfirm = () => {
  post('/api/course/unenroll', {courseId: unenrollTarget.value}, () => {
    ElMessage.success('已退出课程');
    loadMyCourses()
  }, () => {
    ElMessage.error('退出失败')
  })
}

const checkIn = (relation) => {
  post('/api/course/check-in', {courseId: relation.courseId}, () => {
    ElMessage.success('签到成功！');
    loadMyCourses()
  }, () => {
    ElMessage.error('签到失败')
  })
}

const showRatingDialog = ref(false)
const currentRatingCourse = ref(null)
const selectedRating = ref(0)
const ratingReview = ref('')
const isReRating = ref(false)
const isAnonymous = ref(false)

const openRatingDialog = (relation, reRating = false) => {
  currentRatingCourse.value = relation
  selectedRating.value = relation.rating || 0
  ratingReview.value = relation.review || ''
  isReRating.value = reRating
  isAnonymous.value = false
  showRatingDialog.value = true
}

const submitRating = () => {
  if (selectedRating.value === 0) {
    ElMessage.warning('请先选择评分星级');
    return
  }
  post('/api/course/rate', {
    courseId: currentRatingCourse.value.courseId,
    rating: selectedRating.value,
    review: ratingReview.value.trim(),
    anonymous: isAnonymous.value
  }, () => {
    ElMessage.success(isReRating.value ? '重新评分成功' : '评分成功')
    showRatingDialog.value = false;
    loadMyCourses()
  }, () => {
    ElMessage.error('评分失败')
  })
}

const getCheckInProgress = (r) => Math.round((r.checkInCount / (r.maxCheckInCount || 12)) * 100)
const formatCheckInCount = (r) => `${r.checkInCount || 0}/${r.maxCheckInCount || 12}`
const isCheckInCompleted = (r) => r.checkInCount >= (r.maxCheckInCount || 12)
const isTodayCheckedIn = (r) => {
  if (!r.lastCheckInDate) return false
  const d = new Date(r.lastCheckInDate), t = new Date()
  return d.getFullYear() === t.getFullYear() && d.getMonth() === t.getMonth() && d.getDate() === t.getDate()
}
const getCheckInButtonText = (r) => isCheckInCompleted(r) ? '已完成' : isTodayCheckedIn(r) ? '今日已签到' : '签到'
const isCheckInDisabled = (r) => isCheckInCompleted(r) || isTodayCheckedIn(r)
const getRatingText = (v) => ({1: '很差', 2: '较差', 3: '一般', 4: '较好', 5: '很好'}[v] || '未评分')

onMounted(() => {
  loadMyCourses()
})
</script>

<template>
  <div class="my-courses">
    <div class="tab-header">
      <h2 class="header-title">我的课程</h2>
      <span class="course-count">共 {{ total }} 门课程</span>
    </div>

    <div class="course-grid">
      <div v-for="relation in myCourses" :key="relation.id" class="glass-card">

        <div class="course-head">
          <h3 class="course-name">{{ relation.course?.name || `课程 ${relation.courseId}` }}</h3>
          <span v-if="relation.rating > 0" class="rating-pill">
            <svg viewBox="0 0 16 16" fill="currentColor" width="11" height="11">
              <path d="M8 1l1.85 3.75L14 5.5l-3 2.92.71 4.13L8 10.4l-3.71 2.15L5 8.42 2 5.5l4.15-.75z"/>
            </svg>
            {{ relation.rating }}
          </span>
        </div>

        <div class="inset-block">
          <div class="inset-row">
            <span class="inset-label">学习进度</span>
            <span class="inset-val">{{ relation.progress }}%</span>
          </div>
          <div class="bar-track">
            <div class="bar-fill bar-progress" :style="{ width: relation.progress + '%' }"></div>
          </div>
        </div>

        <div class="inset-block">
          <div class="inset-row">
            <span class="inset-label">签到进度</span>
            <span class="inset-val">{{ formatCheckInCount(relation) }}</span>
          </div>
          <div class="bar-track">
            <div class="bar-fill"
                 :class="isCheckInCompleted(relation) ? 'bar-done' : 'bar-checkin'"
                 :style="{ width: getCheckInProgress(relation) + '%' }"></div>
          </div>
        </div>

        <div v-if="relation.rating > 0 && relation.review" class="review-block">
          <span class="review-quote">"</span>
          <p class="review-text">{{ relation.review }}</p>
        </div>

        <div class="actions">
          <button class="btn btn-primary"
                  @click="checkIn(relation)"
                  :disabled="isCheckInDisabled(relation)"
                  :class="{ 'btn-disabled': isCheckInDisabled(relation) }">
            <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" width="14" height="14">
              <path d="M5 10l4 4L15 6" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            {{ getCheckInButtonText(relation) }}
          </button>
          <div class="btn-row">
            <button class="btn btn-learn" @click="router.push('/course/' + relation.courseId)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="14" height="14">
                <polygon points="5 3 19 12 5 21 5 3" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              去学习
            </button>
            <button class="btn btn-ghost" @click="openRatingDialog(relation, relation.rating > 0)">
              <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" width="13" height="13">
                <path d="M10 1.5l2.06 4.17 4.6.67-3.33 3.24.79 4.59L10 11.9l-4.12 2.17.79-4.59L3.34 6.34l4.6-.67z"
                      stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              {{ relation.rating > 0 ? '改评分' : '评分' }}
            </button>
            <button class="btn btn-danger" @click="unenrollCourse(relation.courseId, relation.course?.name)">
              <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" width="13" height="13">
                <path d="M14 6L6 14M6 6l8 8" stroke-width="2" stroke-linecap="round"/>
              </svg>
              退出
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button class="pg-btn" :disabled="page <= 1" @click="goToPage(page - 1)">
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" width="12" height="12"><path d="M10 3L5 8l5 5" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
      <template v-if="pageNumbers[0] > 1">
        <button class="pg-btn" @click="goToPage(1)">1</button>
        <span v-if="pageNumbers[0] > 2" class="pg-dots">…</span>
      </template>
      <button v-for="p in pageNumbers" :key="p" class="pg-btn" :class="{ active: p === page }" @click="goToPage(p)">{{ p }}</button>
      <template v-if="pageNumbers[pageNumbers.length - 1] < totalPages">
        <span v-if="pageNumbers[pageNumbers.length - 1] < totalPages - 1" class="pg-dots">…</span>
        <button class="pg-btn" @click="goToPage(totalPages)">{{ totalPages }}</button>
      </template>
      <button class="pg-btn" :disabled="page >= totalPages" @click="goToPage(page + 1)">
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" width="12" height="12"><path d="M6 3l5 5-5 5" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
    </div>

    <div v-if="myCourses.length === 0" class="empty-state">
      <div class="empty-icon-wrap">
        <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
          <path d="M24 10c-6 0-10 3-10 3v22s4-3 10-3 10 3 10 3V13s-4-3-10-3z" stroke-width="2" stroke-linecap="round"
                stroke-linejoin="round"/>
          <path d="M24 10v22" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <p class="empty-title">还没有报名课程</p>
      <p class="empty-desc">前往「探索课程」开始学习之旅</p>
    </div>

    <!-- 评分弹窗 -->
    <el-dialog :title="isReRating ? '重新评分' : '课程评分'"
               v-model="showRatingDialog" width="460px"
               :close-on-click-modal="false"
               :append-to-body="true"
               align-center
               class="glass-dialog">
      <div v-if="currentRatingCourse" class="dialog-body">
        <p class="dialog-course-name">{{
            currentRatingCourse.course?.name || `课程 ${currentRatingCourse.courseId}`
          }}</p>
        <div class="dialog-section">
          <label class="sec-title">评分</label>
          <el-rate v-model="selectedRating"
                   :colors="['#d1d1d6','#1d1d1f','#1d1d1f']"
                   show-score
                   :score-template="`${selectedRating} 分 · ${getRatingText(selectedRating)}`"/>
          <p class="dialog-hint">请根据课程质量和学习体验评分</p>
        </div>
        <div class="dialog-section">
          <label class="sec-title">评价 <span class="opt-tag">选填</span></label>
          <el-input v-model="ratingReview" type="textarea" :rows="4"
                    placeholder="分享您的学习感受和建议…" maxlength="500" show-word-limit class="glass-textarea"/>
        </div>
        <div class="dialog-section">
          <label class="anonymous-toggle" @click="isAnonymous = !isAnonymous">
            <span class="toggle-check" :class="{ active: isAnonymous }">
              <svg v-if="isAnonymous" viewBox="0 0 20 20" fill="currentColor" width="12" height="12">
                <path d="M5 10l4 4L15 6" stroke="white" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <span class="toggle-label">匿名评价</span>
            <span class="toggle-hint">社区中不会显示你的用户名和头像</span>
          </label>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="dlg-btn dlg-cancel" @click="showRatingDialog = false">取消</button>
          <button class="dlg-btn dlg-confirm" @click="submitRating"
                  :disabled="selectedRating === 0" :class="{ 'dlg-disabled': selectedRating === 0 }">
            {{ isReRating ? '确认修改' : '提交评分' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <ConfirmDeleteModal v-model="showUnenrollModal" :message="unenrollMessage" @confirm="handleUnenrollConfirm" />
  </div>
</template>

<style scoped>
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.my-courses {
  width: 100%;
  padding: 8px;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
}

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

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

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

.course-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.course-name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #1d1d1f;
  line-height: 1.35;
  flex: 1;
}

.rating-pill {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px 9px;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  color: #fff;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.inset-block {
  background: rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 13px 14px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06) inset,
  0 1px 0 rgba(255, 255, 255, 0.85);
  border: 0.5px solid rgba(0, 0, 0, 0.06);
}

.inset-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 9px;
}

.inset-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #86868b;
}

.inset-val {
  font-size: 14px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.02em;
}

.bar-track {
  height: 5px;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 3px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1) inset;
}

.bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.bar-progress {
  background: linear-gradient(90deg, #3a3a3c, #1d1d1f);
}

.bar-checkin {
  background: linear-gradient(90deg, #6e6e73, #3a3a3c);
}

.bar-done {
  background: linear-gradient(90deg, #34c759, #28a745);
}

.review-block {
  position: relative;
  background: rgba(255, 255, 255, 0.38);
  backdrop-filter: blur(10px);
  border: 0.5px solid rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 11px 14px 11px 26px;
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.9) inset;
}

.review-quote {
  position: absolute;
  top: 2px;
  left: 9px;
  font-size: 26px;
  font-family: Georgia, serif;
  color: rgba(0, 0, 0, 0.1);
  line-height: 1;
}

.review-text {
  font-size: 13px;
  color: #6e6e73;
  line-height: 1.6;
  font-style: italic;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.btn-row {
  display: flex;
  gap: 8px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  outline: none;
  font-family: inherit;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: -0.01em;
}

.btn-primary {
  background: rgba(29, 29, 31, 0.88);
  backdrop-filter: blur(8px);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.22), 0 0.5px 0 rgba(255, 255, 255, 0.12) inset;
}

.btn-primary:hover:not(:disabled):not(.btn-disabled) {
  background: rgba(0, 0, 0, 0.95);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.28);
}

.btn-disabled {
  background: rgba(0, 0, 0, 0.06) !important;
  color: #aeaeb2 !important;
  cursor: not-allowed;
  box-shadow: none !important;
  transform: none !important;
}

.btn-learn {
  flex: 1;
  background: rgba(29,29,31,0.72);
  backdrop-filter: blur(12px);
  color: #fff;
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.btn-learn:hover {
  background: rgba(0,0,0,0.82);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.22);
}

.btn-ghost {
  flex: 1;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  color: #1d1d1f;
  border: 0.5px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.95) inset, 0 2px 8px rgba(0, 0, 0, 0.06);
}

.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.78);
  transform: translateY(-1px);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.95) inset, 0 4px 12px rgba(0, 0, 0, 0.09);
}

.btn-danger {
  flex: 1;
  background: rgba(255, 59, 48, 0.08);
  backdrop-filter: blur(12px);
  color: #ff3b30;
  border: 0.5px solid rgba(255, 59, 48, 0.2);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.9) inset;
}

.btn-danger:hover {
  background: rgba(255, 59, 48, 0.14);
  transform: translateY(-1px);
}

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

.empty-desc {
  font-size: 14px;
  color: #86868b;
}

/* ── Dialog ── */
:deep(.glass-dialog .el-dialog) {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: saturate(180%) blur(40px);
  -webkit-backdrop-filter: saturate(180%) blur(40px);
  border: 0.5px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.16);
}

:deep(.glass-dialog .el-dialog__header) {
  padding: 22px 24px 0;
  border-bottom: none;
}

:deep(.glass-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.02em;
}

:deep(.glass-dialog .el-dialog__body) {
  padding: 18px 24px;
}

.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dialog-course-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  padding-bottom: 16px;
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.07);
}

.dialog-section {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.sec-title {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: #86868b;
  display: flex;
  align-items: center;
  gap: 7px;
}

.opt-tag {
  font-size: 11px;
  font-weight: 400;
  text-transform: none;
  letter-spacing: 0;
  color: #aeaeb2;
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 7px;
  border-radius: 4px;
}

.dialog-hint {
  font-size: 12px;
  color: #aeaeb2;
}

.anonymous-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 10px 14px;
  background: rgba(0,0,0,0.03);
  border-radius: 10px;
  transition: background 0.15s;
  user-select: none;
}

.anonymous-toggle:hover {
  background: rgba(0,0,0,0.06);
}

.toggle-check {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 1.5px solid #d1d1d6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.15s;
}

.toggle-check.active {
  background: #1d1d1f;
  border-color: #1d1d1f;
}

.toggle-label {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

.toggle-hint {
  font-size: 12px;
  color: #aeaeb2;
  margin-left: auto;
}

:deep(.glass-textarea .el-textarea__inner) {
  background: rgba(255, 255, 255, 0.48);
  backdrop-filter: blur(10px);
  border: 0.5px solid rgba(255, 255, 255, 0.8);
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
  font-family: inherit;
  resize: none;
}

:deep(.glass-textarea .el-textarea__inner:focus) {
  border-color: rgba(0, 113, 227, 0.4);
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.12);
}

.dialog-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.dlg-btn {
  padding: 9px 22px;
  border-radius: 980px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  font-family: inherit;
  transition: all 0.15s;
}

.dlg-cancel {
  background: rgba(0, 0, 0, 0.06);
  color: #1d1d1f;
}

.dlg-cancel:hover {
  background: rgba(0, 0, 0, 0.10);
}

.dlg-confirm {
  background: rgba(29, 29, 31, 0.88);
  backdrop-filter: blur(8px);
  color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.dlg-confirm:hover:not(.dlg-disabled) {
  background: rgba(0, 0, 0, 0.95);
}

.dlg-disabled {
  opacity: 0.38;
  cursor: not-allowed;
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

  .btn-row {
    flex-direction: column;
  }
}

/* ── Pagination ── */
.pagination { display: flex; align-items: center; justify-content: center; gap: 6px; margin-top: 28px; padding: 16px 0; }
.pg-btn {
  min-width: 36px; height: 36px; border-radius: 10px; border: none;
  background: rgba(255,255,255,0.55); backdrop-filter: blur(12px);
  border: 0.5px solid rgba(255,255,255,0.8);
  box-shadow: 0 1px 0 rgba(255,255,255,0.95) inset, 0 2px 8px rgba(0,0,0,0.06);
  color: #1d1d1f; font-size: 14px; font-weight: 600; font-family: inherit;
  cursor: pointer; transition: all 0.15s;
  display: flex; align-items: center; justify-content: center;
}
.pg-btn:hover:not(:disabled):not(.active) { background: rgba(255,255,255,0.78); transform: translateY(-1px); }
.pg-btn.active { background: rgba(29,29,31,0.88); color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.22); }
.pg-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.pg-dots { color: #86868b; font-size: 14px; padding: 0 2px; }
</style>