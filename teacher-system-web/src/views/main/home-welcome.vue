<template>
  <div class="home-welcome">
    <!-- 超级管理员视图 -->
    <div v-if="isSuperAdmin" class="dashboard-container">
      <div class="welcome-header">
        <div class="header-info">
          <h1 class="welcome-title">
            <span class="icon">👨‍💼</span>
            欢迎回来，{{ username }}
          </h1>
          <p class="welcome-subtitle">超级管理员 - 全局管理控制台</p>
        </div>
        <div class="header-badge">
          <span class="badge super-admin">Super Admin</span>
        </div>
      </div>

      <!-- 功能卡片网格 -->
      <div class="feature-grid">
        <div class="feature-card primary" @click="goToTableManagement">
          <div class="card-icon">📊</div>
          <h3 class="card-title">表格管理</h3>
          <p class="card-desc">创建和编辑表格结构，配置字段和权限</p>
          <div class="card-footer">
            <span class="action-text">立即管理 →</span>
          </div>
        </div>

        <div class="feature-card secondary" @click="goToAccountManagement">
          <div class="card-icon">👥</div>
          <h3 class="card-title">账号管理</h3>
          <p class="card-desc">管理用户账号，分配角色和权限</p>
          <div class="card-footer">
            <span class="action-text">管理账号 →</span>
          </div>
        </div>

        <div class="feature-card accent" @click="goToDataMonitoring">
          <div class="card-icon">📈</div>
          <h3 class="card-title">数据监控</h3>
          <p class="card-desc">实时监控系统运行状态和性能指标</p>
          <div class="card-footer">
            <span class="action-text">查看监控 →</span>
          </div>
        </div>

        <div class="feature-card success" @click="goToSystemConfig">
          <div class="card-icon">⚙️</div>
          <h3 class="card-title">系统配置</h3>
          <p class="card-desc">配置监控地址和系统参数</p>
          <div class="card-footer">
            <span class="action-text">系统设置 →</span>
          </div>
        </div>
      </div>

      <!-- 快速统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon">📊</div>
          <div class="stat-content">
            <div class="stat-value">{{ tableList.length }}</div>
            <div class="stat-label">已创建表格</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.userCount || '-' }}</div>
            <div class="stat-label">系统用户</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingCount || '-' }}</div>
            <div class="stat-label">待审核数据</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📈</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalDataCount || '-' }}</div>
            <div class="stat-label">总数据量</div>
          </div>
        </div>
      </div>

      <!-- 快速提示 -->
      <div class="quick-tips">
        <h3 class="tips-title">💡 管理要点</h3>
        <ul class="tips-list">
          <li>📋 <strong>表格管理</strong>：创建/编辑/删除表格，配置字段和权限</li>
          <li>👥 <strong>账号管理</strong>：创建用户账号，分配角色和权限</li>
          <li>🔍 <strong>数据审核</strong>：从表格列表进入，查看和审核所有数据</li>
          <li>📊 <strong>系统监控</strong>：实时查看系统运行状态和性能指标</li>
        </ul>
      </div>

      <!-- 表格列表快捷访问 -->
      <div class="admin-tables-section">
        <h3 class="section-title">📋 已创建的表格</h3>
        <p class="section-desc">点击表格进入数据管理和审核</p>
        <div v-if="tableList.length > 0" class="table-cards-grid">
          <div 
            v-for="table in tableList" 
            :key="table.tableId"
            class="table-quick-card"
            @click="goToTable(table.tableId)"
          >
            <div class="table-card-icon">📄</div>
            <div class="table-card-info">
              <h4 class="table-card-name">{{ table.tableFullName }}</h4>
              <p class="table-card-desc">点击进入管理和审核</p>
            </div>
            <div class="table-card-arrow">→</div>
          </div>
        </div>
        <div v-else class="no-tables-hint">
          <div class="hint-icon">📭</div>
          <p>暂无表格，点击上方"表格管理"创建新表格</p>
        </div>
      </div>
    </div>

    <!-- 普通管理员视图 -->
    <div v-else-if="isAdmin" class="dashboard-container">
      <div class="welcome-header">
        <div class="header-info">
          <h1 class="welcome-title">
            <span class="icon">👨‍🏫</span>
            欢迎回来，{{ username }}
          </h1>
          <p class="welcome-subtitle">管理员 - 数据审核与管理</p>
        </div>
        <div class="header-badge">
          <span class="badge admin">Admin</span>
        </div>
      </div>

      <!-- 快速统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon">📋</div>
          <div class="stat-content">
            <div class="stat-value">{{ tableList.length }}</div>
            <div class="stat-label">可管理表格</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">⏳</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingCount || '-' }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.scoredCount || '-' }}</div>
            <div class="stat-label">已审核</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📊</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalDataCount || '-' }}</div>
            <div class="stat-label">总数据</div>
          </div>
        </div>
      </div>

      <!-- 表格列表快捷访问 -->
      <div class="admin-tables-section">
        <h3 class="section-title">📋 可管理的表格</h3>
        <p class="section-desc">选择下方表格开始审核和管理数据</p>
        <div v-if="tableList.length > 0" class="table-cards-grid">
          <div 
            v-for="table in tableList" 
            :key="table.tableId"
            class="table-quick-card"
            @click="goToTable(table.tableId)"
          >
            <div class="table-card-icon">📄</div>
            <div class="table-card-info">
              <h4 class="table-card-name">{{ table.tableFullName }}</h4>
              <p class="table-card-desc">点击进入管理和审核</p>
            </div>
            <div class="table-card-arrow">→</div>
          </div>
        </div>
        <div v-else class="no-tables-hint">
          <div class="hint-icon">📭</div>
          <p>暂无可管理的表格</p>
        </div>
      </div>

      <!-- 快速指南 -->
      <div class="quick-tips">
        <h3 class="tips-title">💡 工作指南</h3>
        <ul class="tips-list">
          <li>📝 <strong>审核数据</strong>：点击表格卡片进入，查看用户提交的数据并打分</li>
          <li>✏️ <strong>编辑数据</strong>：可以直接编辑和修改表格中的数据内容</li>
          <li>🔍 <strong>筛选查看</strong>：使用筛选功能快速定位特定状态的数据</li>
          <li>📊 <strong>查看统计</strong>：在表格详情页查看数据统计和分析</li>
        </ul>
      </div>
    </div>

    <!-- 普通用户视图 -->
    <div v-else class="dashboard-container user-view">
      <div class="welcome-header user-header">
        <div class="header-info">
          <h1 class="welcome-title">
            <span class="icon">👤</span>
            你好，{{ username }}
          </h1>
          <p class="welcome-subtitle">开始记录您的数据吧</p>
        </div>
        <div class="header-badge">
          <span class="badge user">User</span>
        </div>
      </div>

      <!-- 快速统计卡片 -->
      <div class="stats-cards user-stats">
        <div class="stat-card user-stat">
          <div class="stat-icon">📊</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.submittedCount || 0 }}</div>
            <div class="stat-label">已提交数据</div>
          </div>
        </div>
        <div class="stat-card user-stat">
          <div class="stat-icon">⏳</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingCount || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </div>
        <div class="stat-card user-stat">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.scoredCount || 0 }}</div>
            <div class="stat-label">审核完成</div>
          </div>
        </div>
      </div>

      <!-- 快速操作区 -->
      <div class="quick-actions-section">
        <div class="action-card main-action" @click="goToMyData">
          <div class="action-icon">📊</div>
          <div class="action-content">
            <h3>我的数据</h3>
            <p>查看和管理您提交的所有数据，查询审核状态和得分</p>
          </div>
          <div class="action-arrow">→</div>
        </div>
      </div>

      <!-- 可用表格列表 -->
      <div class="user-tables-section" v-if="tableList.length > 0">
        <h3 class="section-title">📋 可填写的表格</h3>
        <p class="section-desc">选择下方表格开始填写和提交数据</p>
        <div class="table-cards-grid">
          <div 
            v-for="table in tableList" 
            :key="table.tableId"
            class="table-quick-card user-table-card"
            @click="goToTable(table.tableId)"
          >
            <div class="table-card-icon">📄</div>
            <div class="table-card-info">
              <h4 class="table-card-name">{{ table.tableFullName }}</h4>
              <p class="table-card-desc">点击进入填写数据</p>
            </div>
            <div class="table-card-arrow">→</div>
          </div>
        </div>
      </div>

      <!-- 使用指南 -->
      <div class="user-guide">
        <h3 class="guide-title">📖 使用指南</h3>
        <div class="guide-steps">
          <div class="step">
            <div class="step-number">1</div>
            <div class="step-content">
              <h4>选择表格</h4>
              <p>从左侧菜单选择要填写的表格</p>
            </div>
          </div>
          <div class="step-arrow">→</div>
          <div class="step">
            <div class="step-number">2</div>
            <div class="step-content">
              <h4>填写数据</h4>
              <p>按要求填写表格字段信息</p>
            </div>
          </div>
          <div class="step-arrow">→</div>
          <div class="step">
            <div class="step-number">3</div>
            <div class="step-content">
              <h4>上传附件</h4>
              <p>上传相关证明材料（如需要）</p>
            </div>
          </div>
          <div class="step-arrow">→</div>
          <div class="step">
            <div class="step-number">4</div>
            <div class="step-content">
              <h4>提交审核</h4>
              <p>提交后等待管理员审核评分</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 温馨提示 -->
      <div class="warm-tips">
        <div class="tip-icon">💡</div>
        <div class="tip-content">
          <h4>温馨提示</h4>
          <p>• 数据提交后可以保存为草稿，稍后继续编辑</p>
          <p>• 附件支持 PDF、Word、图片等多种格式</p>
          <p>• 在"我的数据"中可以查看所有提交记录和审核状态</p>
          <p>• 如有疑问，请联系管理员协助</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useUserStore} from '@/store/user'
