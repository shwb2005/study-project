<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { get, post } from "@/net"
import { ElMessage } from "element-plus"
import { useStore } from "@/stores"
import axios from 'axios'

const store = useStore()
const router = useRouter()
const profile = ref({})
const loading = ref(false)
const myCourses = ref([])
const logs = ref([])

// 初始化个人资料数据结构
const initProfile = () => ({
  id: '',
  realName: '',
  phone: '',
  gender: '',
  birthday: '',
  bio: '',
  avatar: '/avatars/default.png',
  lastLoginTime: '',
  created_at: '',
  updated_at: ''
})

// 处理后端返回的数据 - 将下划线字段转为驼峰
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

// 加载个人资料
const loadProfile = () => {
  console.log('开始加载个人资料...')
  get('/api/profile', data => {
    console.log('个人资料API返回数据:', data)
    profile.value = processProfileData(data)

    // 处理性别字段映射
    if (profile.value.gender) {
      profile.value.gender = profile.value.gender.toUpperCase()
    }

    console.log('处理后的个人资料数据:', profile.value)
  }, error => {
    console.error('加载个人资料失败:', error)
    profile.value = initProfile()
  })
}

// 加载我的课程数据
const loadMyCourses = () => {
  console.log('开始加载我的课程...')
  get('/api/course/my-courses', data => {
    console.log('我的课程API返回数据:', data)
    if (data && Array.isArray(data)) {
      myCourses.value = data
      // 调试：打印每个课程的签到信息
      myCourses.value.forEach((course, index) => {
        console.log(`课程 ${index + 1}:`, {
          name: course.course_name || course.name,
          sign_in_count: course.sign_in_count,
          check_in_count: course.check_in_count,
          sign_count: course.sign_count,
          attendance_count: course.attendance_count,
          all_fields: Object.keys(course) // 显示所有可用字段
        })
      })
    } else {
      myCourses.value = []
    }
    console.log('设置的我的课程数据:', myCourses.value)
  }, error => {
    console.error('加载我的课程失败:', error)
    myCourses.value = []
  })
}

// 加载日志数据 - 使用模拟数据
const loadLogs = () => {
  console.log('加载模拟日志数据...')
  // 模拟日志数据 - 最多5条
  const mockLogs = [
    { id: 1, action: '登录系统', time: new Date().toISOString(), ip: '192.168.1.100' },
    { id: 2, action: '查看个人资料', time: new Date(Date.now() - 30 * 60 * 1000).toISOString(), ip: '192.168.1.100' },
    { id: 3, action: '更新个人信息', time: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(), ip: '192.168.1.100' },
    { id: 4, action: '浏览课程', time: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(), ip: '192.168.1.100' },
    { id: 5, action: '课程签到', time: new Date(Date.now() - 26 * 60 * 60 * 1000).toISOString(), ip: '192.168.1.100' }
  ]

  logs.value = mockLogs.slice(0, 5) // 确保最多5条
  console.log('模拟日志数据已加载 (最多5条):', logs.value)
}

// 手动测试签到功能
const testSignIn = () => {
  console.log('=== 手动测试签到功能 ===')
  console.log('当前课程数据:', myCourses.value)

  // 尝试多种可能的签到字段
  myCourses.value.forEach((course, index) => {
    const possibleFields = [
      'sign_in_count', 'check_in_count', 'sign_count',
      'attendance_count', 'signInCount', 'checkInCount'
    ]

    console.log(`课程 ${index + 1} "${course.course_name || course.name}" 的签到字段:`)
    possibleFields.forEach(field => {
      if (course[field] !== undefined) {
        console.log(`  ${field}:`, course[field])
      }
    })
  })

  ElMessage.info('请在控制台查看签到字段调试信息')
}

// 日志调试功能
const debugLogs = () => {
  console.log('=== 日志调试信息 ===')
  console.log('当前日志数量:', logs.value.length)
  console.log('日志数据详情:', logs.value)

  // 添加一条测试日志
  const testLog = {
    id: Date.now(),
    action: '调试操作',
    time: new Date().toISOString(),
    ip: '127.0.0.1'
  }

  // 添加到开头，并保持最多5条
  logs.value.unshift(testLog)
  if (logs.value.length > 5) {
    logs.value = logs.value.slice(0, 5)
  }

  console.log('添加测试日志后的日志数据:', logs.value)
  ElMessage.success('已添加测试日志，请在控制台查看详情')
}

