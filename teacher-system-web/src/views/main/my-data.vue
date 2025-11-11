<template>
  <section class="my-data-container">
    <div class="page-header">
      <h1 class="page-title">我的数据</h1>
      <p class="page-subtitle">查看您上传的所有加分数据</p>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalCount }}</div>
          <div class="stat-label">总数据条数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📝</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.tableCount }}</div>
          <div class="stat-label">涉及表格数</div>
        </div>
      </div>
      <!-- 分数统计仅管理员可见 -->
      <div v-if="isAdmin" class="stat-card">
        <div class="stat-icon">⭐</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalScore }}</div>
          <div class="stat-label">总分数</div>
        </div>
      </div>
      <div v-if="isAdmin" class="stat-card">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.avgScore }}</div>
          <div class="stat-label">平均分数</div>
        </div>
      </div>
    </div>

    <!-- 按表格分组的数据列表 -->
    <div class="data-by-table">
      <h2 class="section-title">数据详情</h2>
      
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="dataByTable.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p class="empty-text">您还没有上传任何数据</p>
        <p class="empty-hint">前往表格列表开始添加数据吧！</p>
      </div>

      <div v-else class="table-groups">
        <div v-for="group in dataByTable" :key="group.tableId" class="table-group">
          <div class="table-group-header">
            <div class="table-header-left">
              <h3 class="table-name">{{ group.tableName }}</h3>
              <span class="data-count-badge">{{ group.dataList.length }} 条数据</span>
            </div>
            <!-- 本表总分仅管理员可见 -->
            <div v-if="isAdmin" class="table-score-summary">
              <span class="score-icon">🏆</span>
              <span class="score-text">本表总分：</span>
              <span class="score-value">{{ calculateTableScore(group) }}</span>
            </div>
          </div>

          <div class="data-list">
            <div v-for="data in group.dataList" :key="data.id" class="data-item">
              <div class="data-item-header">
                <span class="data-id">#{{ data.id }}</span>
                <span class="data-date">{{ formatDate(data.createdAt) }}</span>
                <!-- 管理员显示分数，普通用户显示状态 -->
                <span v-if="isAdmin && data.score !== null" class="data-score">{{ data.score }} 分</span>
                <span v-else :class="['status-badge', getStatusClass(data.status)]">
                  {{ getStatusText(data.status) }}
                </span>
              </div>
              <div class="data-content">
                <div v-for="(value, key) in data.dataContent" :key="key" class="data-field">
                  <span class="field-label">{{ key }}:</span>
                  <span class="field-value">{{ value }}</span>
                </div>
              </div>
              <div v-if="data.reviewMaterial" class="data-attachment">
                <span class="attachment-icon">📎</span>
                <span class="attachment-link" @click="openAttachment(data.reviewMaterial)">
                  {{ getAttachmentName(data.reviewMaterial) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import {ElMessage} from 'element-plus';
import {type DataByTable, getMyDataStatistics} from '@/api/myData';
import {useUserStore} from '@/store/user';

interface Statistics {
  totalCount: number;
  tableCount: number;
  totalScore: number;
  avgScore: string;
}

export default defineComponent({
  name: 'MyData',
  data() {
    return {
      loading: false,
      dataByTable: [] as DataByTable[],
      statistics: {
        totalCount: 0,
        tableCount: 0,
        totalScore: 0,
        avgScore: '0'
      } as Statistics
    };
  },
  computed: {
    // 判断是否是管理员
    isAdmin(): boolean {
      const userStore = useUserStore();
      return userStore.hasAnyPermission(['table:data:admin-field', 'table:data:score']);
    }
  },
  mounted() {
    this.loadMyData();
  },
  methods: {
    async loadMyData() {
      this.loading = true;
      try {
        const response = await getMyDataStatistics();
        
        // 更新统计数据
        this.statistics = {
          totalCount: response.totalCount,
          tableCount: response.tableCount,
          totalScore: response.totalScore,
          avgScore: response.avgScore.toFixed(2)
        };
        
        // 更新数据列表
        this.dataByTable = response.dataByTable;
        
      } catch (error) {
        console.error('加载数据失败:', error);
        ElMessage.error('加载数据失败');
      } finally {
        this.loading = false;
      }
    },

    formatDate(dateStr: string): string {
      if (!dateStr) return '-';
      const date = new Date(dateStr);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    getAttachmentUrl(path: string): string {
      if (!path) return '';
      // 如果是完整URL，直接返回
      if (path.startsWith('http')) {
        return path;
      }
      // 构建完整URL，使用与 table-detail.vue 相同的方式
      const apiTarget = (import.meta as any).env?.VITE_API_TARGET || 'http://localhost:10001';
      const baseApi = (import.meta as any).env?.VITE_BASE_API || '/api/v1';
      const attachmentPath = (import.meta as any).env?.VITE_ATTACHMENT_BASE_URL || 'attachments/';
      
      const baseUrl = `${apiTarget}${baseApi}/${attachmentPath.replace(/^\/|\/$/g, '')}/`;
      return `${baseUrl}${path}`;
    },

    // 计算某个表格的总分
    calculateTableScore(group: DataByTable): string {
      const total = group.dataList.reduce((sum, data) => {
        return sum + (data.score || 0);
      }, 0);
      return total.toFixed(2);
    },

    // 打开附件
    openAttachment(filePath: string) {
      if (!filePath) return;
      
      const fullUrl = this.getAttachmentUrl(filePath);
      window.open(fullUrl, '_blank');
    },

    // 获取附件名称
    getAttachmentName(filePath: string): string {
      if (!filePath) return '';
      
      // 从路径中提取文件名
      const parts = filePath.split('/');
      const filename = parts[parts.length - 1];
      
      if (!filename) return '查看附件';
      
      // 如果文件名包含UUID前缀，去掉它
      const match = filename.match(/^[a-f0-9]{32}_(.+)$/);
      return match && match[1] ? match[1] : filename;
    },

    // 获取状态文本
    getStatusText(status?: number): string {
      if (status === undefined) return '未知';
      const statusMap: Record<number, string> = {
        0: '未提交',
        1: '已提交',
        2: '审核通过'
      };
      return statusMap[status] || '未知';
    },

    // 获取状态样式类
    getStatusClass(status?: number): string {
      if (status === undefined) return '';
      const classMap: Record<number, string> = {
        0: 'status-draft',      // 灰色
        1: 'status-submitted',  // 蓝色
        2: 'status-scored'      // 绿色
      };
      return classMap[status] || '';
    }
  }
});
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.my-data-container {
  padding: $spacing-xxl;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: $spacing-xxl;
}

.page-title {
  font-size: $font-size-xxxl;
  color: $text-primary;
  margin-bottom: $spacing-sm;
  font-weight: $font-weight-bold;
}

.page-subtitle {
  font-size: $font-size-lg;
  color: $text-secondary;
}

// 统计卡片
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: $spacing-xl;
  margin-bottom: $spacing-xxxl;
}

.stat-card {
  background: linear-gradient(135deg, $primary-color 0%, $secondary-color 100%);
  border-radius: $border-radius-large;
  padding: $spacing-xl;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  box-shadow: 0 4px 12px $shadow-light;
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px $shadow-medium;
  }
}

.stat-icon {
  font-size: 48px;
  line-height: 1;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: $font-size-xxxl;
  font-weight: $font-weight-bold;
  color: $background-primary;
  margin-bottom: $spacing-xs;
}

.stat-label {
  font-size: $font-size-md;
  color: rgba($background-primary, 0.9);
}

// 数据列表部分
.data-by-table {
  background: $background-primary;
  border-radius: $border-radius-large;
  padding: $spacing-xxl;
  box-shadow: 0 2px 8px $shadow-light;
}

.section-title {
  font-size: $font-size-xxl;
  color: $text-primary;
  margin-bottom: $spacing-xl;
  font-weight: $font-weight-bold;
}

// 加载状态
.loading-container {
  text-align: center;
  padding: $spacing-xxxl;
  color: $text-secondary;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid $border-color;
  border-top-color: $primary-color;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto $spacing-lg;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 空状态
.empty-state {
  text-align: center;
  padding: $spacing-xxxl;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: $spacing-lg;
}

.empty-text {
  font-size: $font-size-xl;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
}

.empty-hint {
  font-size: $font-size-md;
  color: $text-muted;
}

// 表格分组
.table-groups {
  display: flex;
  flex-direction: column;
  gap: $spacing-xxl;
}

.table-group {
  border: 1px solid $border-color;
  border-radius: $border-radius-large;
  overflow: hidden;
}

.table-group-header {
  background: linear-gradient(135deg, rgba($primary-color, 0.1) 0%, rgba($secondary-color, 0.1) 100%);
  padding: $spacing-lg $spacing-xl;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid $border-color;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.table-header-left {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex: 1;
}

.table-name {
  font-size: $font-size-xl;
  color: $primary-color;
  font-weight: $font-weight-bold;
  margin: 0;
}

.data-count-badge {
  background: $primary-color;
  color: $background-primary;
  padding: $spacing-xs $spacing-md;
  border-radius: $border-radius;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
}

.table-score-summary {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  background: linear-gradient(135deg, $secondary-color 0%, darken($secondary-color, 10%) 100%);
  padding: $spacing-sm $spacing-lg;
  border-radius: $border-radius-large;
  box-shadow: 0 2px 8px rgba($secondary-color, 0.3);
}

.score-icon {
  font-size: $font-size-xl;
}

.score-text {
  color: $background-primary;
  font-size: $font-size-md;
  font-weight: $font-weight-medium;
}

.score-value {
  color: $background-primary;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
}

// 数据列表
.data-list {
  padding: $spacing-lg;
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.data-item {
  background: $background-secondary;
  border-radius: $border-radius;
  padding: $spacing-lg;
  border: 1px solid $border-light;
  transition: all 0.3s;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 2px 8px $shadow-light;
  }
}

.data-item-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-light;
}

.data-id {
  font-weight: $font-weight-bold;
  color: $primary-color;
  font-size: $font-size-md;
}

.data-date {
  color: $text-muted;
  font-size: $font-size-sm;
}

.data-score {
  margin-left: auto;
  background: linear-gradient(135deg, $primary-color 0%, $secondary-color 100%);
  color: $background-primary;
  padding: $spacing-xs $spacing-md;
  border-radius: $border-radius;
  font-weight: $font-weight-bold;
  font-size: $font-size-sm;
}

// 状态标签样式
.status-badge {
  margin-left: auto;
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: $font-size-sm;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
}

.status-draft {
  background: #95a5a6;
  color: white;
}

.status-submitted {
  background: #3498db;
  color: white;
}

.status-scored {
  background: #27ae60;
  color: white;
}

.data-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.data-field {
  display: flex;
  gap: $spacing-sm;
}

.field-label {
  color: $text-secondary;
  font-weight: $font-weight-medium;
  font-size: $font-size-sm;
  min-width: 80px;
}

.field-value {
  color: $text-primary;
  font-size: $font-size-sm;
  flex: 1;
}

.data-attachment {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding-top: $spacing-sm;
  border-top: 1px solid $border-light;
}

.attachment-icon {
  font-size: $font-size-lg;
}

.attachment-link {
  color: $primary-color;
  text-decoration: underline;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: $font-weight-medium;

  &:hover {
    color: $secondary-color;
    text-decoration: underline;
    transform: translateX(2px);
  }
}

// 响应式
@include mobile {
  .my-data-container {
    padding: $spacing-lg;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .data-content {
    grid-template-columns: 1fr;
  }

  .table-group-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-header-left {
    width: 100%;
    flex-wrap: wrap;
  }

  .table-score-summary {
    width: 100%;
    justify-content: center;
  }
}
</style>