import {getTableList, type TableListItem} from '@/api/table'
import {getAllUsersForManagement} from '@/api/userManagement'
import {getMyDataStatistics} from '@/api/myData'
import {getGlobalStatistics, getMyStatusStatistics} from '@/api/tableData'
import {ElMessage} from 'element-plus'

export default defineComponent({ 
  name: 'HomeWelcome',
  data() {
    return {
      tableList: [] as TableListItem[],
      // 统计数据
      stats: {
        userCount: 0,        // 系统用户数
        pendingCount: 0,     // 待审核数据
        totalDataCount: 0,   // 总数据量
        submittedCount: 0,   // 已提交数据（普通用户）
        scoredCount: 0       // 已打分数据（普通用户）
      }
    }
  },
  computed: {
    isSuperAdmin(): boolean {
      const userStore = useUserStore()
      // 超级管理员拥有 table:create 权限
      return userStore.hasPermission('table:create')
    },
    isAdmin(): boolean {
      const userStore = useUserStore()
      // 普通管理员拥有 table:edit 或 table:delete 权限，但没有 table:create
      return !this.isSuperAdmin && userStore.hasAnyPermission([
        'table:edit', 'table:delete', 'table:data:score'
      ])
    },
    username(): string {
      const userStore = useUserStore()
      return userStore.username
    }
  },
  mounted() {
    // 加载表格列表和统计数据
    if (this.isAdmin || this.isSuperAdmin) {
      this.loadTableList()
      this.loadStatistics()
    } else {
      // 普通用户加载个人统计
      this.loadUserStatistics()
    }
  },
  methods: {
    async loadTableList() {
      try {
        this.tableList = await getTableList()
      } catch (error) {
        console.error('加载表格列表失败:', error)
        ElMessage.error('加载表格列表失败')
      }
    },
    
    // 加载管理员统计数据
    async loadStatistics() {
      try {
        // 获取全局数据统计
        const globalStats = await getGlobalStatistics()
        this.stats.totalDataCount = globalStats.totalCount
        this.stats.pendingCount = globalStats.pendingCount
        this.stats.scoredCount = globalStats.scoredCount
        
        if (this.isSuperAdmin) {
          // 超级管理员：获取系统用户数
          const users = await getAllUsersForManagement()
          this.stats.userCount = users.length
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    
    // 加载普通用户统计数据
    async loadUserStatistics() {
      try {
        // 获取用户数据总体统计
        const statistics = await getMyDataStatistics()
        this.stats.submittedCount = statistics.totalCount
        
        // 获取用户数据按状态分类统计
        const statusStats = await getMyStatusStatistics()
        this.stats.pendingCount = statusStats.pendingCount
        this.stats.scoredCount = statusStats.scoredCount
      } catch (error) {
        console.error('加载个人统计失败:', error)
      }
    },
    goToMyData() {
      this.$router.push({ name: 'MyData' })
    },
    goToTableManagement() {
      this.$router.push({ name: 'TableManagement' })
    },
    goToAccountManagement() {
      this.$router.push({ name: 'AccountManagement' })
    },
    goToDataMonitoring() {
      this.$router.push({ name: 'DataMonitoring' })
    },
    goToSystemConfig() {
      this.$router.push({ name: 'SystemConfig' })
    },
    goToTable(tableId: number) {
      this.$router.push({ 
        name: 'TableDetail', 
        params: { id: tableId } 
      })
    }
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.home-welcome {
  min-height: 100vh;
  padding: 40px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
}

// ==================== 通用头部 ====================
.welcome-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding: 30px 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.header-info {
  .welcome-title {
    font-size: 32px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 8px 0;
    display: flex;
    align-items: center;
    gap: 12px;

    .icon {
      font-size: 40px;
    }
  }

  .welcome-subtitle {
    font-size: 16px;
    color: #6b7280;
    margin: 0;
  }
}

.header-badge {
  .badge {
    display: inline-block;
    padding: 8px 20px;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;

    &.super-admin {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    &.admin {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: white;
      box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
    }

    &.user {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      color: white;
      box-shadow: 0 4px 12px rgba(79, 172, 254, 0.4);
    }
  }
}

// ==================== 功能卡片网格 ====================
.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 40px;

  &.admin-grid {
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  }
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    transform: scaleX(0);
    transition: transform 0.3s ease;
  }

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);

    &::before {
      transform: scaleX(1);
    }

    .card-footer .action-text {
      transform: translateX(8px);
    }
  }

  .card-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  .card-title {
    font-size: 22px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 12px 0;
  }

  .card-desc {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.6;
    margin: 0 0 20px 0;
  }

  .card-footer {
    .action-text {
      display: inline-block;
      font-size: 14px;
      font-weight: 600;
      color: #3b82f6;
      transition: transform 0.3s ease;
    }
  }

  &.primary::before {
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  }

  &.secondary::before {
    background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
  }

  &.accent::before {
    background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  }

  &.success::before {
    background: linear-gradient(90deg, #43e97b 0%, #38f9d7 100%);
  }
}

// ==================== 快速提示 ====================
.quick-tips {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);

  .tips-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0 0 20px 0;
  }

  .tips-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      font-size: 15px;
      line-height: 2;
      opacity: 0.95;

      strong {
        color: $primary-color;
        font-weight: 600;
      }
    }
  }
}

