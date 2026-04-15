<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { get, post } from '@/net'
import ModuleTab from '@/components/ModuleTab.vue'
import ConfirmDeleteModal from '@/components/common/ConfirmDeleteModal.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeTab = ref('calendar')
const plans = ref([])
const calendarItems = ref([])
const enrolledCourses = ref([])

// ============ 周视图状态 ============
const today = new Date()
const getMonday = (d) => {
  const date = new Date(d)
  const day = date.getDay()
  const diff = date.getDate() - day + (day === 0 ? -6 : 1)
  date.setDate(diff)
  date.setHours(0, 0, 0, 0)
  return date
}
const currentWeekStart = ref(getMonday(today))
const selectedDay = ref(fmtDate(today))

// 对话框状态
const showPlanDialog = ref(false)
const showItemDialog = ref(false)
const showDayDialog = ref(false)
const showDeleteModal = ref(false)
const editingPlan = ref(null)
const editingItem = ref(null)
const selectedDate = ref(null)
const selectedDateItems = ref([])
const deleteTarget = ref(null)
const deleteMessage = ref('')

// 计划表单
const planForm = ref({ title: '', description: '', startDate: '', endDate: '' })
// 任务表单
const itemForm = ref({
  planId: null,
  courseId: null,
  customTopic: '',
  planDate: '',
  repeatMode: 'once',       // 'once' | 'weekly'
  selectedWeekdays: [],     // [1,2,3,4,5,6,7] 对应周一到周日
  startTime: '',
  endTime: '',
  notes: ''
})

const itemType = ref('course')

// 星期几选项
const weekdayOptions = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' },
  { value: 6, label: '周六' },
  { value: 7, label: '周日' }
]

// 分页
const planPage = ref(1)
const planTotal = ref(0)
const planPageSize = ref(10)

// 背景模糊
const bgRef = ref(null)
const scrollOverlay = ref(0)
const handleScroll = () => {
  const progress = Math.min(window.scrollY / 380, 1)
  if (bgRef.value) {
    bgRef.value.style.filter = `blur(${progress * 48}px) saturate(140%)`
  }
  scrollOverlay.value = progress * 0.52
}

