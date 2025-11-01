<template>
  <section class="table-detail-content">
    <div class="detail-header">
      <h2 class="detail-title">{{ tableInfo.tableFullName || '加载中...' }}</h2>
      <p class="detail-subtitle">
        <span>别名：{{ tableInfo.tableAliasName }}</span>
        <span class="divider">|</span>
        <span>字段数：{{ tableInfo.fieldCount }}</span>
        <span class="divider">|</span>
        <span>创建时间：{{ formatTime(tableInfo.createTime) }}</span>
      </p>
    </div>

    <div class="table-structure-section">
      <h3 class="section-title">表结构</h3>
      <div v-if="loading" class="loading-state">加载中...</div>
      <div v-else-if="fields.length === 0" class="empty-state">暂无字段信息</div>
      <div v-else class="fields-grid">
        <div v-for="(field, index) in fields" :key="index" class="field-card">
          <div class="field-card-header">
            <span class="field-name">{{ field.fieldName }}</span>
            <span v-if="field.root" class="field-tag field-tag-admin">管理员</span>
            <span v-if="field.calc" class="field-tag field-tag-calc">计算字段</span>
          </div>
          <div class="field-card-footer">
            <span class="field-access">{{ field.root ? '仅管理员可操作' : '普通用户可操作' }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="table-data-section">
      <h3 class="section-title">表数据</h3>
      <div class="empty-state">
        <svg class="empty-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
          <line x1="3" y1="9" x2="21" y2="9"></line>
          <line x1="9" y1="21" x2="9" y2="9"></line>
        </svg>
        <p>暂无数据记录</p>
        <p class="empty-hint">数据管理功能即将上线</p>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { ElMessage } from 'element-plus';
import { getTableList, getTableFields, type TableListItem, type TableFieldDTO } from '@/api/table';

interface TableField {
  fieldName: string;
  root: boolean;
  calc: boolean;
}

export default defineComponent({
  name: 'TableDetail',
  data() {
    return {
      loading: false,
      tableInfo: {} as TableListItem,
      fields: [] as TableField[]
    };
  },
  mounted() {
    this.loadTableDetail();
  },
  methods: {
    async loadTableDetail() {
      const tableId = Number(this.$route.params.id);
      if (!tableId) {
        ElMessage.error('表格ID无效');
        this.$router.push({ name: 'HomeWelcome' });
        return;
      }

      this.loading = true;
      try {
        // 从表格列表中查找当前表格
        const tables = await getTableList();
        const table = tables.find(t => t.tableId === tableId);
        
        if (!table) {
          ElMessage.error('表格不存在');
          this.$router.push({ name: 'HomeWelcome' });
          return;
        }

        this.tableInfo = table;
        
        // 获取表格字段详情
        const fields = await getTableFields(tableId);
        this.fields = fields.map(f => ({
          fieldName: f.fieldName,
          root: f.root,
          calc: f.calc
        }));
        
      } catch (error) {
        console.error('加载表格详情失败:', error);
        ElMessage.error('加载表格详情失败');
      } finally {
        this.loading = false;
      }
    },
    formatTime(time: string | undefined): string {
      if (!time) return '-';
      return new Date(time).toLocaleString('zh-CN');
    }
  },
  watch: {
    '$route.params.id'() {
      this.loadTableDetail();
    }
  }
});
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.table-detail-content {
  padding: $spacing-huge;
  max-width: 1400px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: $spacing-xxl;
  padding-bottom: $spacing-xl;
  border-bottom: 2px solid $border-color;
}

.detail-title {
  color: $primary-color;
  font-size: $font-size-xxxl;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-sm;
}

.detail-subtitle {
  color: $text-secondary;
  font-size: $font-size-md;
  
  .divider {
    margin: 0 $spacing-md;
    color: $border-color;
  }
}

.section-title {
  color: $primary-color;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-sm;
  border-bottom: 2px solid rgba($primary-color, 0.2);
}

.table-structure-section {
  background: $background-primary;
  border-radius: $border-radius-large;
  padding: $spacing-xxl;
  margin-bottom: $spacing-xxl;
  @include shadow(2);
}

.fields-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-lg;
}

.field-card {
  background: $background-secondary;
  border: 2px solid $border-color;
  border-radius: $border-radius;
  padding: $spacing-lg;
  transition: all 0.3s;
  
  &:hover {
    border-color: $primary-color;
    box-shadow: 0 4px 12px rgba($primary-color, 0.1);
    transform: translateY(-2px);
  }
}

.field-card-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.field-name {
  color: $text-primary;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
}

.field-tag {
  padding: 2px $spacing-sm;
  border-radius: $border-radius-small;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
}

.field-tag-admin {
  background: rgba($error-color, 0.1);
  color: $error-color;
}

.field-tag-calc {
  background: rgba($secondary-color, 0.1);
  color: $secondary-color;
}

.field-card-footer {
  color: $text-muted;
  font-size: $font-size-sm;
}

.field-access {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.table-data-section {
  background: $background-primary;
  border-radius: $border-radius-large;
  padding: $spacing-xxl;
  @include shadow(2);
}

.loading-state {
  text-align: center;
  padding: $spacing-huge;
  color: $text-secondary;
  font-size: $font-size-lg;
}

.empty-state {
  text-align: center;
  padding: $spacing-huge;
  color: $text-muted;
  
  .empty-icon {
    width: 64px;
    height: 64px;
    margin: 0 auto $spacing-lg;
    opacity: 0.5;
  }
  
  p {
    margin: $spacing-sm 0;
    font-size: $font-size-lg;
  }
  
  .empty-hint {
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

@media (max-width: 992px) {
  .table-detail-content {
    padding: $spacing-lg;
  }
  
  .fields-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-title {
    font-size: $font-size-xxl;
  }
  
  .detail-subtitle {
    font-size: $font-size-sm;
    
    span {
      display: block;
      margin: $spacing-xs 0;
    }
    
    .divider {
      display: none;
    }
  }
}
</style>