// ==================== 统计卡片 ====================
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;

  .stat-card {
    background: linear-gradient(135deg, $background-primary 0%, rgba($primary-color, 0.05) 100%);
    border: 2px solid rgba($primary-color, 0.1);
    border-radius: $border-radius-large;
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    transition: all $transition-normal;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 4px;
      height: 100%;
      background: linear-gradient(180deg, $primary-color 0%, $secondary-color 100%);
      opacity: 0;
      transition: opacity $transition-normal;
    }

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px rgba($primary-color, 0.15);
      border-color: rgba($primary-color, 0.3);

      &::before {
        opacity: 1;
      }
    }

    .stat-icon {
      font-size: 40px;
      flex-shrink: 0;
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 32px;
        font-weight: $font-weight-bold;
        color: $primary-color;
        margin-bottom: 4px;
        line-height: 1;
      }

      .stat-label {
        font-size: $font-size-sm;
        color: $text-secondary;
        font-weight: $font-weight-medium;
      }
    }
  }
}

// 用户统计卡片特殊样式
.user-stats {
  .stat-card.user-stat {
    &.highlight {
      background: linear-gradient(135deg, rgba($success-color, 0.1) 0%, rgba($success-color, 0.05) 100%);
      border-color: rgba($success-color, 0.3);

      &::before {
        background: linear-gradient(180deg, $success-color 0%, lighten($success-color, 10%) 100%);
      }

      .stat-value {
        color: $success-color;
      }
    }
  }
}

