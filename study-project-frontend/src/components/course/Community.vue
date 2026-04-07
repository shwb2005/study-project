<script setup>
import {ref, computed, onMounted} from 'vue'
import {get} from '@/net'

const reviews = ref([])

const courseFilter = ref('all')
const ratingFilter = ref('all')

const courseOptions = computed(() => {
  const courses = [...new Set(reviews.value.map(r => r.courseName))]
  return courses.map(name => ({label: name, value: name}))
})

const ratingOptions = [
  {label: '全部评分', value: 'all'},
  {label: '5星', value: '5'},
  {label: '4星及以上', value: '4'},
  {label: '3星及以上', value: '3'},
]

const filteredReviews = computed(() => {
  return reviews.value.filter(r => {
    if (courseFilter.value !== 'all' && r.courseName !== courseFilter.value) return false
    if (ratingFilter.value !== 'all' && r.rating < Number(ratingFilter.value)) return false
    return true
  })
})

const getInitials = (name) => name ? name.charAt(0) : '?'
const getRatingText = (v) => ({1: '很差', 2: '较差', 3: '一般', 4: '较好', 5: '很好'}[v] || '')
const formatDate = (d) => {
  if (!d) return ''
  const date = d.replace('T', ' ').split(' ')[0]
  return date.replace(/-/g, '/')
}
const avgRating = computed(() => {
  if (filteredReviews.value.length === 0) return '0.0'
  return (filteredReviews.value.reduce((s, r) => s + r.rating, 0) / filteredReviews.value.length).toFixed(1)
})

onMounted(() => {
  get('/api/course/community', (data) => {
    reviews.value = data || []
  })
})
</script>

<template>
  <div class="community">
    <div class="tab-header">
      <h2 class="header-title">学习社区</h2>
      <span class="review-count">{{ filteredReviews.length }} 条评价</span>
    </div>

    <!-- Stats bar -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-n">{{ filteredReviews.length }}</span>
        <span class="stat-l">条评价</span>
      </div>
      <template v-if="courseFilter !== 'all'">
        <div class="stat-sep"></div>
        <div class="stat-item">
          <span class="stat-n">{{ avgRating }}</span>
          <span class="stat-l">平均评分</span>
        </div>
      </template>
      <div class="stat-sep"></div>
      <div class="stat-item">
        <span class="stat-n">{{ courseOptions.length }}</span>
        <span class="stat-l">门课程</span>
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-bar">
      <div class="filter-select-wrap">
        <select v-model="courseFilter" class="filter-select">
          <option value="all">全部课程</option>
          <option v-for="c in courseOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
        </select>
        <svg class="select-arrow" viewBox="0 0 12 12" fill="none" stroke="currentColor" width="12" height="12">
          <path d="M3 5l3 3 3-3" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <div class="filter-select-wrap">
        <select v-model="ratingFilter" class="filter-select">
          <option v-for="r in ratingOptions" :key="r.value" :value="r.value">{{ r.label }}</option>
        </select>
        <svg class="select-arrow" viewBox="0 0 12 12" fill="none" stroke="currentColor" width="12" height="12">
          <path d="M3 5l3 3 3-3" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    </div>

    <!-- Review List -->
    <div class="review-list">
      <div v-for="review in filteredReviews" :key="review.id" class="review-card">
        <div class="review-left">
          <div class="avatar" :class="{ 'avatar-anonymous': review.isAnonymous }">
            <template v-if="review.isAnonymous">
              <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
                <path d="M10 2a5 5 0 100 10 5 5 0 000-10zm0 11c-5.33 0-8 2.67-8 4v1h16v-1c0-1.33-2.67-4-8-4z" opacity="0.5"/>
              </svg>
            </template>
            <template v-else>{{ getInitials(review.username) }}</template>
          </div>
        </div>
        <div class="review-body">
          <div class="review-header">
            <span class="review-user">{{ review.isAnonymous ? '匿名用户' : review.username }}</span>
            <span class="review-date">{{ formatDate(review.createdAt) }}</span>
          </div>
          <div class="review-meta">
            <span class="course-tag">{{ review.courseName }}</span>
            <div class="stars">
              <svg v-for="i in 5" :key="i" viewBox="0 0 16 16" width="14" height="14"
                   :fill="i <= review.rating ? '#ff9500' : 'none'"
                   :stroke="i <= review.rating ? '#ff9500' : '#d2d2d7'" stroke-width="1.2">
                <path d="M8 1.5l1.85 4.17L14 6.34l-3 2.92.71 4.13L8 11.4l-3.71 2 .71-4.13-3-2.92 4.15-.67z"/>
              </svg>
              <span class="rating-text">{{ getRatingText(review.rating) }}</span>
            </div>
          </div>
          <p class="review-content">{{ review.review }}</p>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-if="filteredReviews.length === 0" class="empty-state">
      <div class="empty-icon-wrap">
        <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
          <path d="M36 16c0-6.6-5.4-12-12-12S12 9.4 12 16c0 2.2.6 4.3 1.6 6L24 40l10.4-18c1-1.7 1.6-3.8 1.6-6z"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="24" cy="16" r="4" stroke-width="2"/>
        </svg>
      </div>
      <p class="empty-title">暂无评价</p>
      <p class="empty-desc">当前筛选条件下没有评价</p>
    </div>
  </div>
