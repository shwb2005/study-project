<template>
  <Teleport to="body">
    <div class="delete-overlay" v-if="modelValue" @click.self="onCancel">
      <div class="delete-modal">
        <svg class="delete-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd"/>
        </svg>
        <h2 class="delete-title">确认删除？</h2>
        <p class="delete-message">{{ message }}</p>
        <div class="delete-actions">
          <button class="btn-cancel" @click="onCancel">取消</button>
          <button class="btn-confirm" @click="onConfirm">确认</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  modelValue: Boolean,
  message: {
    type: String,
    default: '确定要删除吗？此操作不可恢复。'
  }
})
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const onCancel = () => {
  emit('update:modelValue', false)
  emit('cancel')
}
const onConfirm = () => {
  emit('update:modelValue', false)
  emit('confirm')
}
</script>

<style scoped>
.delete-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-modal {
  width: 320px;
  background: #1f2937;
  border: 1px solid #374151;
  border-radius: 20px;
  padding: 28px 24px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4);
}

.delete-icon {
  width: 52px;
  height: 52px;
  color: #6b7280;
  margin-bottom: 16px;
}

.delete-title {
  font-size: 18px;
  font-weight: 700;
  color: #f3f4f6;
  margin: 0 0 10px;
}

.delete-message {
  font-size: 14px;
  color: #9ca3af;
  line-height: 1.6;
  margin: 0 0 24px;
  max-width: 260px;
}

.delete-actions {
  display: flex;
  gap: 12px;
  width: 100%;
}

.btn-cancel {
  flex: 1;
  padding: 10px 0;
  border-radius: 50px;
  border: 2px solid #4b5563;
  background: transparent;
  color: #9ca3af;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  border-color: #6b7280;
  color: #d1d5db;
  background: rgba(107, 114, 128, 0.1);
}

.btn-confirm {
  flex: 1;
  padding: 10px 0;
  border-radius: 50px;
  border: 2px solid #10b981;
  background: #10b981;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s ease;
}

.btn-confirm:hover {
  background: #059669;
  border-color: #059669;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.btn-confirm:active {
  transform: scale(0.97);
}
</style>
