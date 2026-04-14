<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { get } from "@/net"
import { ElMessage } from "element-plus"
import { useStore } from "@/stores"
import axios from 'axios'
import AIChat from "@/components/AIChat.vue"
import * as echarts from 'echarts'

const store = useStore()
const router = useRouter()
const profile = ref({})
const loading = ref(false)
const myCourses = ref([])
const logs = ref([])

const initProfile = () => ({
  id: '', realName: '', phone: '', gender: '',
  birthday: '', bio: '', avatar: '/avatars/default.png',
  lastLoginTime: '', created_at: '', updated_at: ''
})

const processProfileData = (data) => {
  if (!data) return initProfile()
  return {
    id: data.id || '',
    realName: data.real_name || data.realName || '',
    phone: data.phone || '',
    gender: data.gender || '',
    birthday: data.birthday || '',
    bio: data.bio || '',
    avatar: data.avatar || '/avatars/default.png',
    lastLoginTime: data.lastLoginTime || '',
    created_at: data.createdAt || data.created_at || '',
    updated_at: data.updatedAt || data.updated_at || '',
    userId: data.userId || ''
  }
}

const loadProfile = () => {
  get('/api/profile', data => {
    profile.value = processProfileData(data)
    if (profile.value.gender) profile.value.gender = profile.value.gender.toUpperCase()
  }, () => { profile.value = initProfile() })
}

const loadMyCourses = () => {
  get('/api/course/my-courses', data => {
    myCourses.value = (data && Array.isArray(data)) ? data : []
  }, () => { myCourses.value = [] })
}

const loadLogs = () => {
  get('/api/activity/logs?limit=5', data => {
    logs.value = (data && Array.isArray(data)) ? data : []
  }, () => { logs.value = [] })
}

const updateProfile = () => {
  if (profile.value.phone && !/^1[3-9]\d{9}$/.test(profile.value.phone)) {
    ElMessage.error('请输入正确的手机号码'); return
  }
  const submitData = {
    realName: profile.value.realName || '',
    real_name: profile.value.realName || '',
    phone: profile.value.phone || '',
    gender: profile.value.gender ? profile.value.gender.toLowerCase() : '',
    birthday: profile.value.birthday || '',
    bio: profile.value.bio || ''
  }
  loading.value = true
  axios.post('/api/profile', submitData, {
    headers: { 'Content-Type': 'application/json' }, withCredentials: true
  }).then(({ data }) => {
    if (data.success) {
      ElMessage.success(data.message); loadProfile()
      loadLogs()
    } else { ElMessage.error(data.message || '更新失败') }
    loading.value = false
  }).catch(() => { ElMessage.error('网络错误，请稍后重试'); loading.value = false })
}

const resetForm = () => { loadProfile() }

const formatDate = (s) => {
  if (!s) return '--'
  try { return new Date(s).toLocaleDateString('zh-CN') } catch { return s }
}
const formatRelTime = (s) => {
  if (!s) return '--'
  try {
    const m = Math.floor((Date.now() - new Date(s).getTime()) / 60000)
    if (m < 1) return '刚刚'
    if (m < 60) return `${m} 分钟前`
    const h = Math.floor(m / 60)
    if (h < 24) return `${h} 小时前`
    return `${Math.floor(h / 24)} 天前`
  } catch { return s }
}

const myCoursesCount = computed(() => myCourses.value.length)

// ─── Dashboard stats ───
const studyStats = ref(null)
const progressChartRef = ref(null)
const trendChartRef = ref(null)
let progressChart = null
let trendChart = null

const loadStudyStats = () => {
  get('/api/course/stats', data => {
    studyStats.value = data || null
    if (data) {
      nextTick(() => {
        renderProgressChart(data.courseProgressList || [])
        renderTrendChart(data.checkInTrend || [])
      })
    }
  }, () => { studyStats.value = null })
}

