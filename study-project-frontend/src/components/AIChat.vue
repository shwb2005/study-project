<script setup>
import { ref, nextTick, computed } from 'vue'
import { ElMessage } from "element-plus"
import { post } from "@/net"

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

  post('/api/ai/chat', { message: messageToSend },
    (response) => {
      const responseData = typeof response === 'string' ? JSON.parse(response) : response
      const responseContent = responseData.response || responseData.message || response

      const hasMore = responseContent.includes('还有') && responseContent.includes('未显示')
      messages.value.push({
        role: 'assistant',
        content: responseContent,
        hasMore: hasMore,
        timestamp: formatTime(new Date())
      })
      loading.value = false
      scrollToBottom()
    },
    (errorMessage) => {
      ElMessage.error(errorMessage || '发送失败，请稍后重试')
      messages.value.push({
        role: 'assistant',
        content: '抱歉，我现在无法回答你的问题，请稍后再试。',
        timestamp: formatTime(new Date())
      })
      loading.value = false
      scrollToBottom()
    },
    (error) => {
      ElMessage.error('网络错误，请稍后重试')
      messages.value.push({
        role: 'assistant',
        content: '抱歉，网络连接出现问题，请稍后再试。',
        timestamp: formatTime(new Date())
      })
      loading.value = false
      scrollToBottom()
    }
  )
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
  <div class="apple-chat">
    <!-- Header -->
    <div class="chat-head">
      <div class="head-left">
        <div class="ai-orb">
          <div class="orb-core"></div>
          <div class="orb-ring"></div>
        </div>
        <div class="head-info">
          <span class="head-title">AI 助手</span>
          <span class="head-status" :class="{ thinking: loading }">
            <i class="status-dot"></i>
            {{ loading ? '正在思考' : '在线' }}
          </span>
        </div>
      </div>
      <button class="clear-btn" @click="clearChat" title="清空聊天">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 6h18"/>
          <path d="M8 6V4.5A1.5 1.5 0 019.5 3h5A1.5 1.5 0 0116 4.5V6"/>
          <path d="M19 6l-.7 12.5A2 2 0 0116.3 20H7.7a2 2 0 01-2-1.5L5 6"/>
          <path d="M10 11v5"/>
          <path d="M14 11v5"/>
        </svg>
      </button>
    </div>

    <!-- Messages -->
    <div class="chat-body" ref="chatContainer">
      <div
        v-for="(message, index) in messages"
        :key="index"
        class="msg"
        :class="message.role"
        @mouseenter="toggleActions(index)"
        @mouseleave="messageActions[index] = false"
      >
        <div class="msg-inner" :class="message.role">
          <!-- Avatar -->
          <div class="msg-avatar" :class="message.role">
            <template v-if="message.role === 'assistant'">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="3" fill="currentColor"/>
                <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2z" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
                <path d="M12 2c2.5 2 4 5.5 4 10s-1.5 8-4 10" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
                <path d="M12 2c-2.5 2-4 5.5-4 10s1.5 8 4 10" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
                <path d="M2 12h20" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
              </svg>
            </template>
            <template v-else>我</template>
          </div>

          <!-- Bubble -->
          <div class="msg-bubble-wrap">
            <div class="msg-bubble" :class="message.role">
              <p class="msg-text">{{ message.content }}</p>
            </div>
            <div v-if="message.hasMore" @click="showAll(index)" class="expand-btn">
              展开全部
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
                <path d="M3 4.5L6 7.5 9 4.5"/>
              </svg>
            </div>
            <div class="msg-meta">
              <span class="msg-time">{{ message.timestamp }}</span>
              <div v-if="messageActions[index]" class="msg-actions">
                <button @click.stop="copyMessage(message.content, index)" class="act-btn" title="复制">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                    <rect x="9" y="9" width="13" height="13" rx="2"/>
                    <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
                  </svg>
                </button>
                <button @click.stop="deleteMessage(index)" class="act-btn del" title="删除">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                    <path d="M3 6h18"/>
                    <path d="M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                    <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Typing indicator -->
      <div v-if="loading" class="msg assistant">
        <div class="msg-inner assistant">
          <div class="msg-avatar assistant">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="3" fill="currentColor"/>
              <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2z" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
              <path d="M12 2c2.5 2 4 5.5 4 10s-1.5 8-4 10" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
              <path d="M12 2c-2.5 2-4 5.5-4 10s1.5 8 4 10" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
              <path d="M2 12h20" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
            </svg>
          </div>
          <div class="msg-bubble-wrap">
            <div class="msg-bubble assistant typing-bubble">
              <div class="typing-dots">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="scroll-anchor"></div>
    </div>

    <!-- Input -->
    <div class="chat-input-area">
      <div class="input-shell">
        <textarea
          v-model="inputMessage"
          @keypress="handleKeyPress"
          placeholder="输入消息…"
          rows="1"
          :maxlength="maxLength"
          :disabled="loading"
        ></textarea>
        <button
          class="send-btn"
          @click="sendMessage"
          :disabled="loading || !inputMessage.trim()"
          :class="{ active: inputMessage.trim() && !loading }"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
            <path d="M12 5l7 7-7 7" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      <div class="input-foot">
        <span class="char-hint">{{ characterCount }} / {{ maxLength }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ═══════════════════════════════════════
   Apple-inspired Chat Design System
   ═══════════════════════════════════════ */

*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.apple-chat {
  --text-1: #1d1d1f;
  --text-2: #6e6e73;
  --text-3: #aeaeb2;
  --bg: #ffffff;
  --surface: #f5f5f7;
  --surface-2: #e8e8ed;
  --border: rgba(60, 60, 67, 0.08);
  --border-2: rgba(60, 60, 67, 0.12);
  --blue: #0071e3;
  --blue-hover: #0077ed;
  --red: #ff3b30;
  --user-bg: #0071e3;
  --user-text: #fff;
  --ai-bg: #f5f5f7;
  --ai-text: #1d1d1f;
  --radius: 20px;

  display: flex;
  flex-direction: column;
  height: 620px;
  background: var(--bg);
  border-radius: 22px;
  overflow: hidden;
  border: 0.5px solid var(--border);
  box-shadow:
    0 0 0 0.5px rgba(0,0,0,0.03),
    0 2px 8px rgba(0,0,0,0.04),
    0 12px 40px rgba(0,0,0,0.06);
  font-family: -apple-system, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', sans-serif;
  -webkit-font-smoothing: antialiased;
  position: relative;
}

/* ── Header ── */

.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(255,255,255,0.72);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 0.5px solid var(--border);
  position: relative;
  z-index: 2;
}

