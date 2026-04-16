import { ref } from 'vue'
import { defineStore } from 'pinia'
import { reactive } from "vue";

export const useStore = defineStore('store', () => {
  const auth = reactive({
    user: null,
    admin: null
  })

  const initialized = ref(false)

  const loginPassword = reactive({
    isTyping: false,
    passwordLength: 0,
    showPassword: false
  })

  const resetLoginPassword = () => {
    loginPassword.isTyping = false
    loginPassword.passwordLength = 0
    loginPassword.showPassword = false
  }

  return { auth, initialized, loginPassword, resetLoginPassword }
})