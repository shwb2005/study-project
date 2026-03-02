<template>
  <div class="all-courses">
    <div class="tab-header">
      <div class="header-content">
        <h2 class="header-title">课程管理</h2>
        <span class="course-count">{{ courseList.length }} 门课程</span>
      </div>
    </div>

    <div class="toolbar">
      <el-button @click="goToHome" class="home-btn">
        <i class="el-icon-house"></i>
        返回首页
      </el-button>
      <el-button type="primary" @click="showAddDialog = true">
        <i class="el-icon-plus"></i>
        添加课程
      </el-button>
    </div>

    <!-- 课程列表 -->
    <div class="course-grid">
      <div
          v-for="course in courseList"
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
                <span>{{ course.teacherName || '未知教师' }}</span>
              </div>

              <div class="meta-item">
                <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" stroke-width="2"
                        stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>{{ course.duration || '未知时长' }}</span>
              </div>
            </div>

            <div class="course-stats">
              <div class="stat-item">
                <span class="stat-value">{{ course.id }}</span>
                <span class="stat-label">ID</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ course.studentsCount || 0 }}</span>
                <span class="stat-label">学生数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ course.maxCheckInCount || 0 }}</span>
                <span class="stat-label">最大签到</span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <button
                class="enroll-btn edit-btn"
                @click="handleEdit(course)"
            >
              编辑
            </button>
            <button
                class="enroll-btn delete-btn"
                @click="handleDelete(course)"
            >
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="courseList.length === 0" class="empty-state">
      <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <p class="empty-text">暂无课程</p>
    </div>

    <!-- 添加课程弹窗 -->
    <el-dialog
        v-model="showAddDialog"
        title="添加课程"
        width="500px"
        :before-close="handleCloseDialog"
    >
      <el-form
          :model="addForm"
          :rules="formRules"
          ref="addFormRef"
          label-width="80px"
      >
        <el-form-item label="课程名称" prop="name">
          <el-input
              v-model="addForm.name"
              placeholder="请输入课程名称"
              maxlength="50"
          />
        </el-form-item>

        <el-form-item label="教师姓名" prop="teacherName">
          <el-input
              v-model="addForm.teacherName"
              placeholder="请输入教师姓名"
              maxlength="20"
          />
        </el-form-item>

        <el-form-item label="课程描述" prop="description">
          <el-input
              v-model="addForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入课程描述"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="课程时长" prop="duration">
          <el-input
              v-model="addForm.duration"
              placeholder="例如：32课时、一学期等"
              maxlength="20"
          />
        </el-form-item>

        <el-form-item label="最大签到次数" prop="maxCheckInCount">
          <el-input-number
              v-model="addForm.maxCheckInCount"
              :min="1"
              :max="100"
              controls-position="right"
              class="w-100"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button
              type="primary"
              @click="handleAddCourse"
              :loading="adding"
          >
            确认添加
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑课程弹窗 -->
    <el-dialog
        v-model="showEditDialog"
        title="编辑课程"
        width="500px"
        :before-close="handleCloseEditDialog"
    >
      <el-form
          :model="editForm"
          :rules="formRules"
          ref="editFormRef"
          label-width="80px"
      >
        <el-form-item label="课程名称" prop="name">
          <el-input
              v-model="editForm.name"
              placeholder="请输入课程名称"
              maxlength="50"
          />
        </el-form-item>

        <el-form-item label="教师姓名" prop="teacherName">
          <el-input
              v-model="editForm.teacherName"
              placeholder="请输入教师姓名"
              maxlength="20"
          />
        </el-form-item>

        <el-form-item label="课程描述" prop="description">
          <el-input
              v-model="editForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入课程描述"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="课程时长" prop="duration">
          <el-input
              v-model="editForm.duration"
              placeholder="例如：32课时、一学期等"
              maxlength="20"
          />
        </el-form-item>

        <el-form-item label="学生数" prop="studentsCount">
          <el-input-number
              v-model="editForm.studentsCount"
              :min="0"
              :max="10000"
              controls-position="right"
              class="w-100"
          />
        </el-form-item>

        <el-form-item label="最大签到次数" prop="maxCheckInCount">
          <el-input-number
              v-model="editForm.maxCheckInCount"
              :min="1"
              :max="100"
              controls-position="right"
              class="w-100"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseEditDialog">取消</el-button>
          <el-button
              type="primary"
              @click="handleUpdateCourse"
              :loading="editing"
          >
            确认修改
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
const editing = ref(false)
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const courseList = ref([])

// 表单引用
const addFormRef = ref()
const editFormRef = ref()

// 表单数据
const addForm = reactive({
  name: '',
  teacherName: '',
  description: '',
  duration: '',
  maxCheckInCount: 12
})