.head-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-orb {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(145deg, #1d1d1f, #3a3a3c);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}

.orb-core {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 0 10px rgba(102, 126, 234, 0.6);
  animation: orbPulse 3s ease-in-out infinite;
}

.orb-ring {
  position: absolute;
  inset: 2px;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,0.08);
}

@keyframes orbPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(0.85); opacity: 0.7; }
}

.head-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.head-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-1);
  letter-spacing: -0.02em;
}

.head-status {
  font-size: 11px;
  color: var(--text-3);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: color 0.3s;
}

.head-status.thinking {
  color: var(--blue);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #34c759;
  display: inline-block;
}

.head-status.thinking .status-dot {
  background: var(--blue);
  animation: dotBlink 1s ease-in-out infinite;
}

@keyframes dotBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.clear-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--text-3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: var(--surface);
  color: var(--red);
}

/* ── Message Area ── */

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 20px 8px;
  scroll-behavior: smooth;
}

.chat-body::-webkit-scrollbar { width: 4px; }
.chat-body::-webkit-scrollbar-track { background: transparent; }
.chat-body::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.1);
  border-radius: 2px;
}

.scroll-anchor { height: 1px; }

.msg {
  margin-bottom: 18px;
  animation: msgIn 0.35s cubic-bezier(0.23, 1, 0.32, 1);
}