// 清空日志
const clearLogs = () => {
  logs.value = []
  ElMessage.info('日志已清空')
}

// 更新个人资料
const updateProfile = () => {
  console.log('=== 更新个人资料 ===')

  // 表单验证
  if (profile.value.phone && !/^1[3-9]\d{9}$/.test(profile.value.phone)) {
    ElMessage.error('请输入正确的手机号码')
    return
  }

  // 同时发送驼峰和下划线两种字段名
  const submitData = {
    realName: profile.value.realName || '',
    real_name: profile.value.realName || '',
    phone: profile.value.phone || '',
    gender: profile.value.gender ? profile.value.gender.toLowerCase() : '',
    birthday: profile.value.birthday || '',
    bio: profile.value.bio || ''
  }

  console.log('提交的数据:', submitData)
  loading.value = true

  axios.post('/api/profile', submitData, {
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true
  }).then(({ data }) => {
    if (data.success) {
      console.log('✅ 更新成功:', data.message)
      ElMessage.success(data.message)
      loadProfile()
      // 添加更新日志 - 保持最多5条
      const newLog = {
        id: Date.now(),
        action: '更新个人资料',
        time: new Date().toISOString(),
        ip: '192.168.1.100'
      }
      logs.value.unshift(newLog)
      if (logs.value.length > 5) {
        logs.value = logs.value.slice(0, 5)
      }
    } else {
      console.error('❌ 更新失败:', data.message)
      ElMessage.error(data.message || '更新失败')
    }
    loading.value = false
  }).catch(error => {
    console.error('❌ 请求错误:', error)
    ElMessage.error('网络错误，请稍后重试')
    loading.value = false
  })
}

// 重置表单
const resetForm = () => {
  loadProfile()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '--'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  } catch {
    return dateString
  }
}

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '--'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN')
  } catch {
    return dateString
  }
}

// 计算属性
const myCoursesCount = computed(() => myCourses.value.length)

// 所有课程签到次数总和 - 增强调试功能
const totalCheckInCount = computed(() => {
  let total = 0
  let foundFields = []

  myCourses.value.forEach((course, index) => {
    // 尝试多种可能的字段名
    const possibleFields = [
      'sign_in_count', 'check_in_count', 'sign_count',
      'attendance_count', 'signInCount', 'checkInCount'
    ]

    let courseSignCount = 0
    let usedField = null

    for (const field of possibleFields) {
      if (course[field] !== undefined && course[field] !== null) {
        courseSignCount = parseInt(course[field]) || 0
        usedField = field
        foundFields.push({ course: course.course_name || course.name, field, value: courseSignCount })
        break
      }
    }

    total += courseSignCount

    if (usedField) {
      console.log(`课程 ${index + 1} "${course.course_name || course.name}" 使用字段 "${usedField}": ${courseSignCount}`)
    } else {
      console.log(`课程 ${index + 1} "${course.course_name || course.name}" 未找到签到字段`)
    }
  })

  console.log('总签到次数计算完成:', total)
  console.log('找到的签到字段:', foundFields)

  return total
})

onMounted(() => {
  console.log('个人中心页面已加载')
  loadProfile()
  loadMyCourses()
  loadLogs()
})
</script>