const renderProgressChart = (list) => {
  if (!progressChartRef.value) return
  if (progressChart) progressChart.dispose()
  progressChart = echarts.init(progressChartRef.value)
  const names = list.map(i => {
    const n = i.name || ''
    return n.length > 8 ? n.slice(0, 8) + '…' : n
  }).reverse()
  const values = list.map(i => i.progress || 0).reverse()
  const needZoom = list.length > 5
  progressChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}: {c}%' },
    grid: { top: 10, bottom: needZoom ? 28 : 10, left: 8, right: 20, containLabel: true },
    xAxis: {
      type: 'value', max: 100,
      axisLabel: { fontSize: 11, color: '#86868b', formatter: '{value}%' },
      splitLine: { lineStyle: { color: 'rgba(0,0,0,0.05)' } }
    },
    yAxis: {
      type: 'category', data: names,
      axisLabel: { fontSize: 11, color: '#6e6e73' },
      axisLine: { lineStyle: { color: 'rgba(0,0,0,0.08)' } },
      axisTick: { show: false }
    },
    dataZoom: needZoom ? [{
      type: 'slider', yAxisIndex: 0, zoomLock: false,
      startValue: 0, endValue: 4,
      width: 16, right: 0,
      borderColor: 'transparent', backgroundColor: 'rgba(0,0,0,0.04)',
      fillerColor: 'rgba(0,0,0,0.1)',
      handleStyle: { color: '#86868b', borderColor: '#86868b' },
      textStyle: { color: '#86868b', fontSize: 10 },
      moveHandleSize: 4
    }] : undefined,
    series: [{
      type: 'bar', data: values, barMaxWidth: 20, barMinHeight: 4,
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#6e6e73' }, { offset: 1, color: '#1d1d1f' }
        ])
      },
      label: {
        show: true, position: 'right', fontSize: 11, color: '#86868b',
        formatter: '{c}%'
      }
    }]
  })
}

const renderTrendChart = (trend) => {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)
  // 填充最近7天（无数据的天补0）
  const days = []
  const counts = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    days.push(`${d.getMonth() + 1}/${d.getDate()}`)
    const found = trend.find(t => t.date === key)
    counts.push(found ? Number(found.count) : 0)
  }
  trendChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>签到 <b>{c}</b> 次' },
    grid: { top: 20, bottom: 36, left: 12, right: 16, containLabel: true },
    xAxis: {
      type: 'category', data: days, boundaryGap: false,
      axisLabel: { fontSize: 11, color: '#86868b' },
      axisLine: { lineStyle: { color: 'rgba(0,0,0,0.08)' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value', minInterval: 1,
      axisLabel: { fontSize: 11, color: '#86868b' },
      splitLine: { lineStyle: { color: 'rgba(0,0,0,0.05)' } }
    },
    series: [{
      type: 'line', data: counts, smooth: true, symbol: 'circle', symbolSize: 7,
      lineStyle: { width: 2.5, color: '#0071e3' },
      itemStyle: { color: '#0071e3', borderWidth: 2, borderColor: '#fff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0,113,227,0.25)' }, { offset: 1, color: 'rgba(0,113,227,0.02)' }
        ])
      }
    }]
  })
}

const handleResize = () => {
  progressChart && progressChart.resize()
  trendChart && trendChart.resize()
}

const getActivityClass = (action) => {
  if (!action) return ''
  if (action.includes('登录')) return 'dot-login'
  if (action.includes('报名') || action.includes('收藏')) return 'dot-enroll'
  if (action.includes('签到')) return 'dot-checkin'
  if (action.includes('评价') || action.includes('讨论')) return 'dot-review'
  if (action.includes('更新')) return 'dot-profile'
  if (action.includes('退出')) return 'dot-exit'
  return ''
}

const totalCheckInCount = computed(() => {
  let total = 0
  myCourses.value.forEach(course => {
    const fields = ['sign_in_count','check_in_count','sign_count','attendance_count','signInCount','checkInCount']
    for (const f of fields) {
      if (course[f] != null) { total += parseInt(course[f]) || 0; break }
    }
  })
  return total
})

const scrollBlur = ref(0)
const scrollOverlay = ref(0)

const handleScroll = () => {
  const progress = Math.min(window.scrollY / 380, 1)
  scrollBlur.value = progress * 48
  scrollOverlay.value = progress * 0.52
}

onMounted(() => {
  loadProfile(); loadMyCourses(); loadLogs(); loadStudyStats()
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', handleResize)
  progressChart && progressChart.dispose()
  trendChart && trendChart.dispose()
})
</script>