</template>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.community {
  width: 100%;
  padding: 8px;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
}

.tab-header { display: flex; align-items: baseline; gap: 14px; margin-bottom: 20px; }
.header-title { font-size: 26px; font-weight: 700; letter-spacing: -0.03em; color: #1d1d1f; }
.review-count { font-size: 14px; color: #86868b; }

/* Stats bar */
.stats-bar {
  display: flex; align-items: center;
  border-radius: 12px; padding: 14px 0;
  background: rgba(0,0,0,0.04);
  box-shadow: 0 2px 4px rgba(0,0,0,0.07) inset, 0 1px 0 rgba(255,255,255,0.88);
  border: 0.5px solid rgba(0,0,0,0.06);
  margin-bottom: 20px;
}
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 3px; }
.stat-n { font-size: 22px; font-weight: 700; letter-spacing: -0.04em; color: #1d1d1f; line-height: 1; }
.stat-l { font-size: 11px; color: #86868b; }
.stat-sep { width: 0.5px; height: 30px; background: rgba(0,0,0,0.08); }

/* Filter bar */
.filter-bar { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
.filter-select-wrap { position: relative; flex: 1; min-width: 140px; }
.filter-select {
  width: 100%; height: 40px;
  padding: 0 32px 0 14px;
  border-radius: 10px;
  border: 0.5px solid rgba(0,0,0,0.1);
  background: rgba(255,255,255,0.6);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  font-size: 13px; font-weight: 500;
  font-family: inherit; color: #1d1d1f;
  appearance: none; -webkit-appearance: none;
  cursor: pointer; outline: none;
  transition: all 0.15s ease;
}
.filter-select:focus {
  border-color: rgba(0,113,227,0.4);
  box-shadow: 0 0 0 3px rgba(0,113,227,0.12);
}
.select-arrow {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  color: #86868b; pointer-events: none;
}

/* Review list */
.review-list { display: flex; flex-direction: column; gap: 12px; }
.review-card {
  display: flex; gap: 14px;
  border-radius: 16px; padding: 18px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.10), 0 0.5px 0 rgba(255,255,255,0.95) inset;
  transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.22s ease;
}
.review-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 24px rgba(0,0,0,0.12), 0 0.5px 0 rgba(255,255,255,0.95) inset;
}

.review-left { flex-shrink: 0; }
.avatar {
  width: 42px; height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d1d1f 0%, #3a3a3c 100%);
  color: #fff; font-size: 16px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  letter-spacing: -0.02em;
}
.avatar-anonymous {
  background: linear-gradient(135deg, #8e8e93 0%, #aeaeb2 100%);
}

.review-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 8px; }
.review-header { display: flex; justify-content: space-between; align-items: center; }
.review-user { font-size: 15px; font-weight: 600; color: #1d1d1f; letter-spacing: -0.01em; }
.review-date { font-size: 12px; color: #aeaeb2; }
.review-meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.course-tag {
  display: inline-flex; align-items: center;
  padding: 2px 10px; border-radius: 20px;
  background: rgba(0,0,0,0.06);
  font-size: 12px; font-weight: 500; color: #6e6e73;
  white-space: nowrap;
}
.stars { display: inline-flex; align-items: center; gap: 2px; }
.rating-text { font-size: 11px; color: #aeaeb2; margin-left: 4px; font-weight: 500; }
.review-content { font-size: 14px; color: #1d1d1f; line-height: 1.65; letter-spacing: -0.01em; }

.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 60px 20px; text-align: center;
}
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
.empty-desc { font-size: 14px; color: #86868b; }

@media (max-width: 768px) {
  .header-title { font-size: 22px; }
  .filter-bar { flex-direction: column; }
  .review-card { padding: 14px; gap: 12px; }
  .avatar { width: 36px; height: 36px; font-size: 14px; }
  .review-content { font-size: 13px; }
}
</style>
