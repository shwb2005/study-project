<script setup>
import { ref, nextTick, computed } from 'vue'
import { ElMessage } from "element-plus"
import axios from "axios"

const messages = ref([
  {
    role: 'assistant',
    content: '你好！我是AI助手，有什么我可以帮助你的吗？',
    hasMore: false,
    timestamp: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
])
const inputMessage = ref('')
const loading = ref(false)
const chatContainer = ref(null)
const messageActions = ref({})

const maxLength = 1000

const characterCount = computed(() => inputMessage.value.length)

const formatTime = (date) => {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTo({
        top: chatContainer.value.scrollHeight,
        behavior: 'smooth'
      })
    }
  })
}

const sendMessage = async (customMessage = null) => {
  const messageToSend = customMessage || inputMessage.value.trim()

  if (!messageToSend || loading.value) return

  if (!customMessage) {
    messages.value.push({
      role: 'user',
      content: messageToSend,
      timestamp: formatTime(new Date())
    })
    inputMessage.value = ''
  }

  loading.value = true
  scrollToBottom()

  try {
    const formData = new URLSearchParams()
    formData.append('message', messageToSend)
    const res = await axios.post('/api/ai/chat', formData, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      withCredentials: true,
      timeout: 120000
    })
    const data = res.data
    if (data.success) {
      const responseContent = String(data.message).replace(/\n/g, '<br>')
      const hasMore = responseContent.includes('还有') && responseContent.includes('未显示')
      messages.value.push({
        role: 'assistant',
        content: responseContent,
        hasMore: hasMore,
        timestamp: formatTime(new Date())
      })
    } else {
      ElMessage.error(data.message || '发送失败')
      messages.value.push({
        role: 'assistant',
        content: '抱歉，我现在无法回答你的问题，请稍后再试。',
        timestamp: formatTime(new Date())
      })
    }
  } catch (error) {
    ElMessage.error(error.message || '网络错误，请稍后重试')
    messages.value.push({
      role: 'assistant',
      content: '抱歉，网络连接出现问题，请稍后再试。',
      timestamp: formatTime(new Date())
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const handleKeyPress = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const showAll = (index) => {
  if (messages.value[index] && messages.value[index].role === 'assistant') {
    messages.value[index].hasMore = false
    ElMessage.success('已显示完整内容')
  }
}

const copyMessage = async (content, index) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
    messageActions.value[index] = false
  } catch (err) {
    ElMessage.error('复制失败，请手动选择文本复制')
  }
}

const deleteMessage = (index) => {
  messages.value.splice(index, 1)
  messageActions.value[index] = false
  ElMessage.success('消息已删除')
}

const clearChat = () => {
  messages.value = [
    {
      role: 'assistant',
      content: '聊天记录已清空，有什么我可以帮助你的吗？',
      hasMore: false,
      timestamp: formatTime(new Date())
    }
  ]
  ElMessage.success('聊天记录已清空')
}

const toggleActions = (index) => {
  messageActions.value[index] = !messageActions.value[index]
}
</script>

<template>
  <div class="card-container">
    <!-- Header -->
    <div class="card-header">
      <div class="img-avatar">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="3" fill="#fff"/>
          <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2z" stroke="#fff" stroke-width="1.5" opacity="0.4"/>
          <path d="M12 2c2.5 2 4 5.5 4 10s-1.5 8-4 10" stroke="#fff" stroke-width="1.5" opacity="0.4"/>
          <path d="M12 2c-2.5 2-4 5.5-4 10s1.5 8 4 10" stroke="#fff" stroke-width="1.5" opacity="0.4"/>
        </svg>
      </div>
      <div class="header-text">
        <span class="text-chat">AI 助手</span>
        <span class="status-text" :class="{ thinking: loading }">
          {{ loading ? '正在思考...' : '在线' }}
        </span>
      </div>
      <button class="clear-btn" @click="clearChat" title="清空聊天">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 6h18"/>
          <path d="M8 6V4.5A1.5 1.5 0 019.5 3h5A1.5 1.5 0 0116 4.5V6"/>
          <path d="M19 6l-.7 12.5A2 2 0 0116.3 20H7.7a2 2 0 01-2-1.5L5 6"/>
        </svg>
      </button>
    </div>

    <!-- Messages -->
    <div class="card-body" ref="chatContainer">
      <div class="messages-container">
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="message-box"
          :class="message.role === 'assistant' ? 'left' : 'right'"
          @mouseenter="toggleActions(index)"
          @mouseleave="messageActions[index] = false"
        >
          <p class="message-text" v-html="message.content"></p>
          <div v-if="message.hasMore" @click="showAll(index)" class="expand-btn">
            展开全部
          </div>
          <div class="message-footer">
            <span class="message-time">{{ message.timestamp }}</span>
            <div v-if="messageActions[index]" class="message-actions">
              <button @click.stop="copyMessage(message.content, index)" class="action-btn" title="复制">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="9" y="9" width="13" height="13" rx="2"/>
                  <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
                </svg>
              </button>
              <button @click.stop="deleteMessage(index)" class="action-btn del" title="删除">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 6h18"/>
                  <path d="M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                  <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Typing indicator -->
        <div v-if="loading" class="message-box left typing">
          <div class="typing-dots">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- Input -->
    <div class="message-input">
      <div class="input-wrapper">
        <textarea
          v-model="inputMessage"
          @keypress="handleKeyPress"
          class="message-send"
          placeholder="输入消息..."
          rows="2"
          :maxlength="maxLength"
          :disabled="loading"
        ></textarea>
        <span class="char-count">{{ characterCount }} / {{ maxLength }}</span>
      </div>
      <button
        class="button-send"
        @click="sendMessage"
        :disabled="loading || !inputMessage.trim()"
        :class="{ active: inputMessage.trim() && !loading }"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          <path d="M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600&family=JetBrains+Mono:wght@400&display=swap');

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.card-container {
  background-color: #fff;
  border-radius: 16px;
  padding: 20px;
  margin: 20px;
  display: flex;
  flex-direction: column;
  width: 340px;
  max-height: 520px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.04);
  font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Header */
.card-header {
  display: flex;
  align-items: center;
  padding-bottom: 14px;
  border-bottom: 1px solid #e8e8e8;
  gap: 12px;
}

.img-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(145deg, #2d2d2d, #4a4a4a);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.header-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.text-chat {
  color: #1a1a1a;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: -0.3px;
}

.status-text {
  font-size: 11px;
  color: #8e8e93;
  font-weight: 500;
}

.status-text.thinking {
  color: #007aff;
}

.clear-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #aeaeb2;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: #f5f5f7;
  color: #ff3b30;
}

