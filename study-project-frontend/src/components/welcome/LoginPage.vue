<script setup>
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from "element-plus";
import { get, post } from "@/net/index.js";
import { reactive, onMounted } from "vue";
import router from "@/router/index.js";
import { useStore } from "@/stores/index.js";

const store = useStore()

const form = reactive({
  username: '',
  password: '',
  remember: false
})

// 页面加载时检查是否有保存的登录信息
onMounted(() => {
  const savedLoginInfo = localStorage.getItem('saved_login_info')
  if (savedLoginInfo) {
    try {
      const loginInfo = JSON.parse(savedLoginInfo)
      form.username = loginInfo.username || ''
      form.password = loginInfo.password || ''
      form.remember = true
    } catch (e) {
      console.error('解析保存的登录信息失败:', e)
      // 清除无效的保存信息
      localStorage.removeItem('saved_login_info')
    }
  }
})

const login = () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码！')
    return
  }

  post('api/auth/login', {
    username: form.username,
    password: form.password,
    remember: form.remember
  }, (message) => {
    ElMessage.success(message)

    // 处理记住我功能
    if (form.remember) {
      // 保存登录信息到本地存储
      const loginInfo = {
        username: form.username,
        password: form.password,
        timestamp: new Date().getTime()
      }
      localStorage.setItem('saved_login_info', JSON.stringify(loginInfo))
    } else {
      // 不记住时清除保存的信息
      localStorage.removeItem('saved_login_info')
    }

    // 获取用户信息并跳转
    get('/api/user/me', (message) => {
      store.auth.user = message
      router.push('/index')
    }, () => {
      store.auth.user = null
    })
  }, (error) => {
    // 修复:添加错误提示
    console.error('登录失败:', error)
    ElMessage.error(error || '登录失败,请检查用户名和密码')
    // 登录失败时,如果用户勾选了记住我,不清除已保存的信息
    // 这样可以避免用户输错密码时丢失已保存的信息
  })
}

// 清除保存的登录信息
const clearSavedInfo = () => {
  localStorage.removeItem('saved_login_info')
  form.username = ''
  form.password = ''
  form.remember = false
  ElMessage.success('已清除保存的登录信息')
}
</script>

<template>
  <div style="text-align: center;margin:0 20px">
    <div style="margin-top: 150px">
      <div style="font-size: 25px;font-weight: bold">登录</div>
      <div style="font-size: 14px;color: gray">进入系统之前请先输入用户名密码进行登录</div>
    </div>
    <div style="margin-top: 50px">
      <el-input v-model="form.username" type="text" placeholder="用户名/邮箱">
        <template #prefix>
          <el-icon>
            <User/>
          </el-icon>
        </template>
      </el-input>
      <el-input v-model="form.password" type="password" style="margin-top: 10px" placeholder="密码" show-password>
        <template #prefix>
          <el-icon>
            <Lock/>
          </el-icon>
        </template>
      </el-input>
    </div>
    <el-row style="margin-top: 5px">
      <el-col :span="12" style="text-align: left">
        <el-checkbox v-model="form.remember" label="记住我" size="large"/>
      </el-col>
      <el-col :span="12" style="text-align: right;margin-top: 8.5px">
        <el-link @click="router.push('/forget')">忘记密码？</el-link>
      </el-col>
    </el-row>


    <div style="margin-top: 40px">
      <el-button @click="login()" style="width: 270px" type="success" plain>立即登录</el-button>
    </div>
    <el-divider>
      <span style="color: gray;font-size: 13px">没有账号</span>
    </el-divider>
    <div>
      <el-button style="width: 270px" @click="router.push('/register')" type="warning" plain>注册账号</el-button>
    </div>
    <!-- 在 WelcomeView.vue 的适当位置添加，比如在登录框下面 -->
    <div style="margin-top: 20px">
      <el-button
          @click="router.push('/admin-login')"
          style="width: 270px"
          type="info"
          plain
      >
        进入管理员系统
      </el-button>
    </div>
  </div>
</template>

<style scoped>
/* 可以添加一些样式来美化记住我功能的显示 */
.el-text {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.el-link {
  vertical-align: baseline;
}
</style>