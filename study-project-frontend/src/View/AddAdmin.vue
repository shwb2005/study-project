<template>
  <div class="all-courses">
    <div class="tab-header">
      <div class="header-content">
        <h2 class="header-title">管理员管理</h2>
        <span class="course-count">{{ adminList.length }} 个管理员</span>
      </div>
    </div>

    <div class="toolbar">
      <el-button @click="goToHome" class="home-btn">
        <i class="el-icon-house"></i>
        返回首页
      </el-button>
      <el-button type="primary" @click="showAddDialog = true">
        <i class="el-icon-plus"></i>
        添加管理员
      </el-button>
    </div>

    <!-- 管理员列表 -->
    <div class="course-grid">
      <div
          v-for="admin in adminList"
          :key="admin.id"
          class="course-card"
      >
        <div class="card-inner">
          <div class="course-header">
            <h3 class="course-name">{{ admin.username || '未命名管理员' }}</h3>
          </div>

          <div class="course-content">
            <p class="course-desc">管理员账户 ID: {{ admin.id }}</p>

            <div class="divider"></div>

            <div class="course-meta">
              <div class="meta-item">
                <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" stroke-width="2"
                        stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>ID: {{ admin.id }}</span>
              </div>

              <div class="meta-item">
                <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" stroke-width="2"
                        stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>管理员</span>
              </div>
            </div>

            <div class="course-stats">
              <div class="stat-item">
                <span class="stat-value">{{ admin.id }}</span>
                <span class="stat-label">ID</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ admin.username ? admin.username.substring(0, 3) + '***' : 'N/A' }}</span>
                <span class="stat-label">用户名</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ admin.createTime ? formatDate(admin.createTime) : 'N/A' }}</span>
                <span class="stat-label">创建时间</span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <button
                v-if="admin.id !== currentAdminId"
                class="enroll-btn"
                @click="handleDelete(admin)"
            >
              删除管理员
            </button>
            <button
                v-else
                class="enrolled-btn"
                disabled
            >
              <svg class="check-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M5 13l4 4L19 7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              当前用户
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="adminList.length === 0" class="empty-state">
      <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <p class="empty-text">暂无管理员</p>
    </div>

    <!-- 添加管理员弹窗 -->
    <el-dialog
        v-model="showAddDialog"
        title="添加管理员"
        width="500px"
        :before-close="handleCloseDialog"
    >
      <el-form
          :model="addForm"
          :rules="addRules"
          ref="addFormRef"
          label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="addForm.username"
              placeholder="请输入用户名"
              maxlength="20"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="addForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              maxlength="20"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button
              type="primary"
              @click="handleAddAdmin"
              :loading="adding"
          >
            确认添加
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from '@/stores/index.js'
import { get, post } from '@/net/index.js'

const router = useRouter()
const store = useStore()

// 状态管理
const loading = ref(false)
const adding = ref(false)
const showAddDialog = ref(false)
const adminList = ref([])
const currentAdminId = ref(null)

// 表单引用
const addFormRef = ref()

// 表单数据
const addForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const addRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (dateString) => {
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  } catch (e) {
    return 'N/A'
  }
}

// 返回首页
const goToHome = () => {
  console.log('🏠 返回首页')
  router.push('/admin')
}

// 检查登录状态
const checkAdminLogin = () => {
  return new Promise((resolve) => {
    if (store.auth.admin) {
      console.log('Store 中已有管理员登录状态')
      resolve(true)
    } else {
      console.log('Store 中无管理员登录状态')
      resolve(false)
    }
  })
}

// 加载管理员列表
const loadAdminList = () => {
  loading.value = true
  get('/api/admin/list',
      (message) => {
        console.log('获取管理员列表成功:', message)
        // 根据实际返回数据结构调整
        // 如果返回的是 {success: true, message: "成功", data: [...]}
        if (message && message.data) {
          adminList.value = message.data
        } else if (Array.isArray(message)) {
          adminList.value = message
        } else {
          adminList.value = []
        }
        loading.value = false
      },
      (message) => {
        ElMessage.error(message || '加载管理员列表失败')
        adminList.value = []
        loading.value = false
      }
  )
}

// 添加管理员
const handleAddAdmin = () => {
  if (!addFormRef.value) return

  addFormRef.value.validate((valid) => {
    if (valid) {
      adding.value = true

      console.log('🔄 开始添加管理员:', {
        username: addForm.username,
        password: addForm.password
      })

      post('/api/admin/add', {
            username: addForm.username,
            password: addForm.password
          },
          (message) => {
            console.log('✅ 添加管理员成功:', message)
            ElMessage.success(message)
            handleCloseDialog()
            loadAdminList()
            adding.value = false
          },
          (message) => {
            console.error('❌ 添加管理员失败:', message)
            ElMessage.error(message || '添加失败')
            adding.value = false
          }
      )
    } else {
      console.log('❌ 表单验证失败')
    }
  })
}

// 删除管理员
const handleDelete = (admin) => {
  ElMessageBox.confirm(
      `确定要删除管理员 "${admin.username}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    post('/api/admin/delete', { id: admin.id },
        (message) => {
          ElMessage.success(message || '管理员删除成功')
          loadAdminList()
        },
        (message) => {
          ElMessage.error(message || '删除失败')
        }
    )
  }).catch(() => {
    // 用户取消
  })
}

// 关闭弹窗
const handleCloseDialog = () => {
  showAddDialog.value = false
  if (addFormRef.value) {
    addFormRef.value.resetFields()
  } else {
    addForm.username = ''
    addForm.password = ''
  }
}

// 获取当前登录管理员信息
const getCurrentAdmin = () => {
  get('/api/admin/me',
      (message) => {
        // 根据实际返回数据结构调整
        if (message && (message.id || message.data?.id)) {
          currentAdminId.value = message.id || message.data.id
          console.log('当前登录管理员ID:', currentAdminId.value)
        }
      },
      (message) => {
        console.log('获取当前管理员信息失败:', message)
      }
  )
}

// 生命周期
onMounted(async () => {
  const isLoggedIn = await checkAdminLogin()
  if (!isLoggedIn) {
    ElMessage.warning('请先登录管理员账户')
    router.push('/admin-login')
    return
  }

  loadAdminList()
  getCurrentAdmin()
})
</script>

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

.toolbar {
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
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

  .toolbar {
    flex-direction: column;
  }
}

/* Element Plus 弹窗样式调整 */
:deep(.el-dialog) {
  border-radius: 16px !important;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 24px;
}

:deep(.el-dialog__body) {
  padding: 24px !important;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #1a1a1a;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

:deep(.el-input__wrapper):hover {
  border-color: #8e8e93;
}

:deep(.el-input__wrapper).is-focus {
  border-color: #1a1a1a;
  box-shadow: 0 0 0 2px rgba(26, 26, 26, 0.1);
}

:deep(.el-button--primary) {
  background-color: #1a1a1a;
  border-color: #1a1a1a;
}

:deep(.el-button--primary):hover {
  background-color: #000000;
  border-color: #000000;
}

.home-btn {
  background-color: #67c23a !important;
  border-color: #67c23a !important;
  color: white !important;
}

.home-btn:hover {
  background-color: #5daf34 !important;
  border-color: #5daf34 !important;
}
</style>
