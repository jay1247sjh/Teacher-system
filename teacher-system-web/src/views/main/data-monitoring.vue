<template>
  <div class="data-monitoring">
    <!-- Prometheus/Grafana 嵌入区域 -->
    <div v-if="!prometheusUrl" class="config-prompt">
      <el-empty description="未配置监控服务">
        <template #image>
          <div class="empty-icon">📈</div>
        </template>
        <p class="config-hint">请在环境变量中配置 VITE_PROMETHEUS_URL 或 VITE_GRAFANA_URL</p>
        <el-button type="primary" @click="showConfigDialog = true">配置监控地址</el-button>
      </el-empty>
    </div>

    <div v-else class="iframe-container">
      <!-- 浮动工具栏 -->
      <div class="floating-toolbar">
        <el-tooltip content="快捷查询" placement="left">
          <el-button circle :icon="TrendCharts" @click="showQuickQuery = true" />
        </el-tooltip>
        <el-tooltip content="重新配置" placement="left">
          <el-button circle :icon="Setting" @click="showConfigDialog = true" />
        </el-tooltip>
        <el-tooltip content="刷新" placement="left">
          <el-button circle :icon="Refresh" @click="retryConnection" />
        </el-tooltip>
      </div>
      
      <iframe
        :src="prometheusUrl"
        class="monitoring-iframe"
        frameborder="0"
        @load="handleIframeLoad"
        @error="handleIframeError"
      ></iframe>
      <div v-if="loading" class="iframe-loading">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <p>加载监控服务中...</p>
      </div>
      
      <!-- 连接失败提示 -->
      <div v-if="connectionError" class="connection-error">
        <el-result
          icon="error"
          title="无法连接到监控服务"
          :sub-title="`无法访问 ${prometheusUrl}，请检查服务是否正常运行或重新配置地址`"
        >
          <template #icon>
            <div class="error-icon">🔌</div>
          </template>
          <template #extra>
            <div class="error-actions">
              <el-button type="primary" @click="retryConnection">
                <span class="btn-icon">🔄</span>
                重试连接
              </el-button>
              <el-button @click="showConfigDialog = true">
                <span class="btn-icon">⚙️</span>
                重新配置
              </el-button>
            </div>
            <div class="error-hints">
              <p><strong>常见问题：</strong></p>
              <ul>
                <li>✓ 检查 Prometheus/Grafana 服务是否启动</li>
                <li>✓ 确认地址和端口号是否正确</li>
                <li>✓ 检查防火墙或网络策略是否阻止访问</li>
                <li>✓ 如果使用 HTTPS，确保证书有效</li>
              </ul>
            </div>
          </template>
        </el-result>
      </div>
    </div>

    <!-- 快捷查询对话框 -->
    <el-dialog
      v-model="showQuickQuery"
      title="📊 快捷查询 - 业务与系统指标"
      width="900px"
      :close-on-click-modal="false"
    >
      <div class="quick-query-content">
        <p class="query-hint">选择下方指标，将自动在 Prometheus 中执行查询</p>
        
        <!-- 业务指标区域 -->
        <div class="category-section">
          <h2 class="section-header">💼 业务指标</h2>
          <el-divider />
          <div class="query-categories">
            <div v-for="category in businessCategories" :key="category.name" class="query-category">
              <h3 class="category-title">{{ category.icon }} {{ category.name }}</h3>
              <div class="query-list">
                <div
                  v-for="query in category.queries"
                  :key="query.name"
                  class="query-item"
                  @click="executeQuery(query.expression)"
                >
                  <div class="query-info">
                    <span class="query-name">{{ query.name }}</span>
                    <span class="query-desc">{{ query.description }}</span>
                  </div>
                  <el-button type="primary" size="small" link>
                    查看 →
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统指标区域 -->
        <div class="category-section">
          <h2 class="section-header">⚙️ 系统指标</h2>
          <el-divider />
          <div class="query-categories">
            <div v-for="category in systemCategories" :key="category.name" class="query-category">
              <h3 class="category-title">{{ category.icon }} {{ category.name }}</h3>
              <div class="query-list">
                <div
                  v-for="query in category.queries"
                  :key="query.name"
                  class="query-item"
                  @click="executeQuery(query.expression)"
                >
                  <div class="query-info">
                    <span class="query-name">{{ query.name }}</span>
                    <span class="query-desc">{{ query.description }}</span>
                  </div>
                  <el-button type="primary" size="small" link>
                    查看 →
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 配置对话框 -->
    <el-dialog
      v-model="showConfigDialog"
      title="配置监控地址"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="configForm" label-width="120px">
        <el-form-item label="Prometheus URL">
          <el-input v-model="configForm.prometheusUrl" placeholder="http://localhost:9090" />
        </el-form-item>
        <el-form-item label="Grafana URL">
          <el-input v-model="configForm.grafanaUrl" placeholder="http://localhost:3000" />
        </el-form-item>
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          show-icon
        >
          <p>配置保存后将在当前浏览器中生效</p>
          <p>建议联系系统管理员在环境变量中统一配置</p>
        </el-alert>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: space-between; width: 100%;">
          <el-button type="danger" plain @click="confirmClearConfig">清空配置</el-button>
          <div>
            <el-button @click="showConfigDialog = false">取消</el-button>
            <el-button type="primary" @click="saveConfig">保存</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import {computed, defineComponent, onMounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Loading, Refresh, Setting, TrendCharts} from '@element-plus/icons-vue'

