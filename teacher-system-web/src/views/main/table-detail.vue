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
      <div class="data-header">
        <h3 class="section-title">表数据</h3>
        <button class="btn-add" @click="openAddDialog">+ 添加数据</button>
      </div>
      
      <div v-if="dataLoading" class="loading-state">加载中...</div>
      <div v-else-if="tableData.length === 0" class="empty-state">
        <p>暂无数据记录</p>
        <p class="empty-hint">点击上方"添加数据"按钮开始录入</p>
      </div>
      <div v-else class="data-table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th class="col-index">序号</th>
              <th v-for="field in fields" :key="field.fieldName" class="col-field">
                {{ field.fieldName }}
                <span v-if="field.root" class="lock-icon" title="管理员字段">🔒</span>
              </th>
              <th class="col-score">分数</th>
              <th class="col-material">审核材料</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in tableData" :key="row.id">
              <td class="col-index">{{ index + 1 }}</td>
              <td v-for="field in fields" :key="field.fieldName" class="col-field">
                {{ row.dataContent[field.fieldName] || '-' }}
              </td>
              <td class="col-score">{{ row.score !== null ? row.score : '-' }}</td>
              <td class="col-material">{{ row.reviewMaterial || '-' }}</td>
              <td class="col-actions">
                <button class="btn-edit" @click="openEditDialog(row)">编辑</button>
                <button class="btn-delete" @click="handleDelete(row.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 添加/编辑数据对话框 -->
    <div v-if="showDataDialog" class="dialog-overlay" @click.self="closeDataDialog">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>{{ editingData ? '编辑数据' : '添加数据' }}</h3>
          <button class="dialog-close" @click="closeDataDialog">×</button>
        </div>
        <div class="dialog-body">
          <div v-for="field in fields" :key="field.fieldName" class="form-group">
            <label class="form-label">
              {{ field.fieldName }}
              <span v-if="field.root && !canEditAdminField" class="lock-icon" title="无权限编辑管理员字段">🔒</span>
            </label>
            <input 
              v-model="formData.dataContent[field.fieldName]"
              :disabled="field.root && !canEditAdminField"
              class="form-input"
              :class="{ 'input-disabled': field.root && !canEditAdminField }"
              :placeholder="`请输入${field.fieldName}`"
            />
          </div>
          <div class="form-group">
            <label class="form-label">
              分数
              <span v-if="!canSetScore" class="lock-icon" title="无权限设置分数">🔒</span>
            </label>
            <input 
              v-model.number="formData.score"
              type="number"
              step="0.01"
              :disabled="!canSetScore"
              class="form-input"
              :class="{ 'input-disabled': !canSetScore }"
              placeholder="请输入分数"
            />
          </div>
          <div class="form-group">
            <label class="form-label">审核材料</label>
            <input 
              v-model="formData.reviewMaterial"
              class="form-input"
              placeholder="审核材料URL或路径（暂未开发）"
              disabled
            />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDataDialog">取消</button>
          <button class="btn-confirm" @click="handleSave">保存</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getTableList, getTableFields, type TableListItem, type TableFieldDTO } from '@/api/table';
import { getTableData, saveTableData, deleteTableData, type TableDataItem } from '@/api/tableData';
import { useUserStore } from '@/store/user';

interface TableField {
  fieldName: string;
  root: boolean;
  calc: boolean;
}

interface FormData {
  id?: number;
  dataContent: Record<string, any>;
  score: number | null;
  reviewMaterial: string | null;
}

