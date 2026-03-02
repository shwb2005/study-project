<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AllCourses from '@/components/course/AllCourses.vue'
import MyCourses from '@/components/course/MyCourses.vue'

const router = useRouter()
const activeTab = ref('all')
</script>

<template>
  <div class="course-page">
    <div class="header">
      <div class="header-left">
        <el-button @click="router.push('/index')" type="text" icon="el-icon-back">
          返回首页
        </el-button>
        <h1>课程中心</h1>
      </div>
      <div class="header-right">
        <el-button @click="router.push('/profile')" type="primary" plain>
          个人中心
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="tabs">
      <el-tab-pane label="所有课程" name="all">
        <AllCourses />
      </el-tab-pane>

      <el-tab-pane label="我的课程" name="my">
        <MyCourses />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.course-page {
  padding: 20px;
  max-width: 1200px;
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
}

.tabs {
  background: white;
  padding: 20px;
  border-radius: 8px;
}
</style>