<script setup>
import {reactive} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import { ref } from 'vue';
import {post} from "@/net/index.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const form = reactive({
  email:'',
  code:'',
  password:'',
  password_repeat:''
})

const active = ref(0)

const validatePassWord = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const rules = {
  email: [
    {required: true, message: '请输入邮箱', trigger: 'blur'},
    {type: 'email', message: '请输入合法的邮箱', trigger: ['blur', 'change']}
  ],
  code: [
    {required: true, message: '请输入验证码', trigger: 'blur'},
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, max: 16, message: '密码长度必须在6-16个字符之间', trigger: ['blur', 'change']}
  ],
  password_repeat: [
    {validator: validatePassWord, trigger: ['blur', 'change']}
  ]
}

const formRef = ref()
const isEmailValid = ref(true);
const coldTime = ref(0);
const onValidate = (prop, isValid) => {
  if (prop === 'email') {
    isEmailValid.value = !isValid; // 邮箱验证通过时设为 false（不禁用）
  }
}

const validateEmail = () => {
  post('api/auth/valid-reset-email', {
    email: form.email
  }, (message) => {
    ElMessage.success(message)
    coldTime.value = 60
    setInterval(() => coldTime.value--, 1000)
  })

}

const startReset = () => {
  formRef.value.validateField(['email', 'code'], (isValid) => {
    if (isValid) {
      post('/api/auth/start-reset', {
        email: form.email,
        code: form.code
      }, () => {
        active.value++
      }, (error) => {
        ElMessage.warning(error || "验证失败")
      })
    } else {
      ElMessage.warning("请正确填写电子邮件和验证码")
    }
  })
}

const doReset = () => {
  formRef.value.validate((isValid) => {
    if (isValid) {
      post('/api/auth/do-reset', {
        password: form.password
      }, (message) => {
        ElMessage.success(message)
        window.location.href = '/?from=reset';
      })
    } else {
      ElMessage.warning("请填写新密码")
    }
  })
}

// 返回主页
const goToHome = () => {
  router.push('/')
}

</script>

<template>
  <div class="reset-container">
    <div style="margin: 30px 20px;">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="验证电子邮件"/>
        <el-step title="重置密码"/>
      </el-steps>
    </div>

    <transition name="el-fade-in-linear" mode="out-in">
      <div style="text-align: center;margin:0 20px;height: 100%" v-if="active===0">
        <div style="margin-top: 80px">
          <div style="font-size: 25px;font-weight: bold">验证邮箱</div>
          <div style="font-size: 14px;color: gray">请输入需要重置密码的电子邮件</div>
        </div>
        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
            <el-form-item prop="email">
              <el-input v-model="form.email" type="text" placeholder="电子邮件地址">
                <template #prefix>
                  <el-icon>
                    <Message/>
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="code">
              <el-row gutter="10" style="width: 100%">
                <el-col :span="17">
                  <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码">
                    <template #prefix>
                      <el-icon>
                        <EditPen/>
                      </el-icon>
                    </template>
                  </el-input>
                </el-col>
                <el-col :span="5">
                  <el-button type="success" @click="validateEmail" :disabled="isEmailValid || coldTime>0">
                    {{ coldTime > 0 ? '请稍后' + coldTime + '秒' : '获取验证码' }}
                  </el-button>
                </el-col>
              </el-row>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-top: 70px">
          <el-button @click="startReset()" style="width: 270px;" type="danger" plain>重置密码</el-button>
        </div>

        <!-- 返回主页按钮 - 放在重置密码按钮下方 -->
        <div style="margin-top: 20px">
          <button class="back-button" @click="goToHome">
            <svg class="back-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M19 12H5M12 19l-7-7 7-7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>返回主页</span>
          </button>
        </div>
      </div>
    </transition>

    <transition name="el-fade-in-linear" mode="out-in">
      <div style="text-align: center;margin:0 20px;height: 100%" v-if="active===1">
        <div style="margin-top: 80px">
          <div style="font-size: 25px;font-weight: bold">重置密码</div>
          <div style="font-size: 14px;color: gray">请输入新密码</div>
        </div>
        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
            <el-form-item prop="password">
              <el-input v-model="form.password" :maxlength="16" type="password" placeholder="新密码">
                <template #prefix>
                  <el-icon>
                    <Lock/>
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password_repeat">
              <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="重复新密码">
                <template #prefix>
                  <el-icon>
                    <Lock/>
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-top: 70px">
          <el-button @click="doReset()" style="width: 270px;" type="danger" plain>立即重置</el-button>
        </div>

        <!-- 返回主页按钮 - 放在立即重置按钮下方 -->
        <div style="margin-top: 20px">
          <button class="back-button" @click="goToHome">
            <svg class="back-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M19 12H5M12 19l-7-7 7-7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>返回主页</span>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.reset-container {
  position: relative;
  min-height: 100vh;
}

/* 返回按钮样式 */
.back-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 270px;
  padding: 12px 20px;
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.back-button:hover {
  background: #f5f5f7;
  border-color: #d0d0d0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.back-button:active {
  transform: translateY(1px);
}

.back-icon {
  width: 18px;
  height: 18px;
  color: #6e6e73;
  transition: color 0.25s;
}

.back-button:hover .back-icon {
  color: #1a1a1a;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .back-button {
    width: 100%;
    max-width: 270px;
    padding: 10px 16px;
    font-size: 13px;
  }

  .back-icon {
    width: 16px;
    height: 16px;
  }
}
</style>