export default defineComponent({
  name: 'TableDetail',
  data() {
    return {
      loading: false,
      dataLoading: false,
      tableInfo: {} as TableListItem,
      fields: [] as TableField[],
      tableData: [] as TableDataItem[],
      showDataDialog: false,
      editingData: null as TableDataItem | null,
      formData: {
        dataContent: {},
        score: null,
        reviewMaterial: null
      } as FormData
    };
  },
  computed: {
    // 判断是否可以设置分数（基于权限标识）
    canSetScore(): boolean {
      const userStore = useUserStore();
      return userStore.hasPermission('table:data:score');
    },
    // 判断是否可以编辑管理员字段（基于权限标识）
    canEditAdminField(): boolean {
      const userStore = useUserStore();
      return userStore.hasPermission('table:data:admin-field');
    },
    // 获取当前用户ID
    currentUserId(): string {
      const userStore = useUserStore();
      return userStore.userId;
    }
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

        // 加载表格数据
        await this.loadTableData();
        
      } catch (error) {
        console.error('加载表格详情失败:', error);
        ElMessage.error('加载表格详情失败');
      } finally {
        this.loading = false;
      }
    },

    async loadTableData() {
      const tableId = Number(this.$route.params.id);
      if (!tableId) return;

      this.dataLoading = true;
      try {
        this.tableData = await getTableData(tableId);
      } catch (error) {
        console.error('加载表格数据失败:', error);
        ElMessage.error('加载表格数据失败');
      } finally {
        this.dataLoading = false;
      }
    },

    openAddDialog() {
      this.editingData = null;
      this.formData = {
        dataContent: {},
        score: null,
        reviewMaterial: null
      };
      // 初始化字段默认值
      this.fields.forEach(field => {
        this.formData.dataContent[field.fieldName] = '';
      });
      this.showDataDialog = true;
    },

    openEditDialog(row: TableDataItem) {
      this.editingData = row;
      this.formData = {
        id: row.id,
        dataContent: { ...row.dataContent },
        score: row.score,
        reviewMaterial: row.reviewMaterial
      };
      this.showDataDialog = true;
    },

    closeDataDialog() {
      this.showDataDialog = false;
      this.editingData = null;
    },

    async handleSave() {
      const tableId = Number(this.$route.params.id);
      if (!tableId) return;

      // 验证必填字段
      const hasEmptyField = this.fields.some(field => {
        const value = this.formData.dataContent[field.fieldName];
        return !value || value.toString().trim() === '';
      });

      if (hasEmptyField) {
        ElMessage.warning('请填写所有字段');
        return;
      }

      try {
        await saveTableData({
          id: this.formData.id,
          tableId,
          dataContent: this.formData.dataContent,
          score: this.formData.score,
          reviewMaterial: this.formData.reviewMaterial
        });

        ElMessage.success(this.editingData ? '修改成功' : '添加成功');
        this.closeDataDialog();
        await this.loadTableData();
      } catch (error) {
        console.error('保存数据失败:', error);
        // 错误信息已由 axios 拦截器处理
      }
    },

    async handleDelete(id: number) {
      try {
        await ElMessageBox.confirm('确定要删除这条数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });

        await deleteTableData(id);
        ElMessage.success('删除成功');
        await this.loadTableData();
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除数据失败:', error);
        }
      }
    },

    formatTime(time: string | undefined): string {
      if (!time) return '-';
      return new Date(time).toLocaleString('zh-CN');
    }
  },
  watch: {
    '$route.params.id'(newId) {
      // 只有当路由参数变化且存在有效ID时才重新加载
      if (newId && this.$route.name === 'TableDetail') {
        this.loadTableDetail();
      }
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

.data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.btn-add {
  background: $primary-color;
  color: white;
  border: none;
  padding: $spacing-sm $spacing-lg;
  border-radius: $border-radius;
  cursor: pointer;
  font-size: $font-size-md;
  transition: all 0.3s;

  &:hover {
    background: darken($primary-color, 10%);
    transform: translateY(-1px);
  }
}

.data-table-container {
  overflow-x: auto;
  margin-top: $spacing-lg;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-md;

  thead {
    background: $background-secondary;
    
    th {
      padding: $spacing-md;
      text-align: left;
      font-weight: $font-weight-semibold;
      color: $text-primary;
      border-bottom: 2px solid $border-color;
      white-space: nowrap;
    }
  }

  tbody {
    tr {
      transition: background 0.2s;

      &:hover {
        background: rgba($primary-color, 0.05);
      }

      &:not(:last-child) {
        border-bottom: 1px solid $border-color;
      }
    }

    td {
      padding: $spacing-md;
      color: $text-primary;
    }
  }

  .col-index {
    width: 60px;
    text-align: center;
  }

  .col-field {
    min-width: 120px;
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .col-score {
    width: 100px;
    text-align: center;
    font-weight: $font-weight-semibold;
    color: $secondary-color;
  }

  .col-material {
    width: 150px;
  }

  .col-actions {
    width: 150px;
    text-align: center;
  }
}

.lock-icon {
  font-size: $font-size-sm;
  margin-left: $spacing-xs;
  opacity: 0.7;
}

.btn-edit,
.btn-delete {
  padding: 4px $spacing-sm;
  border: none;
  border-radius: $border-radius-small;
  cursor: pointer;
  font-size: $font-size-sm;
  margin: 0 4px;
  transition: all 0.2s;
}

.btn-edit {
  background: $secondary-color;
  color: white;

  &:hover {
    background: darken($secondary-color, 10%);
  }
}

.btn-delete {
  background: $error-color;
  color: white;

  &:hover {
    background: darken($error-color, 10%);
  }
}

// 对话框样式
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
  background: $background-primary;
  border-radius: $border-radius-large;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  @include shadow(3);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-lg $spacing-xl;
  border-bottom: 1px solid $border-color;

  h3 {
    margin: 0;
    color: $primary-color;
    font-size: $font-size-xl;
  }
}

.dialog-close {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: $text-secondary;
  line-height: 1;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;

  &:hover {
    color: $error-color;
  }
}

.dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-xl;
}

.form-group {
  margin-bottom: $spacing-lg;
}

.form-label {
  display: block;
  margin-bottom: $spacing-sm;
  color: $text-primary;
  font-weight: $font-weight-medium;
  font-size: $font-size-md;
}

.form-input {
  width: 100%;
  padding: $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  font-size: $font-size-md;
  transition: border-color 0.3s;

  &:focus {
    outline: none;
    border-color: $primary-color;
  }

  &.input-disabled {
    background: $background-secondary;
    cursor: not-allowed;
    opacity: 0.6;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-md;
  padding: $spacing-lg $spacing-xl;
  border-top: 1px solid $border-color;
}

.btn-cancel,
.btn-confirm {
  padding: $spacing-sm $spacing-xl;
  border: none;
  border-radius: $border-radius;
  cursor: pointer;
  font-size: $font-size-md;
  transition: all 0.3s;
}

.btn-cancel {
  background: $background-secondary;
  color: $text-primary;

  &:hover {
    background: darken($background-secondary, 5%);
  }
}

.btn-confirm {
  background: $primary-color;
  color: white;

  &:hover {
    background: darken($primary-color, 10%);
  }
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