<template>
  <div class="profile-page">
    <div class="header">
      <div class="header-left">
        <el-button @click="router.push('/index')" type="text" icon="el-icon-back">
          返回首页
        </el-button>
        <h1>个人中心</h1>
      </div>
      <div class="header-right">
        <el-button @click="router.push('/courses')" type="primary" plain>
          课程中心
        </el-button>
      </div>
    </div>

    <div class="profile-content">
      <!-- 用户信息卡片 -->
      <el-card class="user-card" shadow="never">
        <div class="user-header">
          <div class="avatar-section">
            <el-avatar :size="80" :src="profile.avatar" class="user-avatar">
              {{ profile.realName ? profile.realName.charAt(0) : 'U' }}
            </el-avatar>
          </div>
          <div class="user-info">
            <h2 class="user-name">{{ profile.realName || '未设置姓名' }}</h2>
            <p class="user-bio">{{ profile.bio || '暂无个人简介' }}</p>
            <div class="user-meta">
              <span class="meta-item">
                <i class="el-icon-user"></i>
                ID: {{ profile.id || '--' }}
              </span>
              <span class="meta-item" v-if="profile.phone">
                <i class="el-icon-phone"></i>
                {{ profile.phone }}
              </span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 编辑资料卡片 -->
      <el-card class="profile-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>编辑资料</span>
          </div>
        </template>

        <el-form :model="profile" label-width="100px">
          <el-form-item label="真实姓名">
            <el-input
                v-model="profile.realName"
                placeholder="请输入真实姓名"
                maxlength="20"
                clearable
            />
          </el-form-item>

          <el-form-item label="手机号码">
            <el-input
                v-model="profile.phone"
                placeholder="请输入手机号码"
                maxlength="11"
                clearable
            />
          </el-form-item>

          <el-form-item label="性别">
            <el-radio-group v-model="profile.gender">
              <el-radio label="MALE">男</el-radio>
              <el-radio label="FEMALE">女</el-radio>
              <el-radio label="OTHER">其他</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="生日">
            <el-date-picker
                v-model="profile.birthday"
                type="date"
                placeholder="选择生日"
                value-format="YYYY-MM-DD"
                style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="个人简介">
            <el-input
                v-model="profile.bio"
                type="textarea"
                :rows="3"
                placeholder="请输入个人简介"
                maxlength="200"
                show-word-limit
            />
          </el-form-item>

          <el-form-item>
            <el-button
                type="primary"
                @click="updateProfile"
                :loading="loading"
            >
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 学习统计 -->
      <el-card class="stats-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>学习统计</span>
          </div>
        </template>

        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">{{ myCoursesCount }}</div>
            <div class="stat-label">在学课程</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ totalCheckInCount }}</div>
            <div class="stat-label">签到次数</div>
          </div>
        </div>
        <div class="stats-debug" v-if="totalCheckInCount === 0">
          <el-alert title="签到次数为0，请点击右上角'调试签到'按钮查看详细字段信息" type="warning" show-icon :closable="false" />
        </div>
      </el-card>


      <!-- 账户信息 -->
      <el-card class="account-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>账户信息</span>
          </div>
        </template>

        <div class="account-info">
          <div class="info-item">
            <span class="label">用户名:</span>
            <span class="value">{{ store.auth.user?.username || '--' }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱:</span>
            <span class="value">{{ store.auth.user?.email || '--' }}</span>
          </div>
          <div class="info-item">
            <span class="label">上一次登录:</span>
            <span class="value">{{ formatDate(profile.lastLoginTime) }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-left h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-card, .profile-card, .stats-card, .logs-card, .account-card {
  border-radius: 8px;
  border: 1px solid #eaeaea;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.log-actions {
  display: flex;
  gap: 5px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.user-avatar {
  border: 3px solid #1890ff;
}

.user-info {
  flex: 1;
}

.user-name {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.user-bio {
  margin: 0 0 12px 0;
  color: #666;
  line-height: 1.5;
}

.user-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #888;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 15px;
}

.stat-item {
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 8px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stats-debug {
  margin-top: 10px;
}

.logs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  padding: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  background: #fafafa;
}

.log-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-action {
  font-weight: 500;
  color: #333;
}

.log-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.log-time {
  font-size: 12px;
  color: #666;
}

.log-ip {
  font-size: 12px;
  color: #999;
}

.empty-logs {
  text-align: center;
  color: #999;
  padding: 20px;
}

.account-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  color: #666;
  font-weight: 500;
}

.value {
  color: #333;
  font-weight: bold;
}

@media (max-width: 768px) {
  .profile-page {
    padding: 10px;
  }

  .header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .header-right {
    align-self: flex-end;
  }

  .user-header {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }

  .stats-grid {
    grid-template-columns: repeat(1, 1fr);
  }

  .log-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .log-meta {
    align-items: flex-start;
  }

  .log-actions {
    flex-direction: column;
    gap: 2px;
  }
}
</style>