/* Messages */
.card-body {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.messages-container {
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.messages-container::-webkit-scrollbar {
  width: 4px;
}

.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 2px;
}

.message-box {
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  position: relative;
  animation: msgIn 0.3s ease;
}

@keyframes msgIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-box.left {
  background-color: #f5f5f7;
  color: #1a1a1a;
  align-self: flex-start;
  border-bottom-left-radius: 4px;
}

.message-box.right {
  background-color: #2d2d2d;
  color: #fff;
  align-self: flex-end;
  border-bottom-right-radius: 4px;
}

.message-box.typing {
  padding: 14px 18px;
}

.message-text {
  margin: 0;
  word-break: break-word;
}

.expand-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 4px 10px;
  background: #e8e8ed;
  color: #007aff;
  border: none;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.expand-btn:hover {
  background: #007aff;
  color: #fff;
}

.message-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
  min-height: 20px;
}

.message-time {
  font-size: 10px;
  color: #aeaeb2;
}

.message-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: #8e8e93;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.06);
  color: #1a1a1a;
}

.action-btn.del:hover {
  background: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

/* Typing dots */
.typing-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #8e8e93;
  animation: typingDot 1.4s ease-in-out infinite;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typingDot {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

/* Input */
.message-input {
  display: flex;
  gap: 10px;
  padding-top: 14px;
  border-top: 1px solid #e8e8e8;
  align-items: flex-end;
}

.input-wrapper {
  flex: 1;
  position: relative;
}

.message-send {
  width: 100%;
  padding: 12px 14px;
  padding-bottom: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  font-size: 14px;
  font-family: inherit;
  color: #1a1a1a;
  background: #fafafa;
  outline: none;
  resize: none;
  transition: all 0.2s ease;
}

.char-count {
  position: absolute;
  bottom: 8px;
  right: 12px;
  font-size: 10px;
  color: #aeaeb2;
  pointer-events: none;
}

.message-send::placeholder {
  color: #aeaeb2;
}

.message-send:focus {
  border-color: #007aff;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

.button-send {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  border: none;
  background: #e8e8ed;
  color: #8e8e93;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.25s ease;
}

.button-send.active {
  background: #007aff;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
}

.button-send.active:hover {
  background: #0066d6;
  transform: scale(1.05);
}

.button-send:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

/* Responsive */
@media (max-width: 400px) {
  .card-container {
    width: 100%;
    margin: 10px 0;
    border-radius: 12px;
  }
}
</style>
