import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { reactive } from "vue";

export const useStore = defineStore('store', () => {
  const auth = reactive({
    user: null,
    admin: null  // 添加 admin 字段
  })

  return { auth }
})