<template>
  <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
    <div style="flex: 1;">
      <el-image style="width: 100%;height: 100%;" fit="cover"
                src="https://ts1.tc.mm.bing.net/th/id/R-C.148101c205857241d1666d8d55d09eda?rik=emNVnf9uHL79Fg&riu=http%3a%2f%2fclubimg.club.vmall.com%2fdata%2fattachment%2fforum%2f202004%2f21%2f232808a58jxcywkezumtpe.jpg&ehk=lioHPAdzEqJwA%2bFZznIyKDWb6mew5xjFF2JeGHyicvY%3d&risl=&pid=ImgRaw&r=0"/>
    </div>
    <div class="welcome-title">
      <div style="font-size: 30px;font-weight: bold">欢迎来到学习平台</div>


    </div>
    <div style="width: 400px;background-color: white;z-index: 1">
      <router-view v-slot="{ Component}">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" style="height: 100%"/>
        </transition>
      </router-view>

    </div>
  </div>
</template>

<script setup>

</script>
<style scoped>
.welcome-title{
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}

</style>