export default defineComponent({
  name: 'DataMonitoring',
  components: {
    Loading,
    Setting,
    Refresh,
    TrendCharts
  },
  setup() {
    const showConfigDialog = ref(false)
    const showQuickQuery = ref(false)
    const loading = ref(true)
    const connectionError = ref(false)
    const iframeRef = ref<HTMLIFrameElement | null>(null)
    
    // 配置表单
    const configForm = ref({
      prometheusUrl: '',
      grafanaUrl: ''
    })
    
    // 查询模板 - 按类别组织
    const queryTemplates = ref([
      // ==================== 业务指标区域 ====================
      {
        name: '👥 用户业务',
        icon: '👥',
        category: 'business',
        queries: [
          {
            name: '当前在线用户数',
            description: '实时在线用户统计',
            expression: 'user_online_count'
          },
          {
            name: '登录成功率（5分钟）',
            description: '过去5分钟登录成功次数 / 登录尝试次数',
            expression: 'rate(user_login_success_total[5m]) / rate(user_login_attempt_total[5m]) * 100'
          },
          {
            name: '登录成功率（1分钟）',
            description: '过去1分钟登录成功次数 / 登录尝试次数（更实时）',
            expression: 'rate(user_login_success_total[1m]) / rate(user_login_attempt_total[1m]) * 100'
          },
          {
            name: '登录失败率',
            description: '登录失败趋势分析',
            expression: 'rate(user_login_failure_total[5m])'
          },
          {
            name: '平均登录耗时',
            description: '用户登录平均响应时间',
            expression: 'rate(user_login_duration_seconds_sum[5m]) / rate(user_login_duration_seconds_count[5m])'
          },
          {
            name: '新注册用户数（5分钟）',
            description: '最近5分钟新注册用户数',
            expression: 'increase(user_register_success_total[5m])'
          },
          {
            name: '新注册用户数（1小时）',
            description: '最近1小时新注册用户数',
            expression: 'increase(user_register_success_total[1h])'
          },
          {
            name: '注册用户总数',
            description: '系统启动以来累计注册成功总数',
            expression: 'user_register_success_total'
          },
          {
            name: '注册成功率',
            description: '注册成功次数 / 注册尝试次数',
            expression: 'rate(user_register_success_total[5m]) / rate(user_register_attempt_total[5m]) * 100'
          },
          {
            name: '验证码发送量',
            description: '验证码发送趋势',
            expression: 'rate(user_verification_code_sent_total[5m])'
          },
          {
            name: '用户管理操作速率',
            description: '创建/更新/删除用户操作速率（每秒）',
            expression: 'sum(rate(user_management_created_total[5m]) + rate(user_management_updated_total[5m]) + rate(user_management_deleted_total[5m]))'
          },
          {
            name: '用户管理操作总数',
            description: '系统启动以来所有用户管理操作总数',
            expression: 'sum(user_management_created_total + user_management_updated_total + user_management_deleted_total)'
          },
          {
            name: '头像上传成功率',
            description: '头像上传成功率统计',
            expression: '(rate(user_avatar_upload_total[5m]) - rate(user_avatar_upload_failure_total[5m])) / rate(user_avatar_upload_total[5m]) * 100'
          }
        ]
      },
      {
        name: '📊 表格业务',
        icon: '📊',
        category: 'business',
        queries: [
          {
            name: '活跃表格数量',
            description: '当前系统中活跃的表格总数',
            expression: 'table_active_count'
          },
          {
            name: '待审核数据条数',
            description: '等待管理员审核的数据量',
            expression: 'table_data_pending_count'
          },
          {
            name: '表格创建速率',
            description: '每秒创建表格数',
            expression: 'rate(table_management_created_total[5m])'
          },
          {
            name: '数据提交速率',
            description: '用户提交数据的频率',
            expression: 'rate(table_data_submitted_total[5m])'
          },
          {
            name: '数据审核通过率',
            description: '审核通过 / (通过 + 驳回)',
            expression: 'rate(table_data_approved_total[5m]) / (rate(table_data_approved_total[5m]) + rate(table_data_rejected_total[5m])) * 100'
          },
          {
            name: '数据驳回率',
            description: '数据被驳回的比例',
            expression: 'rate(table_data_rejected_total[5m]) / rate(table_data_submitted_total[5m]) * 100'
          },
          {
            name: '数据导出量',
            description: '数据导出操作统计',
            expression: 'rate(table_data_exported_total[5m])'
          },
          {
            name: '附件上传量',
            description: '附件上传统计',
            expression: 'rate(table_attachment_upload_total[5m])'
          },
          {
            name: '附件下载量',
            description: '附件下载统计',
            expression: 'rate(table_attachment_download_total[5m])'
          }
        ]
      },
      // ==================== 系统指标区域 ====================
      {
        name: '🔍 基础监控',
        icon: '🔍',
        category: 'system',
        queries: [
          {
            name: '查看所有指标',
            description: '显示Prometheus中所有可用的指标',
            expression: '{__name__=~".+"}'
          },
          {
            name: '查看所有Job',
            description: '显示所有被监控的目标',
            expression: 'up'
          },
          {
            name: 'Prometheus本身',
            description: '查看Prometheus服务状态',
            expression: 'prometheus_build_info'
          }
        ]
      },
      {
        name: '💻 系统资源',
        icon: '💻',
        category: 'system',
        queries: [
          {
            name: 'CPU使用率',
            description: '查看系统CPU使用情况（需要node_exporter）',
            expression: '100 - (avg by (instance) (irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)'
          },
          {
            name: '内存使用率',
            description: '查看系统内存使用情况（需要node_exporter）',
            expression: '(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100'
          },
          {
            name: '磁盘使用率',
            description: '查看磁盘空间使用情况（需要node_exporter）',
            expression: '(1 - node_filesystem_avail_bytes{fstype!~"tmpfs|fuse.lxcfs|squashfs|vfat"} / node_filesystem_size_bytes{fstype!~"tmpfs|fuse.lxcfs|squashfs|vfat"}) * 100'
          }
        ]
      },
      {
        name: '⚡ 应用性能',
        icon: '⚡',
        category: 'system',
        queries: [
          {
            name: 'HTTP请求总数',
            description: '查看HTTP请求总量（需要应用配置）',
            expression: 'sum(rate(http_requests_total[5m]))'
          },
          {
            name: 'HTTP错误率',
            description: '查看4xx和5xx错误率（需要应用配置）',
            expression: 'sum(rate(http_requests_total{status=~"4..|5.."}[5m])) / sum(rate(http_requests_total[5m])) * 100'
          }
        ]
      },
      {
        name: '🗄️ 数据库',
        icon: '🗄️',
        category: 'system',
        queries: [
          {
            name: '数据库连接数',
            description: '当前活动连接数（需要mysqld_exporter）',
            expression: 'mysql_global_status_threads_connected'
          },
          {
            name: '查询速率',
            description: '每秒查询数（需要mysqld_exporter）',
            expression: 'rate(mysql_global_status_queries[5m])'
          }
        ]
      },
      {
        name: '☕ JVM监控',
        icon: '☕',
        category: 'system',
        queries: [
          {
            name: 'JVM堆内存使用',
            description: 'Java堆内存使用情况（需要JMX exporter）',
            expression: 'jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100'
          },
          {
            name: 'GC次数',
            description: '垃圾回收次数（需要JMX exporter）',
            expression: 'rate(jvm_gc_collection_seconds_count[5m])'
          }
        ]
      }
    ])

    // 分离业务指标和系统指标
    const businessCategories = computed(() => {
      return queryTemplates.value.filter(cat => cat.category === 'business')
    })

    const systemCategories = computed(() => {
      return queryTemplates.value.filter(cat => cat.category === 'system')
    })

    // 从环境变量或localStorage获取配置
    const getConfig = () => {
      const env = (import.meta as any).env ?? {}
      
      // 优先使用localStorage的配置
      const savedPrometheus = localStorage.getItem('monitoring_prometheus_url')
      const savedGrafana = localStorage.getItem('monitoring_grafana_url')
      
      return {
        prometheusUrl: savedPrometheus || env.VITE_PROMETHEUS_URL || '',
        grafanaUrl: savedGrafana || env.VITE_GRAFANA_URL || ''
      }
    }

    // 初始化配置
    const initConfig = () => {
      const config = getConfig()
      configForm.value.prometheusUrl = config.prometheusUrl
      configForm.value.grafanaUrl = config.grafanaUrl
    }

    // 保存配置
    const saveConfig = () => {
      if (configForm.value.prometheusUrl) {
        localStorage.setItem('monitoring_prometheus_url', configForm.value.prometheusUrl)
      }
      if (configForm.value.grafanaUrl) {
        localStorage.setItem('monitoring_grafana_url', configForm.value.grafanaUrl)
      }
      
      ElMessage.success('配置已保存，页面将刷新')
      showConfigDialog.value = false
      
      // 刷新页面以应用新配置
      setTimeout(() => {
        window.location.reload()
      }, 500)
    }

    // 确认清空配置
    const confirmClearConfig = () => {
      import('element-plus').then(({ ElMessageBox }) => {
        ElMessageBox.confirm(
          '确定要清空所有监控配置吗？清空后将无法查看监控数据。',
          '清空配置确认',
          {
            confirmButtonText: '确定清空',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          clearConfig()
        }).catch(() => {
          // 用户取消操作
        })
      })
    }

    // 清空配置
    const clearConfig = () => {
      // 删除 localStorage 中的监控配置
      localStorage.removeItem('monitoring_prometheus_url')
      localStorage.removeItem('monitoring_grafana_url')
      
      // 清空表单数据
      configForm.value.prometheusUrl = ''
      configForm.value.grafanaUrl = ''
      
      ElMessage.success('配置已清空，页面将刷新')
      showConfigDialog.value = false
      
      // 刷新页面以应用新配置
      setTimeout(() => {
        window.location.reload()
      }, 500)
    }

    // 获取监控URL（优先使用Grafana，其次Prometheus）
    const prometheusUrl = computed(() => {
      const config = getConfig()
      let url = config.grafanaUrl || config.prometheusUrl
      
      // 确保URL包含协议前缀
      if (url && !url.startsWith('http://') && !url.startsWith('https://')) {
        url = 'http://' + url
        console.log('[DataMonitoring] 自动添加http://前缀:', url)
      }
      
      return url
    })

    // iframe加载完成
    const handleIframeLoad = () => {
      loading.value = false
      connectionError.value = false
      console.log('[DataMonitoring] iframe 加载成功')
    }

    // iframe加载错误
    const handleIframeError = () => {
      loading.value = false
      connectionError.value = true
      console.error('[DataMonitoring] iframe 加载失败')
    }

    // 重试连接
    const retryConnection = () => {
      loading.value = true
      connectionError.value = false
      
      // 重新加载iframe
      const iframe = document.querySelector('.monitoring-iframe') as HTMLIFrameElement
      if (iframe) {
        iframe.src = iframe.src
      }
    }
    
    // 执行查询
    const executeQuery = (expression: string) => {
      const baseUrl = prometheusUrl.value
      if (!baseUrl) {
        ElMessage.error('请先配置监控地址')
        return
      }
      
      // 构建查询URL
      const encodedExpr = encodeURIComponent(expression)
      const queryUrl = `${baseUrl}/graph?g0.expr=${encodedExpr}&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h`
      
      // 更新iframe地址
      const iframe = document.querySelector('.monitoring-iframe') as HTMLIFrameElement
      if (iframe) {
        iframe.src = queryUrl
        showQuickQuery.value = false
        ElMessage.success(`正在查询：${expression}`)
      }
    }

    // 监听iframe内部的错误（通过定时检查）
    let errorCheckTimer: any = null
    
    const startErrorCheck = () => {
      // 5秒后检查iframe是否成功加载
      errorCheckTimer = setTimeout(() => {
        if (loading.value) {
          // 如果5秒后还在加载状态，认为连接失败
          loading.value = false
          connectionError.value = true
          ElMessage.error('监控服务连接超时，请检查配置')
        }
      }, 5000)
    }

    const stopErrorCheck = () => {
      if (errorCheckTimer) {
        clearTimeout(errorCheckTimer)
        errorCheckTimer = null
      }
    }

    onMounted(() => {
      console.log('[DataMonitoring] 组件已挂载')
      initConfig()
      console.log('[DataMonitoring] Prometheus URL:', prometheusUrl.value)
      
      if (prometheusUrl.value) {
        startErrorCheck()
      }
    })

    return {
      showConfigDialog,
      showQuickQuery,
      loading,
      connectionError,
      configForm,
      queryTemplates,
      businessCategories,
      systemCategories,
      prometheusUrl,
      Loading,
      Setting,
      Refresh,
      TrendCharts,
      saveConfig,
      confirmClearConfig,
      handleIframeLoad,
      handleIframeError,
      retryConnection,
      executeQuery
    }
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.data-monitoring {
  position: fixed;
  top: 0;
  left: 260px; // 侧边栏宽度
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  background: $background-primary;
  overflow: hidden;
  z-index: 100;
}

.config-prompt {
  height: 100%;
  @include flex-center;
  padding: $spacing-xxxl;
}

.empty-icon {
  font-size: 120px;
  margin-bottom: $spacing-lg;
}

.config-hint {
  color: $text-secondary;
  font-size: $font-size-lg;
  margin: $spacing-lg 0;
}

.iframe-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.floating-toolbar {
  position: fixed;
  bottom: $spacing-xxl;
  right: $spacing-xxl;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  
  button {
    width: 56px;
    height: 56px;
    background: linear-gradient(135deg, $primary-color 0%, $secondary-color 100%);
    color: $text-white;
    border: none;
    box-shadow: 0 4px 16px rgba($primary-color, 0.4);
    transition: all 0.3s;
    
    &:hover {
      transform: translateX(-8px) scale(1.1);
      box-shadow: 0 6px 24px rgba($primary-color, 0.6);
    }
    
    &:active {
      transform: translateX(-8px) scale(1.05);
    }
  }
  
  @media (max-width: 768px) {
    bottom: $spacing-lg;
    right: $spacing-lg;
    
    button {
      width: 48px;
      height: 48px;
    }
  }
}

.monitoring-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: $background-primary;
}

.iframe-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  @include flex-center;
  flex-direction: column;
  background: rgba($background-primary, 0.95);
  color: $primary-color;
  font-size: $font-size-xl;
  gap: $spacing-lg;
  z-index: 10;
  
  .el-icon {
    font-size: 48px;
  }
  
  p {
    margin: 0;
    font-weight: $font-weight-medium;
  }
}

.connection-error {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: $background-primary;
  @include flex-center;
  z-index: 20;
  padding: $spacing-xxl;
  overflow-y: auto;
  
  .error-icon {
    font-size: 80px;
    margin-bottom: $spacing-lg;
  }
  
  .error-actions {
    display: flex;
    gap: $spacing-md;
    margin-bottom: $spacing-xl;
    justify-content: center;
    flex-wrap: wrap;
    
    .btn-icon {
      margin-right: $spacing-xs;
      font-size: $font-size-lg;
    }
  }
  
  .error-hints {
    background: rgba($primary-color, 0.05);
    padding: $spacing-lg;
    border-radius: $border-radius;
    text-align: left;
    max-width: 500px;
    margin: 0 auto;
    
    p {
      color: $text-primary;
      font-size: $font-size-md;
      margin: 0 0 $spacing-sm 0;
    }
    
    ul {
      list-style: none;
      padding: 0;
      margin: $spacing-sm 0 0 0;
      
      li {
        color: $text-secondary;
        font-size: $font-size-sm;
        padding: $spacing-xs 0;
        line-height: 1.6;
        
        &:before {
          margin-right: $spacing-xs;
        }
      }
    }
  }
}

.quick-query-content {
  padding: $spacing-md 0;
  max-height: 70vh;
  overflow-y: auto;
}

.query-hint {
  text-align: center;
  color: $text-secondary;
  font-size: $font-size-md;
  margin-bottom: $spacing-lg;
  padding: $spacing-md;
  background: linear-gradient(135deg, rgba($primary-color, 0.08) 0%, rgba($secondary-color, 0.08) 100%);
  border-radius: $border-radius;
  border-left: 3px solid $primary-color;
}

.category-section {
  margin-bottom: $spacing-xxxl;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .section-header {
    color: $text-primary;
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
    margin: 0 0 $spacing-sm 0;
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    
    &::before {
      content: '';
      width: 4px;
      height: 24px;
      background: linear-gradient(135deg, $primary-color 0%, $secondary-color 100%);
      border-radius: $border-radius-small;
    }
  }
}

.query-categories {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.query-category {
  background: rgba($primary-color, 0.02);
  padding: $spacing-lg;
  border-radius: $border-radius;
  border: 1px solid rgba($primary-color, 0.1);
  transition: all 0.3s;
  
  &:hover {
    background: rgba($primary-color, 0.04);
    border-color: rgba($primary-color, 0.2);
    box-shadow: 0 2px 8px rgba($primary-color, 0.1);
  }
  
  .category-title {
    color: $primary-color;
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    margin: 0 0 $spacing-md 0;
    padding-bottom: $spacing-sm;
    border-bottom: 2px solid rgba($primary-color, 0.2);
  }
}

.query-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.query-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  background: rgba($primary-color, 0.03);
  border-radius: $border-radius;
  border: 1px solid rgba($primary-color, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: rgba($primary-color, 0.08);
    border-color: $primary-color;
    transform: translateX(4px);
    box-shadow: 0 2px 8px rgba($primary-color, 0.15);
  }
}

.query-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  flex: 1;
  
  .query-name {
    color: $text-primary;
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
  }
  
  .query-desc {
    color: $text-secondary;
    font-size: $font-size-sm;
  }
}
</style>

