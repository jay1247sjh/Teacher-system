<template>
  <div class="config-tip-panel">
    <div class="tip-icon">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
      </svg>
    </div>
    
    <div class="tip-content">
      <h3 class="tip-title">{{ title }}</h3>
      <p class="tip-message">{{ message }}</p>
      
      <div v-if="details && details.length > 0" class="tip-details">
        <ul>
          <li v-for="(detail, index) in details" :key="index">{{ detail }}</li>
        </ul>
      </div>
      
      <div v-if="showAction" class="tip-actions">
        <button class="btn-primary" @click="handleAction">
          {{ actionText }}
        </button>
        <button v-if="showCancel" class="btn-secondary" @click="handleCancel">
          取消
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, type PropType} from 'vue'

export default defineComponent({
  name: 'ConfigTipPanel',
  props: {
    title: {
      type: String,
      default: '提示'
    },
    message: {
      type: String,
      required: true
    },
    details: {
      type: Array as PropType<string[]>,
      default: () => []
    },
    showAction: {
      type: Boolean,
      default: false
    },
    actionText: {
      type: String,
      default: '确定'
    },
    showCancel: {
      type: Boolean,
      default: false
    }
  },
  emits: ['action', 'cancel'],
  methods: {
    handleAction() {
      this.$emit('action')
    },
    handleCancel() {
      this.$emit('cancel')
    }
  }
})
</script>

<style scoped lang="scss">
.config-tip-panel {
  display: flex;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border-radius: 12px;
  border: 1px solid #d0d7de;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin: 20px 0;
}

.tip-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3b82f6;
  border-radius: 50%;
  color: white;
  
  svg {
    width: 28px;
    height: 28px;
  }
}

.tip-content {
  flex: 1;
}

.tip-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.tip-message {
  margin: 0 0 16px 0;
  font-size: 14px;
  line-height: 1.6;
  color: #4b5563;
}

.tip-details {
  margin: 16px 0;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  
  ul {
    margin: 0;
    padding-left: 20px;
    
    li {
      margin: 8px 0;
      font-size: 14px;
      color: #6b7280;
      line-height: 1.5;
      
      &::marker {
        color: #3b82f6;
      }
    }
  }
}

.tip-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-primary,
.btn-secondary {
  padding: 10px 24px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  outline: none;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.btn-primary {
  background: #3b82f6;
  color: white;
  
  &:hover {
    background: #2563eb;
  }
}

.btn-secondary {
  background: white;
  color: #6b7280;
  border: 1px solid #d1d5db;
  
  &:hover {
    background: #f9fafb;
    border-color: #9ca3af;
  }
}
</style>