<template>
  <div class="page">

    <!-- Background layers -->
    <div class="bg"></div>
    <div class="bg-dim" :style="{ backdropFilter: `blur(${scrollBlur}px) saturate(140%)`, WebkitBackdropFilter: `blur(${scrollBlur}px) saturate(140%)`, background: `rgba(240,246,252,${scrollOverlay})` }"></div>

    <!-- Navbar -->
    <header class="navbar">
      <button class="nav-btn" @click="router.push('/index')">
        <svg width="9" height="16" viewBox="0 0 9 16" fill="none">
          <path d="M8 1L1 8L8 15" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回
      </button>
      <span class="navbar-title">个人中心</span>
      <button class="nav-btn" @click="router.push('/courses')">课程中心</button>
    </header>

    <main class="main">

      <!-- Identity -->
      <section class="identity">
        <div class="avatar">
          <img :src="profile.avatar" class="avatar-img" onerror="this.style.display='none'" />
          <span class="avatar-letter">{{ profile.realName ? profile.realName.charAt(0) : 'U' }}</span>
        </div>
        <h1 class="name">{{ profile.realName || '未设置姓名' }}</h1>
        <p class="bio">{{ profile.bio || '暂无个人简介' }}</p>
        <div class="meta" v-if="profile.id || profile.phone">
          <span v-if="profile.id">ID {{ profile.id }}</span>
          <span class="dot" v-if="profile.id && profile.phone">·</span>
          <span v-if="profile.phone">{{ profile.phone }}</span>
        </div>
      </section>

      <!-- Stats -->
      <div class="glass-card stats-row">
        <div class="stat">
          <span class="stat-n">{{ studyStats?.enrolledCount ?? myCoursesCount }}</span>
          <span class="stat-l">在学课程</span>
        </div>
        <div class="vline"></div>
        <div class="stat">
          <span class="stat-n">{{ studyStats?.totalCheckIns ?? totalCheckInCount }}</span>
          <span class="stat-l">签到次数</span>
        </div>
        <template v-if="studyStats">
          <div class="vline"></div>
          <div class="stat">
            <span class="stat-n">{{ studyStats.avgProgress }}<small class="stat-u">%</small></span>
            <span class="stat-l">平均进度</span>
          </div>
          <div class="vline"></div>
          <div class="stat">
            <span class="stat-n">{{ studyStats.favoriteCount }}</span>
            <span class="stat-l">收藏课程</span>
          </div>
        </template>
      </div>

      <!-- Dashboard -->
      <section class="glass-card dashboard" v-if="studyStats">
        <h2 class="sec-title">学习数据</h2>

        <!-- Summary cards -->
        <div class="dash-summary">
          <div class="dash-card">
            <span class="dash-n">{{ studyStats.completedCount }}</span>
            <span class="dash-l">已完成</span>
          </div>
          <div class="dash-card">
            <span class="dash-n">{{ studyStats.ratingCount }}</span>
            <span class="dash-l">已评分</span>
          </div>
          <div class="dash-card">
            <span class="dash-n">{{ studyStats.avgRating }}</span>
            <span class="dash-l">平均评分</span>
          </div>
        </div>

        <!-- Charts -->
        <div class="charts-grid">
          <div class="chart-wrap">
            <p class="chart-label">课程进度</p>
            <div ref="progressChartRef" class="chart-box"></div>
          </div>
          <div class="chart-wrap">
            <p class="chart-label">签到趋势（近7天）</p>
            <div ref="trendChartRef" class="chart-box"></div>
          </div>
        </div>
      </section>

      <!-- Edit profile -->
      <section class="glass-card">
        <h2 class="sec-title">编辑资料</h2>

        <div class="field">
          <label class="flbl">真实姓名</label>
          <input class="fin" v-model="profile.realName" placeholder="请输入真实姓名" maxlength="20" />
        </div>
        <div class="field">
          <label class="flbl">手机号码</label>
          <input class="fin" v-model="profile.phone" placeholder="请输入手机号码" maxlength="11" inputmode="numeric" />
        </div>
        <div class="field">
          <label class="flbl">性别</label>
          <div class="seg">
            <label class="seg-opt" :class="{ on: profile.gender === 'MALE' }"><input type="radio" v-model="profile.gender" value="MALE" hidden />男</label>
            <label class="seg-opt" :class="{ on: profile.gender === 'FEMALE' }"><input type="radio" v-model="profile.gender" value="FEMALE" hidden />女</label>
            <label class="seg-opt" :class="{ on: profile.gender === 'OTHER' }"><input type="radio" v-model="profile.gender" value="OTHER" hidden />其他</label>
          </div>
        </div>
        <div class="field">
          <label class="flbl">生日</label>
          <input class="fin" v-model="profile.birthday" type="date" />
        </div>
        <div class="field">
          <label class="flbl">个人简介</label>
          <textarea class="fta" v-model="profile.bio" placeholder="请输入个人简介" maxlength="200" rows="3"></textarea>
          <span class="wc">{{ (profile.bio || '').length }}/200</span>
        </div>

        <div class="form-footer">
          <button class="btn-text" @click="resetForm">重置</button>
          <button class="btn-primary" @click="updateProfile" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            {{ loading ? '保存中…' : '保存修改' }}
          </button>
        </div>
      </section>

      <!-- Account info -->
      <section class="glass-card">
        <h2 class="sec-title">账户信息</h2>
        <ul class="rows">
          <li class="row"><span class="rk">用户名</span><span class="rv">{{ store.auth.user?.username || '--' }}</span></li>
          <li class="row"><span class="rk">邮箱</span><span class="rv">{{ store.auth.user?.email || '--' }}</span></li>
          <li class="row last"><span class="rk">上次登录</span><span class="rv">{{ formatDate(profile.lastLoginTime) }}</span></li>
        </ul>
      </section>

      <!-- Activity -->
      <section class="glass-card">
        <h2 class="sec-title">最近动态</h2>
        <ul class="rows" v-if="logs.length">
          <li class="row activity-row" v-for="(log, i) in logs" :key="log.id" :class="{ last: i === logs.length - 1 }">
            <span class="activity-dot" :class="getActivityClass(log.action)"></span>
            <div class="activity-info">
              <span class="activity-action">{{ log.action }}</span>
              <span class="activity-desc" v-if="log.description">{{ log.description }}</span>
            </div>
            <span class="rv">{{ formatRelTime(log.createdAt) }}</span>
          </li>
        </ul>
        <p class="empty" v-else>暂无动态</p>
      </section>

      <!-- AI Chat -->
      <section class="glass-card ai-chat-section">
        <h2 class="sec-title">AI 智能助手</h2>
        <AIChat />
      </section>

    </main>
  </div>