// ==================== 管理员欢迎消息 ====================
.admin-welcome-message {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  margin-bottom: 32px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);

  .message-icon {
    font-size: 48px;
    flex-shrink: 0;
  }

  .message-content {
    flex: 1;

    h3 {
      font-size: 22px;
      font-weight: 600;
      margin: 0 0 8px 0;
      color: white;
    }

    p {
      font-size: 15px;
      margin: 0;
      opacity: 0.95;
      line-height: 1.6;
    }
  }
}

// ==================== 管理员表格列表 ====================
.admin-tables-section {
  margin-bottom: 40px;

  .section-title {
    font-size: 24px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 8px 0;
  }

  .section-desc {
    font-size: 14px;
    color: #6b7280;
    margin: 0 0 24px 0;
  }

  .table-cards-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 16px;
  }

  .table-quick-card {
    background: white;
    padding: 20px 24px;
    border-radius: 12px;
    border: 2px solid #e5e7eb;
    display: flex;
    align-items: center;
    gap: 16px;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    &:hover {
      border-color: #3b82f6;
      transform: translateY(-4px);
      box-shadow: 0 8px 20px rgba(59, 130, 246, 0.2);

      .table-card-arrow {
        transform: translateX(6px);
      }
    }

    .table-card-main {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 16px;
      cursor: pointer;
      min-width: 0;
    }

    .table-card-actions {
      display: flex;
      gap: 8px;
      flex-shrink: 0;

      button {
        padding: 8px 12px;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 18px;
        transition: all 0.2s ease;
        background: transparent;

        &:hover {
          transform: scale(1.1);
        }

        &.btn-edit {
          &:hover {
            background: #eff6ff;
          }
        }

        &.btn-delete {
          &:hover {
            background: #fee;
          }
        }
      }
    }

    .table-card-icon {
      font-size: 36px;
      flex-shrink: 0;
    }

    .table-card-info {
      flex: 1;
      min-width: 0;

      .table-card-name {
        font-size: 16px;
        font-weight: 600;
        color: #1f2937;
        margin: 0 0 4px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .table-card-desc {
        font-size: 13px;
        color: #6b7280;
        margin: 0;
      }
    }

    .table-card-arrow {
      font-size: 24px;
      color: #3b82f6;
      flex-shrink: 0;
      transition: transform 0.3s ease;
    }
  }

  .no-tables-hint {
    text-align: center;
    padding: 48px 20px;
    background: white;
    border-radius: 12px;
    border: 2px dashed #e5e7eb;

    .hint-icon {
      font-size: 56px;
      margin-bottom: 16px;
    }

    p {
      font-size: 16px;
      color: #6b7280;
      margin: 0;
    }
  }
}

