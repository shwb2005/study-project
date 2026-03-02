<script setup>
import {User,Lock} from '@element-plus/icons-vue'
import {ElMessage} from "element-plus";
import {reactive, ref, onMounted} from "vue";
import router from "@/router/index.js";
import {useStore} from "@/stores/index.js";
import { post } from "@/net/index.js";

const store = useStore()
const loading = ref(false)

const form = reactive({
  username:'',
  password:'',
  remember:false
})

// 组件挂载时检查是否有记住的管理员信息
onMounted(() => {
  const rememberedAdmin = localStorage.getItem('remembered_admin')
  if (rememberedAdmin) {
    try {
      const adminInfo = JSON.parse(rememberedAdmin)
      form.username = adminInfo.username || ''
      form.password = adminInfo.password || ''
      form.remember = true
    } catch (e) {
      console.error('解析记住的管理员信息失败:', e)
    }
  }
})

const login = () => {
  if(!form.username || !form.password){
    ElMessage.warning('请填写管理员用户名和密码！')
    return
  }

  loading.value = true
  console.log('开始管理员登录请求:', form.username)

  post('/api/admin/login', {
    username: form.username,
    password: form.password
  }, (message) => {
    // 登录成功
    ElMessage.success(message)

    // 创建管理员认证对象
    const adminAuth = {
      username: form.username,
      isAdmin: true,
      loginTime: new Date().getTime()
    }

    // 直接设置 store 状态
    store.auth.admin = adminAuth

    // 处理记住我功能
    if (form.remember) {
      // 保存管理员凭据到 localStorage
      try {
        const rememberedInfo = {
          username: form.username,
          password: form.password
        }
        localStorage.setItem('remembered_admin', JSON.stringify(rememberedInfo))
        console.log('管理员凭据已保存')
      } catch (error) {
        console.error('保存管理员凭据失败:', error)
      }
    } else {
      // 不记住时清除保存的凭据
      localStorage.removeItem('remembered_admin')
    }

    // 保存登录状态到 localStorage
    try {
      localStorage.setItem('admin_auth', JSON.stringify(adminAuth))
      console.log('管理员认证信息已保存')
    } catch (error) {
      console.error('保存到 localStorage 失败:', error)
    }

    console.log('Store设置并保存完成:', store.auth.admin)

    // 跳转到管理员页面
    router.push('/admin')

  }, (message) => {
    // 登录失败
    ElMessage.error('登录失败: ' + message)
    loading.value = false
  }, (error) => {
    // 请求错误
    console.error('请求失败:', error)
    ElMessage.error('请求失败，请检查网络连接')
    loading.value = false
  })
}
</script>

<template>
  <div style="text-align: center;margin:0 20px">
    <div style="margin-top: 150px">
      <div style="font-size: 25px;font-weight: bold">管理员登录</div>
      <div style="font-size: 14px;color: gray">进入管理员系统之前请先输入用户名密码进行登录</div>
    </div>
    <diV style="margin-top: 50px">
      <el-input v-model="form.username" type="text" placeholder="管理员用户名">
        <template #prefix>
          <el-icon><User /></el-icon>
        </template>
      </el-input>
      <el-input v-model="form.password" type="password" style="margin-top: 10px" placeholder="密码">
        <template #prefix>
          <el-icon><Lock /></el-icon>
        </template>
      </el-input>
    </div>
    <el-row style="margin-top: 5px">
      <el-col :span="12" style="text-align: left">
        <el-checkbox v-model="form.remember" label="记住我" size="large" />
      </el-col>
      <el-col :span="12" style="text-align: right;margin-top: 8.5px">
        <!-- 如果需要，可以添加其他链接 -->
      </el-col>
    </el-row>
    <div style="margin-top: 40px">
      <el-button @click="login()" style="width: 270px" type="success" plain :loading="loading">
        {{ loading ? '登录中...' : '立即登录' }}
      </el-button>
    </div>
    <el-divider>
      <span style="color: gray;font-size: 13px">返回普通用户</span>
    </el-divider>
    <div>
      <el-button style="width: 270px" @click="router.push('/')" type="warning" plain>返回用户登录</el-button>
    </div>
  </div>
</template>

<style scoped>

</style>