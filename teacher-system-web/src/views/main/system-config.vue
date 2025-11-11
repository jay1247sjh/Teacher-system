<template>
  <section class="system-config-page">
    <div class="page-header">
      <h2>系统配置</h2>
      <p class="subtitle">管理系统的监控和配置信息</p>
    </div>

    <!-- 当配置为空时显示提示面板 -->
    <ConfigTipPanel
      v-if="!hasPrometheusConfig"
      title="监控配置未设置"
      message="系统检测到监控相关配置尚未设置。配置监控后，您可以实时查看系统运行状态和性能指标。"
      :details="[
        '配置 Prometheus URL 后，可以查询系统指标数据',
        '配置 Grafana URL 后，可以查看可视化监控面板',
        '建议联系系统管理员在环境变量中统一配置',
        '配置保存后将在当前浏览器中生效'
      ]"
      :show-action="true"
      action-text="立即配置"
      :show-cancel="false"
      @action="openConfigDialog"
    />

    <!-- 配置对话框 -->
    <div v-if="showConfigDialog" class="dialog-overlay" @click.self="showConfigDialog = false">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>配置监控地址</h3>
          <button class="dialog-close" @click="showConfigDialog = false">×</button>
        </div>
        
        <div class="dialog-body">
          <div class="form-group">
            <label class="form-label">Prometheus URL</label>
            <input
              v-model="configForm.prometheusUrl"
              type="text"
              class="form-input"
              placeholder="例如: 192.168.91.128:9090"
            />
            <p class="form-hint">用于查询系统指标数据的 Prometheus 服务地址</p>
          </div>

          <div class="form-group">
            <label class="form-label">Grafana URL</label>
            <input
              v-model="configForm.grafanaUrl"
              type="text"
              class="form-input"
              placeholder="例如: http://localhost:3000"
            />
            <p class="form-hint">用于查看可视化监控面板的 Grafana 服务地址</p>
          </div>

          <div class="info-box">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="info-icon">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
            <div class="info-content">
              <p class="info-title">提示</p>
              <p class="info-text">配置保存后将在当前浏览器中生效</p>
              <p class="info-text">建议联系系统管理员在环境变量中统一配置</p>
            </div>
          </div>
        </div>

        <div class="dialog-footer">
          <button class="btn-cancel" @click="showConfigDialog = false">取消</button>
          <button class="btn-confirm" @click="saveConfig">保存</button>
        </div>
      </div>
    </div>

    <!-- 配置已设置时显示的内容 -->
    <div v-else class="config-list">
      <div class="config-card">
        <div class="card-header">
          <h3>监控配置</h3>
          <div class="header-actions">
            <button class="btn-clear" @click="confirmClearConfig">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="icon">
                <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
              </svg>
              清空配置
            </button>
            <button class="btn-edit" @click="openConfigDialog">编辑</button>
          </div>
        </div>
        <div class="card-body">
          <div class="config-item">
            <span class="config-label">Prometheus URL:</span>
            <span class="config-value">{{ configForm.prometheusUrl || '未设置' }}</span>
          </div>
          <div class="config-item">
            <span class="config-label">Grafana URL:</span>
            <span class="config-value">{{ configForm.grafanaUrl || '未设置' }}</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import ConfigTipPanel from '@/components/ConfigTipPanel.vue'