// ==================== 工作信息（管理员）====================
.work-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
  margin-top: 0;

  .info-card {
    background: white;
    padding: 28px;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    display: flex;
    gap: 20px;

    .info-icon {
      font-size: 40px;
      flex-shrink: 0;
    }

    .info-content {
      flex: 1;

      h4 {
        font-size: 18px;
        font-weight: 600;
        color: #1f2937;
        margin: 0 0 16px 0;
      }

      ul {
        list-style: none;
        padding: 0;
        margin: 0;

        li {
          font-size: 14px;
          color: #6b7280;
          line-height: 2;
          padding-left: 16px;
          position: relative;

          &::before {
            content: '•';
            position: absolute;
            left: 0;
            color: #3b82f6;
            font-weight: bold;
          }
        }
      }
    }
  }
}

// ==================== 普通用户视图 ====================
.user-view {
  max-width: 1000px;
}

.user-header {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);

  .header-info {
    .welcome-title {
      color: white;
    }

    .welcome-subtitle {
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

.quick-actions-section {
  margin-bottom: 40px;

  .main-action {
    background: white;
    padding: 32px;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    display: flex;
    align-items: center;
    gap: 24px;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-bottom: 20px;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);

      .action-arrow {
        transform: translateX(8px);
      }
    }

    .action-icon {
      font-size: 56px;
      flex-shrink: 0;
    }

    .action-content {
      flex: 1;

      h3 {
        font-size: 24px;
        font-weight: 600;
        color: #1f2937;
        margin: 0 0 8px 0;
      }

      p {
        font-size: 14px;
        color: #6b7280;
        margin: 0;
      }
    }

    .action-arrow {
      font-size: 32px;
      color: #3b82f6;
      transition: transform 0.3s ease;
    }
  }

  .action-cards-row {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;

    .action-card.mini {
      background: white;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
      }

      .action-icon.small {
        font-size: 32px;
        margin-bottom: 12px;
      }

      .action-content {
        h4 {
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 6px 0;
        }

        p {
          font-size: 12px;
          color: #6b7280;
          margin: 0;
        }
      }
    }
  }
}