</template>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

/* ─── Page shell ─── */
.page {
  min-height: 100vh;
  position: relative;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  color: #1d1d1f;
  -webkit-font-smoothing: antialiased;
}

/* ─── Background image ─── */
.bg {
  position: fixed; inset: 0; z-index: 0;
  background-image: url('@/assets/images/3.jpeg');
  background-size: cover;
  background-position: center 40%;
  background-repeat: no-repeat;
}

/* 滚动驱动的动态模糊遮罩 — blur 和透明度由 JS 控制 */
.bg-dim {
  position: fixed; inset: 0; z-index: 1;
  /* 初始无模糊无遮罩，由 :style 绑定动态值 */
  transition: backdrop-filter 0.1s linear, -webkit-backdrop-filter 0.1s linear, background 0.1s linear;
}

/* ─── Navbar — frosted glass ─── */
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
  color: #0071e3; padding: 0;
  transition: opacity 0.1s;
}
.nav-btn:hover { opacity: 0.7; }
.navbar-title { font-size: 16px; font-weight: 600; letter-spacing: -0.02em; }

/* ─── Main ─── */
.main {
  position: relative; z-index: 2;
  max-width: 720px;
  margin: 0 auto;
  padding: 28px 18px 72px;
  display: flex; flex-direction: column; gap: 14px;
}

/* ─── Glass card ─── */
.glass-card {
  border-radius: 20px;
  padding: 22px 22px;
  background: rgba(255,255,255,0.62);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow:
      0 2px 32px rgba(0,0,0,0.08),
      0 0.5px 0 rgba(255,255,255,0.95) inset;
}