export default defineComponent({
  name: 'SystemConfig',
  components: {
    ConfigTipPanel
  },
  data() {
    return {
      showConfigDialog: false,
      configForm: {
        prometheusUrl: '',
        grafanaUrl: ''
      }
    }
  },
  computed: {
    hasPrometheusConfig(): boolean {
      return !!(this.configForm.prometheusUrl || this.configForm.grafanaUrl)
    }
  },
  mounted() {
    this.loadConfig()
  },
  methods: {
    openConfigDialog() {
      // 打开对话框前重新加载配置，确保显示最新数据
      this.loadConfig()
      this.showConfigDialog = true
    },
    
    loadConfig() {
      // 从 localStorage 加载配置（使用与 data-monitoring.vue 相同的键名）
      const prometheusUrl = localStorage.getItem('monitoring_prometheus_url') || ''
      const grafanaUrl = localStorage.getItem('monitoring_grafana_url') || ''
      
      this.configForm.prometheusUrl = prometheusUrl
      this.configForm.grafanaUrl = grafanaUrl
    },
    
    saveConfig() {
      // 保存配置到 localStorage（使用与 data-monitoring.vue 相同的键名）
      const prometheusUrl = this.configForm.prometheusUrl.trim()
      const grafanaUrl = this.configForm.grafanaUrl.trim()
      
      // 如果两个 URL 都为空，删除配置
      if (!prometheusUrl && !grafanaUrl) {
        localStorage.removeItem('monitoring_prometheus_url')
        localStorage.removeItem('monitoring_grafana_url')
        // 清空表单数据
        this.configForm.prometheusUrl = ''
        this.configForm.grafanaUrl = ''
        ElMessage.success('配置已清空')
      } else {
        // 保存或更新配置
        if (prometheusUrl) {
          localStorage.setItem('monitoring_prometheus_url', prometheusUrl)
        } else {
          localStorage.removeItem('monitoring_prometheus_url')
        }
        
        if (grafanaUrl) {
          localStorage.setItem('monitoring_grafana_url', grafanaUrl)
        } else {
          localStorage.removeItem('monitoring_grafana_url')
        }
        
        // 更新表单数据为 trim 后的值
        this.configForm.prometheusUrl = prometheusUrl
        this.configForm.grafanaUrl = grafanaUrl
        ElMessage.success('配置保存成功')
      }
      
      this.showConfigDialog = false
    },
    
    // 确认清空配置
    confirmClearConfig() {
      ElMessageBox.confirm(
        '确定要清空所有监控配置吗？清空后将无法查看监控数据。',
        '清空配置确认',
        {
          confirmButtonText: '确定清空',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: 'confirm-clear-btn'
        }
      ).then(() => {
        this.clearConfig()
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 清空配置
    clearConfig() {
      // 删除 localStorage 中的监控配置
      localStorage.removeItem('monitoring_prometheus_url')
      localStorage.removeItem('monitoring_grafana_url')
      
      // 清空表单数据
      this.configForm.prometheusUrl = ''
      this.configForm.grafanaUrl = ''
      
      ElMessage.success('配置已清空')
    }
  }
})
</script>

<style scoped lang="scss">
.system-config-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
  
  h2 {
    margin: 0 0 8px 0;
    font-size: 28px;
    font-weight: 600;
    color: #1f2937;
  }
  
  .subtitle {
    margin: 0;
    font-size: 14px;
    color: #6b7280;
  }
}

.config-list {
  display: grid;
  gap: 20px;
}

.config-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid #e5e7eb;
    background: #f9fafb;
    
    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .card-body {
    padding: 24px;
  }
}

.config-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  
  &:last-child {
    border-bottom: none;
  }
  
  .config-label {
    flex: 0 0 180px;
    font-weight: 500;
    color: #4b5563;
  }
  
  .config-value {
    flex: 1;
    color: #1f2937;
    word-break: break-all;
  }
}

.btn-edit,
.btn-clear {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
  
  .icon {
    width: 16px;
    height: 16px;
  }
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.btn-edit {
  background: white;
  color: #3b82f6;
  border: 1px solid #3b82f6;
  
  &:hover {
    background: #3b82f6;
    color: white;
  }
}

.btn-clear {
  background: white;
  color: #ef4444;
  border: 1px solid #ef4444;
  
  &:hover {
    background: #ef4444;
    color: white;
  }
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-container {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  
  h3 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
  }
  
  .dialog-close {
    width: 32px;
    height: 32px;
    border: none;
    background: transparent;
    font-size: 24px;
    color: #6b7280;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.2s;
    
    &:hover {
      background: #f3f4f6;
      color: #1f2937;
    }
  }
}

.dialog-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 24px;
  
  .form-label {
    display: block;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #374151;
  }
  
  .form-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 14px;
    transition: all 0.2s;
    
    &:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }
  }
  
  .form-hint {
    margin: 6px 0 0 0;
    font-size: 12px;
    color: #6b7280;
  }
}

.info-box {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  margin-top: 20px;
  
  .info-icon {
    flex-shrink: 0;
    width: 20px;
    height: 20px;
    color: #3b82f6;
  }
  
  .info-content {
    flex: 1;
    
    .info-title {
      margin: 0 0 8px 0;
      font-size: 14px;
      font-weight: 600;
      color: #1e40af;
    }
    
    .info-text {
      margin: 4px 0;
      font-size: 13px;
      color: #1e40af;
      line-height: 1.5;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}

.btn-cancel,
.btn-confirm {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.btn-cancel {
  background: white;
  color: #6b7280;
  border: 1px solid #d1d5db;
  
  &:hover {
    background: #f9fafb;
    border-color: #9ca3af;
  }
}

.btn-confirm {
  background: #3b82f6;
  color: white;
  
  &:hover {
    background: #2563eb;
  }
}
</style>