const editForm = reactive({
  id: null,
  name: '',
  teacherName: '',
  description: '',
  duration: '',
  studentsCount: 0,
  maxCheckInCount: 12
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '课程名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  teacherName: [
    { required: true, message: '请输入教师姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '教师姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' },
    { min: 10, max: 200, message: '课程描述长度在 10 到 200 个字符', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入课程时长', trigger: 'blur' }
  ],
  studentsCount: [
    { required: true, message: '请输入学生数', trigger: 'blur' }
  ]
}

// 返回首页
const goToHome = () => {
  console.log('🏠 返回首页')
  router.push('/admin')
}

// 检查管理员登录状态
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

// 加载课程列表
const loadCourseList = () => {
  loading.value = true
  get('/api/admin/course/list',
      (message) => {
        console.log('获取课程列表成功:', message)
        if (message && message.data) {
          courseList.value = message.data
        } else if (Array.isArray(message)) {
          courseList.value = message
        } else {
          courseList.value = []
        }
        loading.value = false
      },
      (message) => {
        ElMessage.error(message || '加载课程列表失败')
        courseList.value = []
        loading.value = false
      }
  )
}

// 添加课程
const handleAddCourse = () => {
  if (!addFormRef.value) return
  addFormRef.value.validate((valid) => {
    if (valid) {
      adding.value = true

      console.log('🔄 开始添加课程:', {
        name: addForm.name,
        teacherName: addForm.teacherName,
        description: addForm.description,
        duration: addForm.duration,
        maxCheckInCount: addForm.maxCheckInCount
      })

      post('/api/admin/course/add', {
            name: addForm.name,
            teacherName: addForm.teacherName,
            description: addForm.description,
            duration: addForm.duration,
            maxCheckInCount: addForm.maxCheckInCount
          },
          (message) => {
            console.log('✅ 添加课程成功:', message)
            ElMessage.success(message)
            handleCloseDialog()
            loadCourseList()
            adding.value = false
          },
          (message) => {
            console.error('❌ 添加课程失败:', message)
            ElMessage.error(message || '添加失败')
            adding.value = false
          }
      )
    } else {
      console.log('❌ 表单验证失败')
    }
  })
}

// 编辑课程
const handleEdit = (course) => {
  editForm.id = course.id
  editForm.name = course.name
  editForm.teacherName = course.teacherName
  editForm.description = course.description
  editForm.duration = course.duration
  editForm.studentsCount = course.studentsCount || 0
  editForm.maxCheckInCount = course.maxCheckInCount
  showEditDialog.value = true
}

// 更新课程
const handleUpdateCourse = () => {
  if (!editFormRef.value) return
  editFormRef.value.validate((valid) => {
    if (valid) {
      editing.value = true

      console.log('🔄 开始更新课程:', {
        ...editForm
      })

      post('/api/admin/course/update', {
            id: editForm.id,
            name: editForm.name,
            teacherName: editForm.teacherName,
            description: editForm.description,
            duration: editForm.duration,
            studentsCount: editForm.studentsCount,
            maxCheckInCount: editForm.maxCheckInCount
          },
          (message) => {
            console.log('✅ 更新课程成功:', message)
            ElMessage.success(message)
            handleCloseEditDialog()
            loadCourseList()
            editing.value = false
          },
          (message) => {
            console.error('❌ 更新课程失败:', message)
            ElMessage.error(message || '更新失败')
            editing.value = false
          }
      )
    } else {
      console.log('❌ 表单验证失败')
    }
  })
}

// 删除课程
const handleDelete = (course) => {
  ElMessageBox.confirm(
      `确定要删除课程 "${course.name}" 吗？此操作将删除所有相关的报名记录，且不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    post('/api/admin/course/delete', {courseId: course.id},
        (message) => {
          ElMessage.success(message || '课程删除成功')
          loadCourseList()
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
    addForm.name = ''
    addForm.teacherName = ''
    addForm.description = ''
    addForm.duration = ''
    addForm.maxCheckInCount = 12
  }
}

const handleCloseEditDialog = () => {
  showEditDialog.value = false
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  } else {
    editForm.id = null
    editForm.name = ''
    editForm.teacherName = ''
    editForm.description = ''
    editForm.duration = ''
    editForm.studentsCount = 0
    editForm.maxCheckInCount = 12
  }
}

// 生命周期
onMounted(async () => {
  const isLoggedIn = await checkAdminLogin()
  if (!isLoggedIn) {
    ElMessage.warning('请先登录管理员账户')
    router.push('/admin-login')
    return
  }

  loadCourseList()
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
  display: flex;
  gap: 8px;
}

.enroll-btn,
.enrolled-btn {
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  outline: none;
  flex: 1;
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

.edit-btn {
  background: #1a1a1a;
  color: white;
}

.edit-btn:hover {
  background: #000000;
}

.delete-btn {
  background: #ffffff;
  color: #1a1a1a;
  border: 1px solid #e0e0e0;
}

.delete-btn:hover {
  background: #f5f5f7;
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

  .card-footer {
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

:deep(.el-input-number.w-100) {
  width: 100%;
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