/* ─── Identity ─── */
.identity {
  display: flex; flex-direction: column; align-items: center;
  text-align: center; padding: 44px 20px 32px;
}
.avatar {
  position: relative;
  width: 90px; height: 90px; border-radius: 50%;
  background: rgba(255,255,255,0.55);
  backdrop-filter: blur(16px);
  display: flex; align-items: center; justify-content: center;
  overflow: hidden; margin-bottom: 18px;
  border: 1.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 6px 28px rgba(0,0,0,0.13);
}
.avatar-img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
.avatar-letter { font-size: 36px; font-weight: 300; color: #48484a; letter-spacing: -0.02em; }

.name {
  font-size: 26px; font-weight: 600; letter-spacing: -0.03em;
  color: #1d1d1f;
  text-shadow: 0 1px 8px rgba(255,255,255,0.7);
  margin-bottom: 8px;
}
.bio {
  font-size: 15px; color: #3a3a3c; line-height: 1.55;
  max-width: 380px; margin-bottom: 10px;
  text-shadow: 0 1px 4px rgba(255,255,255,0.5);
}
.meta { display: flex; align-items: center; gap: 7px; font-size: 13px; color: #6e6e73; }
.dot { color: #c7c7cc; }

/* ─── Stats ─── */
.stats-row { display: flex; align-items: center; padding: 18px 0; }
.stat { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stat-n { font-size: 28px; font-weight: 600; letter-spacing: -0.04em; line-height: 1; }
.stat-l { font-size: 12px; color: #6e6e73; }
.vline { width: 0.5px; height: 32px; background: rgba(0,0,0,0.1); }

/* ─── Section title ─── */
.sec-title {
  font-size: 11px; font-weight: 600;
  letter-spacing: 0.07em; text-transform: uppercase;
  color: #86868b; margin-bottom: 16px;
}

/* ─── Form ─── */
.field { margin-bottom: 14px; position: relative; }
.flbl { display: block; font-size: 12px; color: #6e6e73; margin-bottom: 6px; }

.fin {
  width: 100%; height: 44px;
  background: rgba(255,255,255,0.48);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255,255,255,0.80);
  border-radius: 10px; padding: 0 13px;
  font-size: 15px; font-family: inherit; color: #1d1d1f;
  outline: none; transition: box-shadow 0.15s, background 0.15s;
  appearance: none; -webkit-appearance: none;
}
.fin:focus {
  background: rgba(255,255,255,0.7);
  box-shadow: 0 0 0 3px rgba(0,113,227,0.22);
}
.fin[type="date"] { color-scheme: light; }

.fta {
  width: 100%;
  background: rgba(255,255,255,0.48);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255,255,255,0.80);
  border-radius: 10px; padding: 11px 13px;
  font-size: 15px; font-family: inherit; color: #1d1d1f;
  outline: none; resize: vertical; min-height: 84px;
  line-height: 1.55; transition: box-shadow 0.15s, background 0.15s;
}
.fta:focus {
  background: rgba(255,255,255,0.7);
  box-shadow: 0 0 0 3px rgba(0,113,227,0.22);
}
.wc { display: block; text-align: right; font-size: 11px; color: #aeaeb2; margin-top: 4px; }

/* Segmented Control */
.seg {
  display: flex;
  background: rgba(255,255,255,0.40);
  backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255,255,255,0.72);
  border-radius: 10px; padding: 3px; gap: 2px;
}
.seg-opt {
  flex: 1; text-align: center; padding: 8px 0;
  font-size: 14px; color: #3a3a3c; border-radius: 7px;
  cursor: pointer; transition: all 0.12s;
  user-select: none; font-family: inherit;
}
.seg-opt.on {
  background: rgba(255,255,255,0.92);
  color: #1d1d1f; font-weight: 500;
  box-shadow: 0 1px 5px rgba(0,0,0,0.10);
}

/* ─── Form footer ─── */
.form-footer {
  display: flex; justify-content: flex-end;
  align-items: center; gap: 20px; margin-top: 8px;
}
.btn-text {
  background: none; border: none; cursor: pointer;
  font-size: 15px; font-family: inherit;
  color: #0071e3; padding: 0; transition: opacity 0.1s;
}
.btn-text:hover { opacity: 0.7; }

.btn-primary {
  display: inline-flex; align-items: center; gap: 7px;
  background: rgba(0,113,227,0.88);
  backdrop-filter: blur(8px);
  color: #fff; border: none;
  border-radius: 980px; padding: 10px 22px;
  font-size: 15px; font-family: inherit; font-weight: 500;
  letter-spacing: -0.01em; cursor: pointer;
  box-shadow: 0 2px 14px rgba(0,113,227,0.28);
  transition: background 0.12s, transform 0.1s;
}
.btn-primary:hover:not(:disabled) { background: rgba(0,119,237,0.95); }
.btn-primary:active:not(:disabled) { transform: scale(0.97); }
.btn-primary:disabled { opacity: 0.42; cursor: not-allowed; }

.spinner {
  width: 13px; height: 13px; flex-shrink: 0;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: #fff; border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ─── Rows (account + activity) ─── */
.rows { list-style: none; }
.row {
  display: flex; align-items: center; gap: 10px;
  padding: 13px 0; border-bottom: 0.5px solid rgba(0,0,0,0.07);
}
.row.last { border-bottom: none; }
.rk { font-size: 15px; color: #1d1d1f; }
.rv { font-size: 15px; color: #6e6e73; margin-left: auto; }

/* pulse dot for activity */
.activity-row {
  align-items: flex-start;
  gap: 12px;
}
.activity-dot {
  flex-shrink: 0; width: 8px; height: 8px;
  border-radius: 50%; background: #0071e3; margin-top: 5px;
}
.activity-dot.dot-login { background: #34c759; }
.activity-dot.dot-enroll { background: #ff9500; }
.activity-dot.dot-checkin { background: #0071e3; }
.activity-dot.dot-review { background: #af52de; }
.activity-dot.dot-profile { background: #5ac8fa; }
.activity-dot.dot-exit { background: #ff3b30; }
.activity-info {
  flex: 1; display: flex; flex-direction: column; gap: 2px;
}
.activity-action {
  font-size: 15px; color: #1d1d1f; font-weight: 500;
}
.activity-desc {
  font-size: 13px; color: #86868b; line-height: 1.4;
}

.empty { font-size: 15px; color: #86868b; text-align: center; padding: 14px 0; }

/* ─── AI Chat Section ─── */
.ai-chat-section {
  padding: 20px;
}

.ai-chat-section :deep(.card-container) {
  width: 100%;
  max-width: 100%;
  margin: 0;
  box-shadow: none;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

/* ─── Responsive ─── */
@media (max-width: 430px) {
  .main { padding: 16px 14px 52px; }
  .identity { padding: 28px 12px 22px; }
  .name { font-size: 22px; }
  .stat-n { font-size: 28px; }
  .navbar { padding: 0 16px; }
  .glass-card { padding: 18px 16px; }
  .ai-chat-section :deep(.card-container) {
    width: 100%;
    min-height: 400px;
  }
  .charts-grid { grid-template-columns: 1fr; }
}

/* ─── Dashboard ─── */
.dashboard { display: flex; flex-direction: column; gap: 18px; }
.stat-u { font-size: 14px; font-weight: 400; color: #86868b; }

.dash-summary { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.dash-card {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 14px 0; border-radius: 14px;
  background: rgba(0,0,0,0.03); border: 0.5px solid rgba(0,0,0,0.05);
  box-shadow: 0 1px 0 rgba(255,255,255,0.88) inset;
}
.dash-n { font-size: 26px; font-weight: 700; letter-spacing: -0.04em; color: #1d1d1f; line-height: 1; }
.dash-l { font-size: 11px; color: #86868b; font-weight: 500; }

.charts-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.chart-wrap {
  background: rgba(0,0,0,0.025); border-radius: 16px; padding: 14px 14px 8px;
  border: 0.5px solid rgba(0,0,0,0.05);
  box-shadow: 0 1px 0 rgba(255,255,255,0.85) inset;
}
.chart-label { font-size: 11px; font-weight: 600; letter-spacing: 0.06em; text-transform: uppercase; color: #86868b; margin-bottom: 8px; }
.chart-box { width: 100%; height: 200px; }
</style>