// 格式化日期为 YYYY-MM-DD
function fmtDate(d) {
  if (!d) return ''
  if (typeof d === 'string') return d.substring(0, 10)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

// 按日期索引的日历项
const itemsByDate = computed(() => {
  const map = {}
  for (const item of calendarItems.value) {
    const key = fmtDate(item.planDate)
    if (!map[key]) map[key] = []
    map[key].push(item)
  }
  return map
})

// ============ 周视图计算 ============
const dayNames = ['一', '二', '三', '四', '五', '六', '日']

const weekDays = computed(() => {
  const days = []
  const todayStr = fmtDate(new Date())
  for (let i = 0; i < 7; i++) {
    const date = new Date(currentWeekStart.value)
    date.setDate(date.getDate() + i)
    const key = fmtDate(date)
    days.push({
      date,
      key,
      dayNumber: date.getDate(),
      dayName: dayNames[i],
      isToday: key === todayStr,
      items: itemsByDate.value[key] || []
    })
  }
  return days
})

const weekTitle = computed(() => {
  const start = currentWeekStart.value
  const end = new Date(start)
  end.setDate(end.getDate() + 6)
  const sm = start.getMonth() + 1, sd = start.getDate()
  const em = end.getMonth() + 1, ed = end.getDate()
  return sm === em ? `${start.getFullYear()}年${sm}月${sd}日 - ${ed}日` : `${sm}月${sd}日 - ${em}月${ed}日`
})

// 选中日期的任务
const selectedDayItems = computed(() => {
  return itemsByDate.value[selectedDay.value] || []
})

// 本周统计
const weekStats = computed(() => {
  let total = 0, completed = 0
  for (const day of weekDays.value) {
    total += day.items.length
    completed += day.items.filter(i => i.completed).length
  }
  return { total, completed }
})

// ============ 数据加载 ============

const loadCalendarItems = () => {
  const start = fmtDate(currentWeekStart.value)
  const endObj = new Date(currentWeekStart.value)
  endObj.setDate(endObj.getDate() + 6)
  const end = fmtDate(endObj)
  get(`/api/study-plan/calendar?startDate=${start}&endDate=${end}`, data => {
    calendarItems.value = data || []
  }, () => { calendarItems.value = [] })
}

const loadPlans = () => {
  get(`/api/study-plan/list?page=${planPage.value}&pageSize=${planPageSize.value}`, data => {
    plans.value = (data?.list || []).map(p => ({ ...p, _expanded: false, items: [] }))
    planTotal.value = data?.total || 0
  }, () => { plans.value = [] })
}

const loadEnrolledCourses = () => {
  get('/api/course/my-courses?page=1&pageSize=100', data => {
    enrolledCourses.value = (data?.list || []).map(r => r.course || r)
  }, () => { enrolledCourses.value = [] })
}

// ============ 周导航 ============

const prevWeek = () => {
  const d = new Date(currentWeekStart.value)
  d.setDate(d.getDate() - 7)
  currentWeekStart.value = d
}

const nextWeek = () => {
  const d = new Date(currentWeekStart.value)
  d.setDate(d.getDate() + 7)
  currentWeekStart.value = d
}

watch(currentWeekStart, loadCalendarItems)

// ============ 日期点击 ============

const selectDay = (key) => {
  selectedDay.value = key
}

const onDateClick = (day) => {
  const key = fmtDate(day)
  selectedDate.value = key
  selectedDateItems.value = itemsByDate.value[key] || []
  showDayDialog.value = true
}

// ============ 计划 CRUD ============

const openCreatePlan = () => {
  editingPlan.value = null
  planForm.value = { title: '', description: '', startDate: '', endDate: '' }
  showPlanDialog.value = true
}

const openEditPlan = (plan) => {
  editingPlan.value = plan
  planForm.value = {
    title: plan.title,
    description: plan.description || '',
    startDate: fmtDate(plan.startDate),
    endDate: fmtDate(plan.endDate)
  }
  showPlanDialog.value = true
}

const submitPlan = () => {
  const f = planForm.value
  if (!f.title || !f.startDate || !f.endDate) {
    ElMessage.warning('请填写标题和日期范围')
    return
  }
  if (editingPlan.value) {
    post(`/api/study-plan/update?id=${editingPlan.value.id}&title=${encodeURIComponent(f.title)}&startDate=${f.startDate}&endDate=${f.endDate}&description=${encodeURIComponent(f.description || '')}&status=${editingPlan.value.status || 0}`, {}, () => {
      ElMessage.success('更新成功')
      showPlanDialog.value = false
      loadPlans()
      loadCalendarItems()
    })
  } else {
    post(`/api/study-plan/create?title=${encodeURIComponent(f.title)}&startDate=${f.startDate}&endDate=${f.endDate}&description=${encodeURIComponent(f.description || '')}`, {}, () => {
      ElMessage.success('创建成功')
      showPlanDialog.value = false
      loadPlans()
      loadCalendarItems()
    })
  }
}

const deletePlan = (plan) => {
  deleteTarget.value = { type: 'plan', id: plan.id, title: plan.title }
  deleteMessage.value = `确定删除计划「${plan.title}」？所有任务将一并删除。`
  showDeleteModal.value = true
}

// ============ 任务项 CRUD ============

const openCreateItem = (planId, date) => {
  editingItem.value = null
  itemForm.value = {
    planId: planId,
    courseId: null,
    customTopic: '',
    planDate: date || '',
    repeatMode: 'once',           // 默认单次模式
    selectedWeekdays: [],         // 清空星期选择
    startTime: '',
    endTime: '',
    notes: ''
  }
  itemType.value = 'course'
  showItemDialog.value = true
}

const openEditItem = (item) => {
  editingItem.value = item
  itemForm.value = {
    planId: item.planId,
    courseId: item.courseId,
    customTopic: item.customTopic || '',
    planDate: fmtDate(item.planDate),
    startTime: item.startTime || '',
    endTime: item.endTime || '',
    notes: item.notes || ''
  }
  itemType.value = item.courseId ? 'course' : 'custom'
  showItemDialog.value = true
}

const submitItem = () => {
  const f = itemForm.value

  // 验证：根据重复模式进行不同的验证
  if (editingItem.value) {
    // 编辑模式：验证日期
    if (!f.planDate) {
      ElMessage.warning('请选择日期')
      return
    }
  } else {
    // 新增模式：根据重复模式验证
    if (f.repeatMode === 'once') {
      if (!f.planDate) {
        ElMessage.warning('请选择日期')
        return
      }
    } else if (f.repeatMode === 'weekly') {
      if (f.selectedWeekdays.length === 0) {
        ElMessage.warning('请选择至少一个星期')
        return
      }
    }
  }

  // 构建基础参数
  const courseIdParam = itemType.value === 'course' && f.courseId ? `&courseId=${f.courseId}` : ''
  const customTopicParam = itemType.value === 'custom' && f.customTopic ? `&customTopic=${encodeURIComponent(f.customTopic)}` : ''
  const startTimeParam = f.startTime ? `&startTime=${f.startTime}` : ''
  const endTimeParam = f.endTime ? `&endTime=${f.endTime}` : ''
  const notesParam = f.notes ? `&notes=${encodeURIComponent(f.notes)}` : ''

  if (editingItem.value) {
    // 编辑模式：保持原有逻辑
    post(`/api/study-plan/item/update?id=${editingItem.value.id}&planId=${f.planId}&planDate=${f.planDate}${courseIdParam}${customTopicParam}${startTimeParam}${endTimeParam}${notesParam}`, {}, () => {
      ElMessage.success('更新成功')
      showItemDialog.value = false
      loadPlans()
      loadCalendarItems()
    })
  } else {
    // 新增模式：根据重复模式调用不同接口
    if (f.repeatMode === 'once') {
      // 单次任务：使用原有接口
      post(`/api/study-plan/item/add?planId=${f.planId}&planDate=${f.planDate}${courseIdParam}${customTopicParam}${startTimeParam}${endTimeParam}${notesParam}`, {}, () => {
        ElMessage.success('添加成功')
        showItemDialog.value = false
        loadPlans()
        loadCalendarItems()
      })
    } else {
      // 每周重复：使用新的批量添加接口
      const weekdaysParam = f.selectedWeekdays.map(d => `weekdays=${d}`).join('&')
      const url = `/api/study-plan/item/add-batch?planId=${f.planId}${courseIdParam}${customTopicParam}${startTimeParam}${endTimeParam}${notesParam}&${weekdaysParam}`
      console.log('=== 批量添加任务 ===')
      console.log('请求URL:', url)
      console.log('表单数据:', f)
      console.log('星期参数:', weekdaysParam)
      post(url, {}, (data) => {
        console.log('响应数据:', data)
        console.log('响应类型:', typeof data)
        console.log('createdCount:', data?.createdCount)
        if (data && data.createdCount === 0) {
          ElMessage.warning('所选日期范围内没有符合条件的星期，请调整计划周期或选择其他星期')
        } else if (data && data.createdCount > 0) {
          ElMessage.success(`已添加${data.createdCount}个任务`)
          showItemDialog.value = false
          loadPlans()
          loadCalendarItems()
        } else {
          console.error('响应数据格式异常:', data)
          ElMessage.error('服务器返回数据格式异常')
        }
      }, (message) => {
        console.error('批量添加业务失败:', message)
      }, (error) => {
        console.error('批量添加请求失败:', error)
      })
    }
  }
}

const deleteItem = (item) => {
  deleteTarget.value = { type: 'item', id: item.id, planId: item.planId }
  deleteMessage.value = '确定删除此任务？'
  showDeleteModal.value = true
}

// 删除确认处理
const handleDeleteConfirm = () => {
  if (!deleteTarget.value) return

  if (deleteTarget.value.type === 'plan') {
    post(`/api/study-plan/delete?id=${deleteTarget.value.id}`, {}, () => {
      ElMessage.success('删除成功')
      loadPlans()
      loadCalendarItems()
    })
  } else if (deleteTarget.value.type === 'item') {
    post(`/api/study-plan/item/delete?id=${deleteTarget.value.id}&planId=${deleteTarget.value.planId}`, {}, () => {
      ElMessage.success('删除成功')
      loadPlans()
      loadCalendarItems()
    })
  }
}

const toggleComplete = (item) => {
  const newVal = !item.completed
  post(`/api/study-plan/item/toggle?id=${item.id}&completed=${newVal}`, {}, () => {
    item.completed = newVal
    loadPlans()
  })
}

// 状态标签
const statusText = (s) => ['进行中', '已完成', '已放弃'][s] || '未知'
const statusColor = (s) => ['#0071e3', '#34c759', '#8e8e93'][s] || '#8e8e93'

// 任务展示名
const itemName = (item) => item.courseName || item.customTopic || '学习任务'

// 切换星期几选择状态
const toggleWeekday = (dayValue) => {
  const index = itemForm.value.selectedWeekdays.indexOf(dayValue)
  if (index > -1) {
    itemForm.value.selectedWeekdays.splice(index, 1)
  } else {
    itemForm.value.selectedWeekdays.push(dayValue)
  }
}

// 计算选中的星期几文本（用于提示）
const selectedWeekdayText = computed(() => {
  if (itemForm.value.selectedWeekdays.length === 0) return ''
  const sortedDays = [...itemForm.value.selectedWeekdays].sort((a, b) => a - b)
  const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return sortedDays.map(d => dayNames[d]).join('、')
})

// 展开/折叠计划（懒加载 items）
const togglePlanExpand = (plan) => {
  plan._expanded = !plan._expanded
  if (plan._expanded && plan.items.length === 0) {
    get(`/api/study-plan/${plan.id}`, data => {
      if (data && data.items) {
        plan.items = data.items
      }
    })
  }
}

// 分页
const onPlanPageChange = (p) => {
  planPage.value = p
  loadPlans()
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  loadCalendarItems()
  loadPlans()
  loadEnrolledCourses()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="page">
    <div class="bg" ref="bgRef"></div>
    <div class="bg-dim" :style="{ background: `rgba(240,246,252,${scrollOverlay})` }"></div>

    <header class="navbar">
      <div class="navbar-center">
        <ModuleTab />
      </div>
    </header>

    <main class="main">
      <div class="glass-tabs">
        <div class="tab-bar">
          <button class="tab-btn" :class="{ active: activeTab === 'calendar' }" @click="activeTab = 'calendar'">
            日历视图
          </button>
          <button class="tab-btn" :class="{ active: activeTab === 'list' }" @click="activeTab = 'list'">
            计划列表
          </button>
        </div>
        <div class="tab-content">

          <!-- ==================== 日历视图 ==================== -->
          <div v-if="activeTab === 'calendar'" class="calendar-section">
            <div class="meeting-card">
              <!-- 头部 -->
              <div class="mc-header">
                <div class="mc-title">学习日程</div>
                <div class="mc-date-selector">
                  <button class="mc-arrow" @click="prevWeek">
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M9 3L4.5 7L9 11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </button>
                  <span>{{ weekTitle }}</span>
                  <button class="mc-arrow" @click="nextWeek">
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M5 3L9.5 7L5 11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </button>
                </div>
              </div>

              <!-- 统计 -->
              <div class="mc-calls-info">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
                <span>本周 {{ weekStats.total }} 项任务，已完成 {{ weekStats.completed }} 项</span>
              </div>

              <!-- 日期导航 + 指示器 -->
              <div class="mc-date-nav-and-indicators">
                <div class="mc-date-nav-container">
                  <div v-for="day in weekDays" :key="day.key"
                       class="mc-day-item"
                       :class="{ 'mc-day-active': selectedDay === day.key }"
                       @click="selectDay(day.key)">
                    <div class="mc-day-number">{{ day.dayNumber }}</div>
                    <div class="mc-day-name">{{ day.dayName }}</div>
                  </div>
                </div>
                <div class="mc-indicator-container">
                  <div v-for="day in weekDays" :key="'ind-' + day.key"
                       class="mc-indicator-dot"
                       :class="{ 'mc-indicator-active': day.items.length > 0 }">
                  </div>
                  <div class="mc-indicator-line"></div>
                </div>
              </div>
            </div>

            <!-- 选中日期的任务列表 -->
            <div class="selected-day-section" style="margin-top: 20px;">
              <div class="selected-day-header">
                <h3 class="sec-title">{{ selectedDay }} 任务</h3>
                <button class="btn-primary sm" @click="onDateClick(selectedDay)">查看详情</button>
              </div>
              <div v-if="selectedDayItems.length === 0" class="empty-state small">
                <p>这天还没有安排学习任务</p>
              </div>
              <div class="today-list" v-else>
                <div class="today-item" v-for="item in selectedDayItems" :key="item.id">
                  <label class="checkbox-wrap" @click.stop>
                    <input type="checkbox" :checked="item.completed" @change="toggleComplete(item)" />
                    <span class="checkmark"></span>
                  </label>
                  <div class="today-info" :class="{ done: item.completed }">
                    <span class="today-time" v-if="item.startTime">{{ item.startTime }}{{ item.endTime ? '-' + item.endTime : '' }}</span>
                    <span class="today-name">{{ itemName(item) }}</span>
                  </div>
                  <div class="item-ops" @click.stop>
                    <button class="icon-btn sm" @click="openEditItem(item)">
                      <svg width="12" height="12" viewBox="0 0 14 14" fill="none"><path d="M9.5 1.5l3 3L4 13H1v-3L9.5 1.5z" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    </button>
                    <button class="icon-btn sm danger" @click="deleteItem(item)">
                      <svg width="12" height="12" viewBox="0 0 14 14" fill="none"><path d="M2 4h10M5 4V2h4v2M3 4v8a1 1 0 001 1h6a1 1 0 001-1V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- ==================== 计划列表 ==================== -->
          <div v-if="activeTab === 'list'" class="list-section">
            <div class="list-header">
              <h3 class="sec-title">我的计划</h3>
              <button class="btn-primary" @click="openCreatePlan">新建计划</button>
            </div>

            <div v-if="plans.length === 0" class="empty-state">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c7c7cc" stroke-width="1.5">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              <p>还没有学习计划，点击上方按钮创建一个吧</p>
            </div>

            <div class="plan-card" v-for="plan in plans" :key="plan.id">
              <div class="plan-header" @click="togglePlanExpand(plan)">
                <div class="plan-info">
                  <h4 class="plan-title">{{ plan.title }}</h4>
                  <div class="plan-meta">
                    <span>{{ fmtDate(plan.startDate) }} ~ {{ fmtDate(plan.endDate) }}</span>
                    <span class="status-tag" :style="{ color: statusColor(plan.status), background: statusColor(plan.status) + '14' }">
                      {{ statusText(plan.status) }}
                    </span>
                  </div>
                </div>
                <div class="plan-actions" @click.stop>
                  <button class="icon-btn" title="添加任务" @click="openCreateItem(plan.id, fmtDate(new Date()))">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><line x1="8" y1="3" x2="8" y2="13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><line x1="3" y1="8" x2="13" y2="8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                  </button>
                  <button class="icon-btn" title="编辑" @click="openEditPlan(plan)">
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M9.5 1.5l3 3L4 13H1v-3L9.5 1.5z" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </button>
                  <button class="icon-btn danger" title="删除" @click="deletePlan(plan)">
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M2 4h10M5 4V2h4v2M3 4v8a1 1 0 001 1h6a1 1 0 001-1V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </button>
                  <svg class="expand-icon" :class="{ expanded: plan._expanded }" width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M4 5l3 3 3-3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </div>
              </div>
              <!-- 进度条 -->
              <div class="progress-bar-wrap" v-if="plan.itemCount > 0">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: (plan.itemCount ? (plan.completedCount / plan.itemCount * 100) : 0) + '%' }"></div>
                </div>
                <span class="progress-text">{{ plan.completedCount || 0 }}/{{ plan.itemCount || 0 }}</span>
              </div>
              <!-- 展开的任务列表 -->
              <div class="plan-items" v-if="plan._expanded && plan.items && plan.items.length > 0">
                <div class="plan-item-row" v-for="item in plan.items" :key="item.id">
                  <label class="checkbox-wrap" @click.stop>
                    <input type="checkbox" :checked="item.completed" @change="toggleComplete(item)" />
                    <span class="checkmark"></span>
                  </label>
                  <div class="item-info" :class="{ done: item.completed }">
                    <span class="item-date">{{ fmtDate(item.planDate) }}</span>
                    <span class="item-time" v-if="item.startTime">{{ item.startTime }}{{ item.endTime ? '-' + item.endTime : '' }}</span>
                    <span class="item-name">{{ item.courseName || item.customTopic || '学习任务' }}</span>
                  </div>
                  <div class="item-ops" @click.stop>
                    <button class="icon-btn sm" @click="openEditItem(item)">
                      <svg width="12" height="12" viewBox="0 0 14 14" fill="none"><path d="M9.5 1.5l3 3L4 13H1v-3L9.5 1.5z" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    </button>
                    <button class="icon-btn sm danger" @click="deleteItem(item)">
                      <svg width="12" height="12" viewBox="0 0 14 14" fill="none"><path d="M2 4h10M5 4V2h4v2M3 4v8a1 1 0 001 1h6a1 1 0 001-1V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination" v-if="planTotal > planPageSize">
              <button class="page-btn" :disabled="planPage <= 1" @click="onPlanPageChange(planPage - 1)">上一页</button>
              <span class="page-info">{{ planPage }} / {{ Math.ceil(planTotal / planPageSize) }}</span>
              <button class="page-btn" :disabled="planPage >= Math.ceil(planTotal / planPageSize)" @click="onPlanPageChange(planPage + 1)">下一页</button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- ==================== 对话框：创建/编辑计划 ==================== -->
    <el-dialog v-model="showPlanDialog" :title="editingPlan ? '编辑计划' : '新建计划'" width="420" :close-on-click-modal="false" class="plan-dialog">
      <div class="form-group">
        <label>计划标题 <span class="req">*</span></label>
        <input v-model="planForm.title" placeholder="如：本周学习计划" class="form-input" />
      </div>
      <div class="form-group">
        <label>计划描述</label>
        <textarea v-model="planForm.description" placeholder="可选" class="form-input" rows="2"></textarea>
      </div>
      <div class="form-row">
        <div class="form-group half">
          <label>开始日期 <span class="req">*</span></label>
          <input v-model="planForm.startDate" type="date" class="form-input" />
        </div>
        <div class="form-group half">
          <label>结束日期 <span class="req">*</span></label>
          <input v-model="planForm.endDate" type="date" class="form-input" />
        </div>
      </div>
      <template #footer>
        <button class="btn-cancel" @click="showPlanDialog = false">取消</button>
        <button class="btn-primary" @click="submitPlan">{{ editingPlan ? '保存' : '创建' }}</button>
      </template>
    </el-dialog>

    <!-- ==================== 对话框：添加/编辑任务 ==================== -->
    <el-dialog v-model="showItemDialog" :title="editingItem ? '编辑任务' : '添加任务'" width="420" :close-on-click-modal="false" class="plan-dialog">
      <div class="form-group">
        <label>任务类型</label>
        <div class="type-switch">
          <button :class="{ active: itemType === 'course' }" @click="itemType = 'course'">课程学习</button>
          <button :class="{ active: itemType === 'custom' }" @click="itemType = 'custom'">自定义主题</button>
        </div>
      </div>
      <div class="form-group" v-if="itemType === 'course'">
        <label>选择课程 <span class="req">*</span></label>
        <select v-model="itemForm.courseId" class="form-input">
          <option :value="null" disabled>请选择课程</option>
          <option v-for="c in enrolledCourses" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </div>
      <div class="form-group" v-if="itemType === 'custom'">
        <label>学习主题 <span class="req">*</span></label>
        <input v-model="itemForm.customTopic" placeholder="如：复习数据结构" class="form-input" />
      </div>
      <div class="form-group" v-if="!editingItem">
        <label>重复设置 <span class="req">*</span></label>
        <div class="repeat-mode-selector">
          <button :class="{ active: itemForm.repeatMode === 'once' }" @click="itemForm.repeatMode = 'once'">
            单次
          </button>
          <button :class="{ active: itemForm.repeatMode === 'weekly' }" @click="itemForm.repeatMode = 'weekly'">
            每周重复
          </button>
        </div>
      </div>
      <!-- 单次模式：显示日期选择器 -->
      <div class="form-group" v-if="!editingItem && itemForm.repeatMode === 'once'">
        <label>计划日期 <span class="req">*</span></label>
        <input v-model="itemForm.planDate" type="date" class="form-input" />
      </div>
      <!-- 每周重复模式：显示星期几选择器 -->
      <div class="form-group" v-if="!editingItem && itemForm.repeatMode === 'weekly'">
        <label>选择星期 <span class="req">*</span></label>
        <div class="weekday-selector">
          <button
            v-for="day in weekdayOptions"
            :key="day.value"
            :class="{ selected: itemForm.selectedWeekdays.includes(day.value) }"
            @click="toggleWeekday(day.value)"
            class="weekday-btn"
          >
            {{ day.label }}
          </button>
        </div>
        <div class="weekday-hint" v-if="itemForm.selectedWeekdays.length > 0">
          将在计划周期内的每个{{ selectedWeekdayText }}创建任务
        </div>
      </div>
      <!-- 编辑模式：显示日期选择器 -->
      <div class="form-group" v-if="editingItem">
        <label>计划日期 <span class="req">*</span></label>
        <input v-model="itemForm.planDate" type="date" class="form-input" />
      </div>
      <div class="form-row">
        <div class="form-group half">
          <label>开始时间</label>
          <input v-model="itemForm.startTime" type="time" class="form-input" />
        </div>
        <div class="form-group half">
          <label>结束时间</label>
          <input v-model="itemForm.endTime" type="time" class="form-input" />
        </div>
      </div>
      <div class="form-group">
        <label>备注</label>
        <textarea v-model="itemForm.notes" placeholder="可选" class="form-input" rows="2"></textarea>
      </div>
      <template #footer>
        <button class="btn-cancel" @click="showItemDialog = false">取消</button>
        <button class="btn-primary" @click="submitItem">{{ editingItem ? '保存' : '添加' }}</button>
      </template>
    </el-dialog>

    <!-- ==================== 对话框：日期详情 ==================== -->
    <el-dialog v-model="showDayDialog" :title="selectedDate + ' 任务'" width="420" class="plan-dialog">
      <div v-if="selectedDateItems.length === 0" class="empty-state small">
        <p>这天还没有安排学习任务</p>
      </div>
      <div class="day-item" v-for="item in selectedDateItems" :key="item.id">
        <label class="checkbox-wrap" @click.stop>
          <input type="checkbox" :checked="item.completed" @change="toggleComplete(item); loadCalendarItems()" />
          <span class="checkmark"></span>
        </label>
        <div class="day-info" :class="{ done: item.completed }">
          <div class="day-name">{{ itemName(item) }}</div>
          <div class="day-meta">
            <span v-if="item.startTime">{{ item.startTime }}{{ item.endTime ? ' - ' + item.endTime : '' }}</span>
            <span v-if="item.notes" class="day-notes">{{ item.notes }}</span>
          </div>
        </div>
        <div class="item-ops" @click.stop>
          <button class="icon-btn sm" @click="openEditItem(item)">
            <svg width="12" height="12" viewBox="0 0 14 14" fill="none"><path d="M9.5 1.5l3 3L4 13H1v-3L9.5 1.5z" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
          <button class="icon-btn sm danger" @click="deleteItem(item)">
            <svg width="12" height="12" viewBox="0 0 14 14" fill="none"><path d="M2 4h10M5 4V2h4v2M3 4v8a1 1 0 001 1h6a1 1 0 001-1V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
        </div>
      </div>
      <template #footer>
        <button class="btn-primary" @click="showDayDialog = false">关闭</button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <ConfirmDeleteModal v-model="showDeleteModal" :message="deleteMessage" @confirm="handleDeleteConfirm" />
  </div>
</template>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.page {
  min-height: 100vh;
  position: relative;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  color: #1d1d1f;
  -webkit-font-smoothing: antialiased;
}

.bg {
  position: fixed; inset: 0; z-index: 0;
  background-image: url('@/assets/images/4.jpg');
  background-size: cover;
  background-position: center 40%;
  background-repeat: no-repeat;
  transition: filter 0.1s linear;
}

.bg-dim {
  position: fixed; inset: 0; z-index: 1;
  transition: background 0.1s linear;
  pointer-events: none;
}

.navbar {
  position: sticky; top: 0; z-index: 100;
  height: 52px;
  display: flex; align-items: center; justify-content: center;
  padding: 0 20px;
  background: rgba(255,255,255,0.65);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border-bottom: 0.5px solid rgba(255,255,255,0.65);
  box-shadow: 0 1px 0 rgba(0,0,0,0.05);
}
.navbar-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn {
  display: flex; align-items: center; gap: 5px;
  background: none; border: none; cursor: pointer;
  font-family: inherit; font-size: 16px;
  color: #0071e3; padding: 0;
  transition: opacity 0.1s;
}
.nav-btn:hover { opacity: 0.7; }
.navbar-title { font-size: 18px; font-weight: 600; letter-spacing: -0.02em; color: #1d1d1f; }

.main {
  position: relative; z-index: 2;
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 28px 72px;
}

.glass-tabs {
  border-radius: 24px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: saturate(200%) blur(40px);
  -webkit-backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.10), 0 0.5px 0 rgba(255,255,255,0.95) inset;
}

.tab-bar {
  display: flex; gap: 6px; padding: 14px 20px 0;
  border-bottom: 0.5px solid rgba(0,0,0,0.07);
}

.tab-btn {
  position: relative; padding: 12px 28px;
  background: none; border: none; cursor: pointer;
  font-family: inherit; font-size: 16px; font-weight: 500;
  color: #86868b; border-radius: 12px 12px 0 0;
  transition: color 0.15s, background 0.15s;
}
.tab-btn:hover { color: #1d1d1f; background: rgba(0,0,0,0.03); }
.tab-btn.active { color: #1d1d1f; font-weight: 700; }
.tab-btn.active::after {
  content: ''; position: absolute;
  bottom: -0.5px; left: 20px; right: 20px;
  height: 3px; background: #1d1d1f;
  border-radius: 3px 3px 0 0;
}

.tab-content { padding: 28px 24px; }

/* ============ Meeting Card 日历 ============ */
.meeting-card {
  background: rgba(255,255,255,0.62);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-radius: 2rem;
  padding: 2rem 2rem 1.8rem;
  width: 100%;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08), 0 1px 0 rgba(255,255,255,0.9) inset;
  border: 0.5px solid rgba(255,255,255,0.7);
}

.mc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
}

.mc-title {
  font-size: 2rem;
  font-weight: bold;
  line-height: 1.2;
}

.mc-date-selector {
  background: rgba(255,255,255,0.7);
  border-radius: 9999px;
  padding: 0.45rem 0.9rem;
  border: 0.5px solid rgba(0,0,0,0.08);
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: inherit;
  font-size: 0.95rem;
  font-weight: 500;
  color: #1d1d1f;
}
.mc-arrow {
  width: 34px; height: 34px; border-radius: 50%;
  border: none; background: none; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: #1d1d1f; transition: background 0.15s;
}
.mc-arrow:hover { background: rgba(0,0,0,0.08); }

.mc-calls-info {
  display: flex;
  align-items: center;
  margin-bottom: 1.4rem;
  color: #6e6e73;
  font-size: 0.95rem;
}
.mc-calls-info svg { flex-shrink: 0; margin-right: 8px; }
.mc-calls-info span { margin-left: 0; }

.mc-date-nav-and-indicators { position: relative; }

.mc-date-nav-container {
  background-color: white;
  border-radius: 20px;
  padding: 16px 10px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.mc-day-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  cursor: pointer;
  transition: transform 0.15s;
}
.mc-day-item:active { transform: scale(0.95); }

.mc-day-number,
.mc-day-name {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 48px;
  background-color: transparent;
  transition: background-color 0.2s ease;
}

.mc-day-number {
  font-size: 1.35rem;
  font-weight: 600;
  height: 34px;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  padding-top: 4px;
}

.mc-day-name {
  font-size: 0.75rem;
  height: 22px;
  color: #666;
  border-bottom-left-radius: 24px;
  border-bottom-right-radius: 24px;
}

.mc-day-item:hover .mc-day-number,
.mc-day-item:hover .mc-day-name { background-color: #f8f8f8; }

.mc-day-active .mc-day-number,
.mc-day-active .mc-day-name { background-color: #f0ff7a; }

.mc-indicator-container {
  display: flex;
  justify-content: space-between;
  width: 100%;
  position: relative;
  padding: 0 30px;
  box-sizing: border-box;
}

.mc-indicator-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background-color: #d0d0ce;
  position: relative;
  z-index: 2;
  transition: background 0.2s;
}

.mc-indicator-active { background-color: #1d1d1f; }

.mc-indicator-line {
  position: absolute;
  top: 50%;
  left: 34px;
  right: 34px;
  height: 1px;
  border-top: 1.5px dashed #d0d0ce;
  z-index: 1;
}

/* ============ 选中日期任务区 ============ */
.selected-day-section { margin-top: 24px; }
.selected-day-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px;
}
.selected-day-header .sec-title { margin-bottom: 0; }

.btn-primary.sm {
  padding: 8px 18px; font-size: 13px;
}

/* ============ 今日任务 ============ */
.sec-title {
  font-size: 15px; font-weight: 700; color: #86868b;
  text-transform: uppercase; letter-spacing: 0.05em;
  margin-bottom: 14px;
}
.today-list { display: flex; flex-direction: column; gap: 10px; }
.today-item {
  display: flex; align-items: center; gap: 14px;
  padding: 16px 20px;
  background: rgba(255,255,255,0.6);
  border-radius: 16px;
  border: 0.5px solid rgba(0,0,0,0.05);
}
.today-info { flex: 1; display: flex; align-items: center; gap: 10px; }
.today-info.done .today-name { text-decoration: line-through; color: #86868b; }
.today-time { font-size: 14px; color: #0071e3; font-weight: 600; white-space: nowrap; }
.today-name { font-size: 16px; font-weight: 500; }

/* ============ 复选框 ============ */
.checkbox-wrap {
  display: flex; align-items: center; cursor: pointer;
}
.checkbox-wrap input { display: none; }
.checkmark {
  width: 22px; height: 22px; border-radius: 50%;
  border: 2px solid #c7c7cc; display: flex;
  align-items: center; justify-content: center;
  transition: all 0.2s;
}
.checkbox-wrap input:checked + .checkmark {
  background: #34c759; border-color: #34c759;
}
.checkbox-wrap input:checked + .checkmark::after {
  content: ''; width: 6px; height: 10px;
  border: solid white; border-width: 0 2.2px 2.2px 0;
  transform: rotate(45deg) translateY(-1px);
}

/* ============ 计划列表 ============ */
.list-section {}
.list-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
}
.list-header .sec-title { margin-bottom: 0; }

.btn-primary {
  display: inline-flex; align-items: center;
  padding: 10px 26px; border-radius: 980px;
  background: #1d1d1f; color: #fff;
  font-size: 15px; font-weight: 600;
  border: none; cursor: pointer;
  transition: opacity 0.15s;
  font-family: inherit;
}
.btn-primary:hover { opacity: 0.8; }

.btn-cancel {
  display: inline-flex; align-items: center;
  padding: 10px 26px; border-radius: 980px;
  background: #f5f5f7; color: #1d1d1f;
  font-size: 15px; font-weight: 600;
  border: none; cursor: pointer;
  font-family: inherit; margin-right: 10px;
}

.empty-state {
  text-align: center; padding: 52px 24px;
  color: #86868b; font-size: 16px;
}
.empty-state svg { margin-bottom: 16px; }
.empty-state.small { padding: 28px; }
.empty-state p { margin: 0; }

/* ============ 计划卡片 ============ */
.plan-card {
  background: rgba(255,255,255,0.6);
  border-radius: 18px;
  border: 0.5px solid rgba(0,0,0,0.06);
  padding: 22px;
  margin-bottom: 16px;
  transition: box-shadow 0.2s;
}
.plan-card:hover { box-shadow: 0 2px 16px rgba(0,0,0,0.06); }

.plan-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  cursor: pointer;
}
.plan-title { font-size: 19px; font-weight: 600; margin: 0 0 6px; }
.plan-meta {
  display: flex; align-items: center; gap: 10px;
  font-size: 14px; color: #86868b;
}
.status-tag {
  padding: 3px 10px; border-radius: 8px;
  font-size: 12px; font-weight: 600;
}

.plan-actions {
  display: flex; align-items: center; gap: 6px; flex-shrink: 0;
}
.icon-btn {
  width: 34px; height: 34px; border-radius: 10px;
  border: none; background: none; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: #86868b; transition: all 0.15s;
}
.icon-btn:hover { background: rgba(0,0,0,0.06); color: #1d1d1f; }
.icon-btn.danger:hover { color: #ff3b30; background: rgba(255,59,48,0.08); }
.icon-btn.sm { width: 30px; height: 30px; }

.expand-icon {
  margin-left: 4px; color: #86868b;
  transition: transform 0.2s;
}
.expand-icon.expanded { transform: rotate(180deg); }

/* 进度条 */
.progress-bar-wrap {
  display: flex; align-items: center; gap: 12px;
  margin-top: 14px;
}
.progress-bar {
  flex: 1; height: 6px; border-radius: 3px;
  background: rgba(0,0,0,0.06); overflow: hidden;
}
.progress-fill {
  height: 100%; border-radius: 3px;
  background: linear-gradient(90deg, #0071e3, #34c759);
  transition: width 0.3s;
}
.progress-text { font-size: 13px; color: #86868b; white-space: nowrap; }

/* 计划任务列表 */
.plan-items { margin-top: 14px; padding-top: 14px; border-top: 0.5px solid rgba(0,0,0,0.06); }
.plan-item-row {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 0;
}
.plan-item-row + .plan-item-row { border-top: 0.5px solid rgba(0,0,0,0.04); }
.item-info { flex: 1; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.item-info.done .item-name { text-decoration: line-through; color: #86868b; }
.item-date { font-size: 14px; color: #86868b; }
.item-time { font-size: 14px; color: #0071e3; font-weight: 500; }
.item-name { font-size: 16px; font-weight: 500; }
.item-ops { display: flex; gap: 4px; }

/* ============ 日期详情对话框 ============ */
.day-item {
  display: flex; align-items: flex-start; gap: 14px;
  padding: 14px 0;
}
.day-item + .day-item { border-top: 0.5px solid rgba(0,0,0,0.06); }
.day-info { flex: 1; }
.day-info.done .day-name { text-decoration: line-through; color: #86868b; }
.day-name { font-size: 16px; font-weight: 500; }
.day-meta { font-size: 13px; color: #86868b; margin-top: 4px; display: flex; flex-direction: column; gap: 3px; }
.day-notes { color: #6e6e73; }

/* ============ 表单样式 ============ */
.form-group { margin-bottom: 20px; }
.form-group label {
  display: block; font-size: 14px; font-weight: 600;
  color: #1d1d1f; margin-bottom: 8px;
}
.form-group .req { color: #ff3b30; }
.form-input {
  width: 100%; padding: 13px 14px;
  border: 1px solid #d2d2d7; border-radius: 12px;
  font-size: 15px; font-family: inherit;
  background: rgba(255,255,255,0.8);
  transition: border-color 0.15s;
  outline: none;
}
.form-input:focus { border-color: #0071e3; }
textarea.form-input { resize: vertical; min-height: 56px; }

.form-row { display: flex; gap: 14px; }
.form-group.half { flex: 1; }

.type-switch {
  display: flex; gap: 4px; background: rgba(0,0,0,0.04);
  padding: 4px; border-radius: 12px;
}
.type-switch button {
  flex: 1; padding: 10px 14px; border: none;
  border-radius: 10px; font-size: 14px; font-weight: 500;
  cursor: pointer; background: none; color: #86868b;
  font-family: inherit; transition: all 0.15s;
}
.type-switch button.active {
  background: #fff; color: #1d1d1f; font-weight: 600;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

/* 重复模式选择器 */
.repeat-mode-selector {
  display: flex;
  gap: 8px;
  background: rgba(0,0,0,0.04);
  padding: 4px;
  border-radius: 12px;
  margin-bottom: 12px;
}
.repeat-mode-selector button {
  flex: 1;
  padding: 10px 14px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  background: none;
  color: #86868b;
  font-family: inherit;
  transition: all 0.15s;
}
.repeat-mode-selector button.active {
  background: #fff;
  color: #1d1d1f;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

/* 星期几选择器 */
.weekday-selector {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}
.weekday-btn {
  padding: 12px 8px;
  border: 1px solid #d2d2d7;
  border-radius: 10px;
  background: #fff;
  font-size: 13px;
  font-weight: 500;
  color: #1d1d1f;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
}
.weekday-btn:hover {
  background: #f5f5f7;
  border-color: #0071e3;
}
.weekday-btn.selected {
  background: #0071e3;
  color: #fff;
  border-color: #0071e3;
}

/* 星期选择提示 */
.weekday-hint {
  margin-top: 10px;
  font-size: 13px;
  color: #86868b;
  font-style: italic;
}

/* ============ 分页 ============ */
.pagination {
  display: flex; align-items: center; justify-content: center;
  gap: 14px; margin-top: 24px;
}
.page-btn {
  padding: 8px 18px; border-radius: 10px;
  border: 1px solid #d2d2d7; background: #fff;
  font-size: 14px; cursor: pointer; font-family: inherit;
  transition: all 0.15s;
}
.page-btn:hover:not(:disabled) { background: #f5f5f7; }
.page-btn:disabled { opacity: 0.4; cursor: default; }
.page-info { font-size: 14px; color: #86868b; }

/* ============ 响应式 ============ */
@media (max-width: 768px) {
  .main { padding: 18px 16px 56px; }
  .navbar { padding: 0 16px; }
  .tab-content { padding: 20px 16px; }
  .tab-btn { padding: 10px 16px; font-size: 14px; }
  .mc-title { font-size: 1.5rem; }
  .mc-date-selector { padding: 0.45rem 0.8rem; font-size: 0.85rem; }
  .mc-day-number, .mc-day-name { width: 36px; }
  .mc-day-number { font-size: 1.05rem; }
  .mc-day-name { font-size: 0.65rem; }
  .meeting-card { padding: 1.2rem; border-radius: 1.8rem; }
  .plan-header { flex-direction: column; gap: 10px; }
  .plan-actions { align-self: flex-end; }
}
</style>