// ==================== 使用指南 ====================
.user-guide {
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;

  .guide-title {
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 28px 0;
  }

  .guide-steps {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 16px;

    .step {
      flex: 1;
      min-width: 150px;
      text-align: center;

      .step-number {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        color: white;
        font-size: 20px;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 16px;
      }

      .step-content {
        h4 {
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 8px 0;
        }

        p {
          font-size: 13px;
          color: #6b7280;
          margin: 0;
          line-height: 1.5;
        }
      }
    }

    .step-arrow {
      font-size: 24px;
      color: #cbd5e1;
      flex-shrink: 0;
    }
  }
}

// ==================== 温馨提示 ====================
.warm-tips {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  padding: 24px 28px;
  border-radius: 16px;
  display: flex;
  gap: 20px;

  .tip-icon {
    font-size: 36px;
    flex-shrink: 0;
  }

  .tip-content {
    flex: 1;

    h4 {
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 12px 0;
    }

    p {
      font-size: 14px;
      color: #4b5563;
      margin: 0 0 6px 0;
      line-height: 1.6;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

// ==================== 响应式设计 ====================
@media (max-width: 992px) {
  .welcome-header {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .guide-steps {
    flex-direction: column;

    .step-arrow {
      transform: rotate(90deg);
    }
  }

  .work-info {
    grid-template-columns: 1fr;
  }

  .action-cards-row {
    grid-template-columns: 1fr !important;
  }

  .table-cards-grid {
    grid-template-columns: 1fr !important;
  }

  .admin-welcome-message {
    flex-direction: column;
    text-align: center;
  }
}

@media (max-width: 768px) {
  .home-welcome {
    padding: 20px 12px;
  }

  .welcome-header {
    padding: 20px;

    .header-info .welcome-title {
      font-size: 24px;

      .icon {
        font-size: 32px;
      }
    }
  }

  .feature-card {
    padding: 24px;
  }

  .quick-actions-section .main-action {
    flex-direction: column;
    text-align: center;

    .action-arrow {
      display: none;
    }
  }
}
</style>