@keyframes msgIn {
  from { opacity: 0; transform: translateY(8px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.msg-inner {
  display: flex;
  gap: 10px;
  max-width: 82%;
}

.msg-inner.user {
  flex-direction: row-reverse;
  margin-left: auto;
}

.msg-inner.assistant {
  margin-right: auto;
}

/* ── Avatar ── */

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  margin-top: 2px;
}

.msg-avatar.user {
  background: linear-gradient(135deg, #5856d6, #0071e3);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0,113,227,0.2);
}

.msg-avatar.assistant {
  background: linear-gradient(145deg, #1d1d1f, #3a3a3c);
  color: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

/* ── Bubble ── */

.msg-bubble-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.msg-bubble {
  padding: 10px 16px;
  border-radius: 18px;
  position: relative;
  transition: transform 0.15s ease;
}

.msg-bubble.user {
  background: var(--user-bg);
  color: var(--user-text);
  border-bottom-right-radius: 6px;
}

.msg-bubble.assistant {
  background: var(--ai-bg);
  color: var(--ai-text);
  border-bottom-left-radius: 6px;
}

.msg-bubble:hover {
  transform: scale(1.005);
}

.msg-text {
  font-size: 15px;
  line-height: 1.55;
  letter-spacing: -0.01em;
  word-break: break-word;
  margin: 0;
}

/* ── Expand ── */

.expand-btn {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 5px 12px;
  background: var(--surface);
  color: var(--blue);
  border: none;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  align-self: flex-start;
  font-family: inherit;
}

.expand-btn:hover {
  background: var(--blue);
  color: #fff;
}

/* ── Meta ── */

.msg-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 4px;
  opacity: 0;
  transform: translateY(-2px);
  transition: all 0.2s ease;
  pointer-events: none;
}

.msg:hover .msg-meta {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.msg-time {
  font-size: 11px;
  color: var(--text-3);
  font-weight: 400;
}

.msg-actions {
  display: flex;
  gap: 4px;
}

.act-btn {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--text-3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}

.act-btn:hover {
  background: var(--surface);
  color: var(--text-1);
}

.act-btn.del:hover {
  background: rgba(255,59,48,0.08);
  color: var(--red);
}

/* ── Typing ── */

.typing-bubble {
  padding: 14px 18px !important;
}

.typing-dots {
  display: flex;
  gap: 5px;
  align-items: center;
}

.typing-dots span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--text-3);
  animation: typeDot 1.4s ease-in-out infinite;
}

.typing-dots span:nth-child(2) { animation-delay: 0.15s; }
.typing-dots span:nth-child(3) { animation-delay: 0.3s; }

@keyframes typeDot {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.3; }
  30% { transform: translateY(-5px); opacity: 1; }
}

/* ── Input ── */

.chat-input-area {
  padding: 12px 20px 14px;
  background: rgba(255,255,255,0.72);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  backdrop-filter: saturate(180%) blur(20px);
  border-top: 0.5px solid var(--border);
  position: relative;
  z-index: 2;
}

.input-shell {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  background: var(--surface);
  border-radius: 22px;
  padding: 6px 6px 6px 18px;
  border: 1px solid var(--border);
  transition: all 0.2s ease;
}

.input-shell:focus-within {
  border-color: var(--blue);
  box-shadow: 0 0 0 3px rgba(0,113,227,0.08);
  background: #fff;
}

.input-shell textarea {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  font-family: inherit;
  line-height: 1.5;
  color: var(--text-1);
  resize: none;
  padding: 8px 0;
  max-height: 100px;
  overflow-y: auto;
}

.input-shell textarea::placeholder {
  color: var(--text-3);
}

.input-shell textarea::-webkit-scrollbar { width: 3px; }
.input-shell textarea::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.08);
  border-radius: 2px;
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: var(--surface-2);
  color: var(--text-3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.25s cubic-bezier(0.23, 1, 0.32, 1);
}

.send-btn.active {
  background: var(--blue);
  color: #fff;
  box-shadow: 0 2px 10px rgba(0,113,227,0.25);
}

.send-btn.active:hover {
  background: var(--blue-hover);
  transform: scale(1.06);
}

.send-btn:disabled {
  cursor: not-allowed;
}

.input-foot {
  display: flex;
  justify-content: flex-end;
  padding: 6px 6px 0;
}

.char-hint {
  font-size: 11px;
  color: var(--text-3);
  font-weight: 400;
  letter-spacing: 0.01em;
}

/* ── Responsive ── */

@media (max-width: 768px) {
  .apple-chat { border-radius: 16px; height: 100vh; }
  .chat-body { padding: 16px 14px 8px; }
  .msg-inner { max-width: 88%; }
  .chat-input-area { padding: 10px 14px 12px; }
}
</style>
