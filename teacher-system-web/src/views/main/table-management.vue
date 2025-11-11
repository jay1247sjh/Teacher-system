<template v-if="$route.name === 'TableManagement'">
    <section class="table-management-content flex-1 flex-column">
        <!-- 已创建表格列表 -->
        <div v-if="!isEditMode && existingTables.length > 0" class="existing-tables-section">
            <h3 class="section-title">📋 已创建的表格</h3>
            <p class="section-hint">点击表格进入数据管理，或使用右侧按钮编辑/删除表格</p>
            <div class="tables-list">
                <div v-for="table in existingTables" :key="table.tableId" class="table-item">
                    <div class="table-item-main" @click="goToTableDetail(table.tableId)">
                        <div class="table-icon">📄</div>
                        <div class="table-info">
                            <h4 class="table-name">{{ table.tableFullName }}</h4>
                            <p class="table-meta">别名：{{ table.tableAliasName }} | 字段数：{{ table.fieldCount }}</p>
                        </div>
                        <div class="table-arrow">→</div>
                    </div>
                    <div class="table-actions">
                        <button class="btn-edit-table" @click.stop="startEditTable(table.tableId)" title="编辑表格">
                            ✏️ 编辑
                        </button>
                        <button class="btn-delete-table" @click.stop="confirmDeleteTable(table.tableId)" title="删除表格">
                            🗑️ 删除
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 分隔标题 -->
        <div class="section-divider">
            <h3 class="section-divider-title">
                <span class="divider-icon">{{ isEditMode ? '✏️' : '➕' }}</span>
                {{ isEditMode ? '编辑表格' : '创建新表格' }}
            </h3>
            <button v-if="isEditMode" class="btn-cancel-edit" @click="cancelEdit">
                <span class="cancel-icon">←</span> 返回列表
            </button>
        </div>

        <div class="table-meta-editor">
            <div class="table-meta-row">
                <label class="meta-label">表格全称 <span class="meta-required">*</span></label>
                <input 
                    type="text" 
                    class="meta-input" 
                    :class="{ 'input-error': validationErrors.fullName }"
                    v-model="tableMeta.fullName" 
                    maxlength="64"
                    placeholder="请输入表格全称" 
                    @blur="validateFullName"
                />
            </div>
            <div v-if="validationErrors.fullName" class="error-message">{{ validationErrors.fullName }}</div>
            
            <div class="table-meta-row">
                <label class="meta-label">表格别称 <span class="meta-required">*</span></label>
                <input 
                    type="text" 
                    class="meta-input" 
                    :class="{ 'input-error': validationErrors.alias }"
                    v-model="tableMeta.alias" 
                    maxlength="32"
                    placeholder="请输入别名（唯一标识）" 
                    @blur="validateAlias"
                />
            </div>
            <div v-if="validationErrors.alias" class="error-message">{{ validationErrors.alias }}</div>
            
            <div class="table-meta-row">
                <label class="meta-label"></label>
                <button 
                    type="button"
                    class="submit-btn" 
                    :disabled="isSubmitting || isLoading || !canSubmit"
                    @click="handleSubmit"
                >
                    <span v-if="isLoading">加载中...</span>
                    <span v-else-if="isSubmitting">{{ isEditMode ? '更新中...' : '创建中...' }}</span>
                    <span v-else>{{ isEditMode ? '更新表格' : '创建表格' }}</span>
                </button>
            </div>
        </div>
        <div class="table-structure-editor">
            <div class="editor-header flex-between">
                <span class="editor-title">表结构自定义</span>
                <button type="button" class="add-field-btn" @click="openAddDialog">+ 添加字段</button>
            </div>
            <div class="fields-list">
                <span v-for="(field, idx) in fieldList" :key="field.name" class="field-chip"
                    :class="{ 'chip-disabled': !field.editable }" @click="openEditDialog(idx)">
                    {{ field.name }}
                    <span v-if="!field.editable" class="field-non-edit" title="仅管理员可操作">🔒</span>
                    <span class="chip-delete-btn" title="删除" @click.stop="removeField(idx)">×</span>
                </span>
            </div>
            <div v-if="validationErrors.fields" class="error-message" style="margin-top: 10px;">{{ validationErrors.fields }}</div>
            <div v-if="showDialog" class="field-dialog-bg">
                <div class="field-dialog">
                    <h4 class="dialog-title">{{ dialogMode === 'edit' ? '编辑字段' : '添加新字段' }}</h4>
                    <label class="dialog-label">字段名</label>
                    <input class="dialog-input" v-model="fieldForm.name" placeholder="字段名，如：地址" maxlength="10" />
                    <div class="dialog-checkbox-row">
                        <label>
                            <input type="checkbox" v-model="fieldForm.editable" />
                            <span>允许普通用户操作</span>
                        </label>
                    </div>
                    <div class="dialog-actions flex-between">
                        <button type="button" class="btn-outline" @click="resetDialog">取消</button>
                        <button type="button" class="btn-primary" :disabled="!isValidFieldName"
                            @click="dialogMode === 'edit' ? confirmEdit() : addField()">确定</button>
                    </div>
                    <div v-if="showFieldExists" class="dialog-warning">字段已存在</div>
                </div>
            </div>
        </div>
        <div class="table-structure-preview-minimal">
            <div class="table-name-header">
                <span class="table-name-main">
                    {{ tableMeta.fullName ? tableMeta.fullName : '未命名表格' }}
                </span>
                <span v-if="tableMeta.alias" class="table-name-alias">（别名：{{ tableMeta.alias }}）</span>
            </div>
            
            <!-- 有字段时显示表格预览 -->
            <table v-if="fieldList.length > 0" class="table-preview-minimal">
                <thead>
                    <tr>
                        <th v-for="field in fieldList" :key="field.name" :class="{ 'th-disabled': !field.editable }">
                            <span>{{ field.name }}</span>
                            <span v-if="!field.editable" class="th-non-edit" title="仅管理员可操作">🔒</span>
                        </th>
                    </tr>
                </thead>
            </table>
            
            <!-- 无字段时显示提示 -->
            <div v-else class="empty-fields-hint">
                <div class="hint-icon">📝</div>
                <p class="hint-title">还没有添加字段</p>
                <p class="hint-desc">点击上方"<strong>+ 添加字段</strong>"按钮开始创建表格字段</p>
                <div class="hint-arrow">↑</div>
            </div>
        </div>
    </section>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  createTable,
  deleteTable,
  getTableFields,
  getTableList,
  type TableDTO,
  type TableFieldDTO,
  type TableListItem,
  updateTable
} from '@/api/table';

// 类型定义
interface FieldDef {
    name: string;
    editable: boolean;
}
interface TableMeta {
    fullName: string;
    alias: string;
}
interface ValidationErrors {
    fullName?: string;
    alias?: string;
    fields?: string;
}

export default defineComponent({
    name: 'TableManagement',
    inject: ['refreshTableList'],
    data() {
        // 尝试从 sessionStorage 恢复数据
        const savedData = sessionStorage.getItem('tableManagementFormData')
        let initialData = {
            tableMeta: { fullName: '', alias: '' } as TableMeta,
            fieldList: [] as FieldDef[]
        }
        
        if (savedData) {
            try {
                const parsed = JSON.parse(savedData)
                initialData = {
                    tableMeta: parsed.tableMeta || initialData.tableMeta,
                    fieldList: parsed.fieldList || initialData.fieldList
                }
            } catch (e) {
                console.warn('Failed to parse saved form data:', e)
            }
        }
        
        return {
            // 已有表格列表
            existingTables: [] as TableListItem[],

            // 编辑模式
            isEditMode: false,
            editingTableId: null as number | null,

            // 表格属性
            tableMeta: initialData.tableMeta,

            // 字段定义
            fieldList: initialData.fieldList,

            // 表单验证错误
            validationErrors: {
                fullName: undefined,
                alias: undefined,
                fields: undefined
            } as ValidationErrors,

            // 提交状态
            isSubmitting: false,
            isLoading: false,

            // 字段编辑/弹窗状态
            showDialog: false,
            showFieldExists: false,
            dialogMode: 'add' as 'add' | 'edit',
            editingFieldIndex: -1,
            selOpen: false,
            fieldForm: {
                name: '',
                editable: true
            } as FieldDef,
            selectEl: null as HTMLElement | null
        };
    },
    watch: {
        // 监听表单数据变化，自动保存到 sessionStorage
        tableMeta: {
            handler() {
                this.saveFormData()
            },
            deep: true
        },
        fieldList: {
            handler() {
                this.saveFormData()
            },
            deep: true
        }
    },
    computed: {
        isValidFieldName(): boolean {
            const val = this.fieldForm.name.trim();
            if (!val) return false;
            if (this.dialogMode === 'add') {
                return !this.fieldList.some(f => f.name === val);
            } else if (this.dialogMode === 'edit') {
                return !this.fieldList.some((f, i) => f.name === val && i !== this.editingFieldIndex);
            }
            return false;
        },
        canSubmit(): boolean {
            return (
                this.tableMeta.fullName.trim() !== '' &&
                this.tableMeta.alias.trim() !== '' &&
                this.fieldList.length > 0 &&
                !this.validationErrors.fullName &&
                !this.validationErrors.alias &&
                !this.validationErrors.fields
            );
        }
    },
    async mounted() {
        document.addEventListener('click', this.onDocClick as any);
        
        // 加载已有表格列表
        await this.loadExistingTables();
        
        // 检查是否是编辑模式
        const editTableId = this.$route.query.edit;
        if (editTableId) {
            this.isEditMode = true;
            this.editingTableId = Number(editTableId);
            await this.loadTableData(this.editingTableId);
        }
    },
    beforeUnmount() {
        document.removeEventListener('click', this.onDocClick as any);
    },
    methods: {
        // 加载已有表格列表
        async loadExistingTables(): Promise<void> {
            try {
                this.existingTables = await getTableList();
            } catch (error) {
                console.error('加载表格列表失败:', error);
                ElMessage.error('加载表格列表失败');
            }
        },

        // 跳转到表格详情页
        goToTableDetail(tableId: number): void {
            this.$router.push({
                name: 'TableDetail',
                params: { id: tableId }
            });
        },

        // 开始编辑表格
        async startEditTable(tableId: number): Promise<void> {
            this.isEditMode = true;
            this.editingTableId = tableId;
            await this.loadTableData(tableId);
            // 滚动到表单顶部
            window.scrollTo({ top: 0, behavior: 'smooth' });
        },

        // 取消编辑
        cancelEdit(): void {
            this.isEditMode = false;
            this.editingTableId = null;
            this.resetForm();
        },

        // 确认删除表格
        async confirmDeleteTable(tableId: number): Promise<void> {
            const table = this.existingTables.find(t => t.tableId === tableId);
            if (!table) return;

            try {
                await ElMessageBox.confirm(
                    `确定要删除表格"${table.tableFullName}"吗？删除后将无法恢复！`,
                    '删除确认',
                    {
                        confirmButtonText: '确定删除',
                        cancelButtonText: '取消',
                        type: 'warning',
                        confirmButtonClass: 'el-button--danger'
                    }
                );

                // 执行删除
                await deleteTable(tableId);
                ElMessage.success('表格删除成功');

                // 刷新列表
                await this.loadExistingTables();

                // 调用父组件的刷新方法
                if (this.refreshTableList && typeof this.refreshTableList === 'function') {
                    (this.refreshTableList as () => void)();
                }
            } catch (error: any) {
                if (error !== 'cancel') {
                    console.error('删除表格失败:', error);
                }
            }
        },

        // 表单验证方法
        validateFullName(): void {
            const fullName = this.tableMeta.fullName.trim();
            if (!fullName) {
                this.validationErrors.fullName = '表格全称不能为空';
            } else if (fullName.length < 2) {
                this.validationErrors.fullName = '表格全称至少2个字符';
            } else if (fullName.length > 64) {
                this.validationErrors.fullName = '表格全称最多64个字符';
            } else {
                this.validationErrors.fullName = undefined;
            }
        },
        validateAlias(): void {
            const alias = this.tableMeta.alias.trim();
            if (!alias) {
                this.validationErrors.alias = '表格别称不能为空';
            } else if (alias.length < 2) {
                this.validationErrors.alias = '表格别称至少2个字符';
            } else if (alias.length > 32) {
                this.validationErrors.alias = '表格别称最多32个字符';
            } else if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(alias)) {
                this.validationErrors.alias = '别称只能包含字母、数字、下划线和中文';
            } else {
                this.validationErrors.alias = undefined;
            }
        },
        validateFields(): void {
            if (this.fieldList.length === 0) {
                this.validationErrors.fields = '至少需要添加一个字段';
            } else {
                this.validationErrors.fields = undefined;
            }
        },
        // 加载表格数据（编辑模式）
        async loadTableData(tableId: number): Promise<void> {
            this.isLoading = true;
            try {
                // 获取表格列表，找到对应的表格
                const tableList = await getTableList();
                const table = tableList.find(t => t.tableId === tableId);
                
                if (!table) {
                    ElMessage.error('表格不存在');
                    this.$router.push({ name: 'HomeWelcome' });
                    return;
                }

                // 加载表格元信息
                this.tableMeta.fullName = table.tableFullName;
                this.tableMeta.alias = table.tableAliasName;

                // 加载表格字段
                const fields = await getTableFields(tableId);
                this.fieldList = fields.map((field: TableFieldDTO) => ({
                    name: field.fieldName,
                    editable: !field.root
                }));

                ElMessage.success('表格数据加载成功');
            } catch (error: any) {
                console.error('加载表格数据失败:', error);
                ElMessage.error('加载表格数据失败');
                this.$router.push({ name: 'HomeWelcome' });
            } finally {
                this.isLoading = false;
            }
        },

        // 提交表单
        async handleSubmit(): Promise<void> {
            // 验证所有字段
            this.validateFullName();
            this.validateAlias();
            this.validateFields();

            // 检查是否有错误
            if (!this.canSubmit) {
                ElMessage.warning('请检查表单填写是否正确');
                return;
            }

            // 构造请求数据
            const tableDTO: TableDTO = {
                tableFullName: this.tableMeta.fullName.trim(),
                tableAliasName: this.tableMeta.alias.trim(),
                tableFields: this.fieldList.map(field => ({
                    root: !field.editable,  // root=true表示仅管理员可操作，即editable=false
                    fieldName: field.name
                }))
            };

            try {
                this.isSubmitting = true;
                
                if (this.isEditMode && this.editingTableId) {
                    // 更新模式
                    console.log('发送更新表格请求:', tableDTO);
                    await updateTable(this.editingTableId, tableDTO);
                    console.log('更新表格成功');
                    ElMessage.success('表格更新成功！');
                } else {
                    // 创建模式
                    console.log('发送创建表格请求:', tableDTO);
                    const result = await createTable(tableDTO);
                    console.log('创建表格成功，返回结果:', result);
                    ElMessage.success('表格创建成功！');
                }
                
                // 重置表单
                this.resetForm();
                
                // 刷新表格列表
                await this.loadExistingTables();
                
                // 调用父组件的刷新方法更新表格列表
                if (this.refreshTableList && typeof this.refreshTableList === 'function') {
                    (this.refreshTableList as () => void)();
                }
                
                // 如果是编辑模式，退出编辑模式
                if (this.isEditMode) {
                    this.isEditMode = false;
                    this.editingTableId = null;
                }
            } catch (error: any) {
                console.error('提交表格失败:', error);
                console.error('错误详情:', error.message, error.response);
                // request.ts中已经显示了错误提示，这里不需要再次提示
            } finally {
                this.isSubmitting = false;
            }
        },
        // 保存表单数据到 sessionStorage
        saveFormData(): void {
            const dataToSave = {
                tableMeta: this.tableMeta,
                fieldList: this.fieldList
            }
            sessionStorage.setItem('tableManagementFormData', JSON.stringify(dataToSave))
        },
        // 重置表单
        resetForm(): void {
            this.tableMeta.fullName = '';
            this.tableMeta.alias = '';
            this.fieldList = [];
            this.validationErrors = {
                fullName: undefined,
                alias: undefined,
                fields: undefined
            };
            // 清除保存的数据
            sessionStorage.removeItem('tableManagementFormData')
        },
        openAddDialog(): void {
            this.dialogMode = 'add';
            this.showDialog = true;
            this.showFieldExists = false;
            this.editingFieldIndex = -1;
            this.fieldForm = { name: '', editable: true };
            this.selOpen = false;
        },
        openEditDialog(idx: number): void {
            this.dialogMode = 'edit';
            this.showDialog = true;
            this.editingFieldIndex = idx;
            this.showFieldExists = false;
            const field = this.fieldList[idx];
            if (field) {
                this.fieldForm = {
                    name: field.name,
                    editable: field.editable
                };
            }
            this.selOpen = false;
        },
        resetDialog(): void {
            this.showDialog = false;
            this.showFieldExists = false;
            this.editingFieldIndex = -1;
            this.fieldForm = { name: '', editable: true };
            this.selOpen = false;
        },
        addField(): void {
            if (!this.isValidFieldName) return;
            this.fieldList.push({ 
                name: this.fieldForm.name.trim(),
                editable: this.fieldForm.editable
            });
            this.resetDialog();
            // 重新验证字段
            this.validateFields();
        },
        confirmEdit(): void {
            if (!this.isValidFieldName || this.editingFieldIndex < 0) return;
            this.fieldList[this.editingFieldIndex] = {
                name: this.fieldForm.name.trim(),
                editable: this.fieldForm.editable
            };
            this.resetDialog();
        },
        removeField(idx: number): void {
            this.fieldList.splice(idx, 1);
            if (this.editingFieldIndex === idx) this.resetDialog();
            // 重新验证字段
            this.validateFields();
        },
        onDocClick(e: MouseEvent): void {
            if (this.selOpen && this.selectEl && !this.selectEl.contains(e.target as Node)) {
                this.selOpen = false;
            }
        }
    }
});
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

// ==================== 已创建表格列表 ====================
.existing-tables-section {
  background: linear-gradient(135deg, rgba($primary-color, 0.03) 0%, rgba($secondary-color, 0.02) 100%);
  border-radius: $border-radius-large;
  padding: $spacing-xxl;
  margin-bottom: $spacing-xxl;
  box-shadow: 0 4px 16px rgba($primary-color, 0.08);
  border: 1px solid rgba($primary-color, 0.1);

  .section-title {
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
    color: $primary-color;
    margin: 0 0 $spacing-sm 0;
    display: flex;
    align-items: center;
    gap: $spacing-sm;

    &::before {
      content: '';
      width: 4px;
      height: 28px;
      background: linear-gradient(135deg, $primary-color 0%, $secondary-color 100%);
      border-radius: 2px;
    }
  }

  .section-hint {
    font-size: $font-size-md;
    color: $text-secondary;
    margin: 0 0 $spacing-xl 0;
    padding-left: 16px;
  }
}

.tables-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.table-item {
  background: white;
  border: 2px solid $border-color;
  border-radius: $border-radius-large;
  padding: $spacing-xl;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: linear-gradient(135deg, $primary-color 0%, $secondary-color 100%);
    transform: scaleY(0);
    transition: transform 0.3s;
  }

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 8px 24px rgba($primary-color, 0.15);
    transform: translateY(-4px);

    &::before {
      transform: scaleY(1);
    }
  }

  .table-item-main {
    flex: 1;
    display: flex;
    align-items: center;
    gap: $spacing-md;
    cursor: pointer;
    min-width: 0;
  }

  .table-icon {
    font-size: 42px;
    flex-shrink: 0;
    filter: drop-shadow(0 2px 4px rgba($primary-color, 0.2));
  }

  .table-info {
    flex: 1;
    min-width: 0;
  }

  .table-name {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $text-primary;
    margin: 0 0 $spacing-xs 0;
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  .table-meta {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin: 0;
    display: flex;
    align-items: center;
    gap: $spacing-md;

    &::before {
      content: '📊';
      font-size: 14px;
    }
  }

  .table-arrow {
    font-size: $font-size-xxl;
    color: $primary-color;
    transition: all 0.3s;
    opacity: 0.6;
  }

  &:hover .table-arrow {
    transform: translateX(8px);
    opacity: 1;
  }

  .table-actions {
    display: flex;
    gap: $spacing-md;
    flex-shrink: 0;

    button {
      padding: $spacing-md $spacing-xl;
      border: none;
      border-radius: $border-radius-large;
      cursor: pointer;
      font-size: $font-size-md;
      font-weight: $font-weight-semibold;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      white-space: nowrap;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 0;
        height: 0;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.3);
        transform: translate(-50%, -50%);
        transition: width 0.6s, height 0.6s;
      }

      &:hover::before {
        width: 300px;
        height: 300px;
      }

      &.btn-edit-table {
        background: linear-gradient(135deg, $secondary-color 0%, darken($secondary-color, 5%) 100%);
        color: white;
        box-shadow: 0 2px 8px rgba($secondary-color, 0.2);

        &:hover {
          background: linear-gradient(135deg, darken($secondary-color, 5%) 0%, darken($secondary-color, 10%) 100%);
          transform: translateY(-2px);
          box-shadow: 0 4px 16px rgba($secondary-color, 0.4);
        }
      }

      &.btn-delete-table {
        background: linear-gradient(135deg, $error-color 0%, darken($error-color, 5%) 100%);
        color: white;
        box-shadow: 0 2px 8px rgba($error-color, 0.2);

        &:hover {
          background: linear-gradient(135deg, darken($error-color, 5%) 0%, darken($error-color, 10%) 100%);
          transform: translateY(-2px);
          box-shadow: 0 4px 16px rgba($error-color, 0.4);
        }
      }

      &:active {
        transform: translateY(0);
      }
    }
  }
}

// ==================== 分隔标题 ====================
.section-divider {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-lg 0;
  margin-bottom: $spacing-xl;
  border-bottom: 3px solid $primary-color;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: -3px;
    left: 0;
    width: 120px;
    height: 3px;
    background: $secondary-color;
  }

  .section-divider-title {
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
    color: $primary-color;
    margin: 0;
    display: flex;
    align-items: center;
    gap: $spacing-sm;

    .divider-icon {
      font-size: $font-size-xxxl;
    }
  }

  .btn-cancel-edit {
    padding: $spacing-sm $spacing-xl;
    background: white;
    color: $text-primary;
    border: 2px solid $border-color;
    border-radius: $border-radius-large;
    cursor: pointer;
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    gap: $spacing-xs;

    .cancel-icon {
      font-size: $font-size-lg;
      transition: transform 0.3s;
    }

    &:hover {
      background: $primary-color;
      border-color: $primary-color;
      color: white;
      transform: translateX(-4px);
      box-shadow: 0 4px 12px rgba($primary-color, 0.3);

      .cancel-icon {
        transform: translateX(-2px);
      }
    }
  }
}

:root {
    --main-font-color: #222;
}

body,
.main-layout,
.table-management-content,
.table-structure-editor,
.editor-header,
.editor-title,
.fields-list,
.field-chip,
.table-preview-minimal,
th,
.dialog-title,
.dialog-label,
.dialog-input,
.dialog-select,
.btn-primary,
.btn-outline,
.user-details,
.user-avatar {
    color: var(--main-font-color) !important;
}

.main-layout {
    display: flex;
    min-height: 100vh;
    background: $background-secondary !important;
    font-family: $font-family-primary;
}

.left-tree {
    width: 260px;
    background: $background-primary;
    border-right: 1px solid $border-color;
    @include shadow(2);
    @include flex-column;
    padding: $spacing-xl $spacing-lg;
    min-height: 100vh;
    position: relative;
}

.bg-secondary {
    background: $background-secondary !important;
}

.bg-white {
    background: $background-primary !important;
}

.shadow-lg {
    @include shadow(3);
}

.flex-column {
    @include flex-column;
}

.flex-column-center {
    @include flex-column-center;
}

.flex-center {
    @include flex-center;
}

.flex-between {
    @include flex-between;
}

.flex-1 {
    flex: 1 1 0%;
    min-width: 0;
}

.tree-container {
    flex: 1;
}

.tree-container ul {
    list-style: none;
    padding-left: $spacing-lg;
    margin: 0;
}

details summary {
    list-style: none;
    outline: none;
    cursor: pointer;
    user-select: none;
}

.tree-title.clickable,
.tree-leaf.clickable {
    cursor: pointer;

    &:hover {
        color: $primary-color;
        background: $background-secondary;
        border-radius: $border-radius-small;
    }
}

.tree-title {
    font-weight: $font-weight-semibold;
    color: $primary-color;
    padding: $spacing-xs 0;
    transition: background-color 0.15s;
}

.tree-leaf {
    color: $text-secondary;
    font-size: $font-size-md;
    margin-left: $spacing-md;
    padding: $spacing-xs 0 $spacing-xs $spacing-sm;
    border-radius: $border-radius-small;
    transition: background-color 0.2s, color 0.2s;
}

.user-info {
    margin-top: $spacing-xxl;
    padding-top: $spacing-xl;
    border-top: 1px solid $border-color;
    text-align: center;
}

.avatar-wrapper {
    @include flex-center;
    margin-bottom: $spacing-md;
}

.user-avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: linear-gradient(135deg, $primary-color 60%, $secondary-color 100%);
    color: $text-white;
    font-size: $font-size-xxxl;
    font-weight: $font-weight-bold;
    @include flex-center;
    box-shadow: 0 4px 12px rgba($primary-color, 0.09);
    transition: box-shadow 0.2s;
}

.user-details {
    margin-top: $spacing-xs;

    .user-name {
        display: block;
        font-size: $font-size-lg;
        color: $primary-color;
        font-weight: $font-weight-medium;
        margin-bottom: $spacing-xs;
    }

    .user-id {
        display: block;
        color: $text-secondary;
        font-size: $font-size-md;
    }
}

/* 右侧上下两部分样式 */
.table-management-content {
    @include flex-column;
    background: transparent;
    padding: $spacing-huge $spacing-huge;
    height: 100%;

    >* {
        margin-bottom: $spacing-huge;
    }
}

.table-structure-editor {
    background: $background-primary;
    border-radius: $border-radius;
    box-shadow: 0 2px 8px $shadow-light;
    padding: $spacing-xxl $spacing-xl $spacing-xl $spacing-xl;
    min-height: 110px;
    margin-bottom: $spacing-xl;
}

.editor-header {
    margin-bottom: $spacing-md;
}

.editor-title {
    font-size: $font-size-xl;
    color: $primary-color;
    font-weight: $font-weight-medium;
}

.add-field-btn {
    padding: $spacing-sm $spacing-lg;
    border-radius: $border-radius;
    font-size: $font-size-md;
    background: $primary-color;
    color: $text-white;
    border: none;
    cursor: pointer;
    @include shadow(1);
    transition: background 0.2s, box-shadow 0.2s;

    &:hover {
        background: $secondary-color;
        box-shadow: 0 6px 24px $shadow-light;
    }
}

.fields-list {
    margin-top: $spacing-md;
    position: relative;
    min-height: 38px;

    .field-chip {
        display: inline-block;
        background: $background-secondary;
        color: var(--main-font-color);
        border-radius: $border-radius-small;
        padding: 3px $spacing-md 3px $spacing-md;
        margin-right: $spacing-md;
        margin-bottom: $spacing-sm;
        font-size: $font-size-md;
        font-weight: $font-weight-medium;
        border: 1px solid $primary-color;
        transition: background 0.15s;
        position: relative;
        cursor: pointer !important;

        &:hover {
            background: $primary-color;
            color: $text-white;
        }

        .field-non-edit {
            margin-left: $spacing-xs;
            color: $error-color;
            font-size: $font-size-sm;
        }

        .chip-delete-btn {
            display: none;
            position: absolute;
            top: -10.5px;
            right: -11px;
            font-size: 1.1em;
            color: $error-color;
            cursor: pointer;
            background: $background-primary;
            border-radius: 50%;
            line-height: 1;
            width: 22px;
            height: 22px;
            text-align: center;
            padding: 0;
            border: none;

            &:hover {
                background: $error-color;
                color: $text-white;
            }
        }

        &:hover .chip-delete-btn {
            display: inline-block;
        }

        &.chip-disabled {
            color: $text-muted;
            background: rgba($secondary-color, 0.13);
            border-color: $border-color;

            .field-non-edit,
            .chip-delete-btn {
                color: $error-color !important;
            }
        }
    }
}

.table-structure-preview {
    background: $background-primary;
    border-radius: $border-radius;
    box-shadow: 0 2px 8px $shadow-medium;
    padding: $spacing-xl $spacing-xl $spacing-xxl $spacing-xl;
}

.table-preview {
    width: 100%;
    border-collapse: collapse;
    font-size: $font-size-md;

    thead th {
        background: $background-secondary;
        color: $primary-color;
        font-weight: $font-weight-semibold;
        padding: $spacing-md 0;
        border-bottom: 2px solid $primary-color;
    }

    tbody td {
        color: $text-primary;
        padding: $spacing-md 0;
        border-bottom: 1px solid $border-light;
        text-align: center;
    }

    tr:last-child td {
        border-bottom: none;
    }
}

/* Dialog 弹窗 */
.field-dialog-bg {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.18);
    z-index: 10000;
    @include flex-center;
}

.field-dialog {
    min-width: 320px;
    max-width: 90vw;
    background: $background-primary;
    border-radius: $border-radius-large;
    @include shadow(3);
    padding: $spacing-xxl $spacing-xl $spacing-xl $spacing-xl;
    text-align: left;
    animation: fadein 0.18s;
}

.dialog-title {
    color: $primary-color;
    font-weight: $font-weight-bold;
    font-size: $font-size-xl;
    margin-bottom: $spacing-lg;
}

.dialog-input {
    width: 100%;
    padding: $spacing-sm $spacing-xl;
    border: 1.5px solid $border-color;
    border-radius: $border-radius;
    font-size: $font-size-lg;
    margin-bottom: $spacing-lg;
    outline: none;

    &:focus {
        border-color: $primary-color;
    }
}

.dialog-select {
    width: 100%;
    padding: $spacing-sm $spacing-md;
    border: 2px solid $primary-color;
    border-radius: $border-radius-large;
    font-size: $font-size-lg;
    margin-bottom: $spacing-lg;
    background: $background-light;
    outline: none;
    color: var(--main-font-color) !important;
    transition: border 0.18s, box-shadow 0.18s;
    box-shadow: 0 2px 8px rgba($secondary-color, 0.12);
    appearance: none;
    -webkit-appearance: none;
    /* for safari */
    background-image:
        linear-gradient(45deg, transparent 49%, $primary-color 50%, $primary-color 51%, transparent 52%),
        linear-gradient(135deg, transparent 49%, $primary-color 50%, $primary-color 51%, transparent 52%);
    background-position:
        calc(100% - 22px) calc(50% - 5px),
        calc(100% - 17px) calc(50% - 5px);
    background-size: 5px 5px, 5px 5px;
    background-repeat: no-repeat;
}

.dialog-select:focus {
    border-color: $secondary-color;
    box-shadow: 0 0 0 2px rgba($primary-color, 0.09);
}

.dialog-select option {
    color: var(--main-font-color);
    border-radius: $border-radius-large;
    padding: $spacing-sm $spacing-lg;
}

.dialog-checkbox-row {
    margin-bottom: $spacing-lg;

    label {
        user-select: none;
        font-size: $font-size-md;
        color: $primary-color;
        cursor: pointer;
    }

    input[type="checkbox"] {
        accent-color: $primary-color;
        margin-right: $spacing-xs;
    }
}

.dialog-actions {
    gap: $spacing-xl;
}

.btn-primary {
    @include button-primary;
    padding: $spacing-sm $spacing-xl;
    font-size: $font-size-md;
    border-radius: $border-radius;
}

.btn-outline {
    @include button-outline;
    padding: $spacing-sm $spacing-xl;
    font-size: $font-size-md;
    border-radius: $border-radius;
}

.dialog-warning {
    color: $error-color;
    font-size: $font-size-md;
    margin-top: $spacing-md;
}

@keyframes fadein {
    from {
        opacity: 0;
        transform: scale(1.06);
    }

    to {
        opacity: 1;
        transform: scale(1);
    }
}

// 仅填充主要新表格UI部分，其它内容复用保持不变
.table-structure-preview-login-theme {
    background: rgba($secondary-color, 0.07);
    border-radius: $border-radius-large;
    @include shadow(2);
    padding: $spacing-xl $spacing-xl $spacing-lg $spacing-xl;
    min-height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid rgba($primary-color, 0.20);
}

.table-preview-login-ui {
    width: auto;
    border-collapse: separate;
    border-spacing: 0;
    background: rgba($background-primary, 0.93);
    border-radius: $border-radius-large;
    box-shadow: 0 3px 12px $shadow-light;

    th {
        background: transparent;
        color: $primary-color;
        font-size: $font-size-xl;
        font-weight: $font-weight-bold;
        padding: $spacing-lg $spacing-xxl;
        border-bottom: none;
        border-right: 2px solid rgba($primary-color, 0.08);

        &:last-child {
            border-right: none;
        }

        letter-spacing: 1.5px;
        text-align: center;
        box-shadow: 0 1px 0.5px $shadow-light;
    }
}

.table-structure-preview-minimal {
    width: 90%;
    margin: 36px auto 0 auto;
    padding: 0;
    background: transparent;
    border: none;
    min-height: initial;
    display: block;
}

// ==================== 空字段提示 ====================
.empty-fields-hint {
    background: linear-gradient(135deg, rgba($secondary-color, 0.05) 0%, rgba($primary-color, 0.03) 100%);
    border: 2px dashed rgba($primary-color, 0.3);
    border-radius: $border-radius-large;
    padding: $spacing-xxl $spacing-xl;
    text-align: center;
    margin-top: $spacing-lg;
    transition: all 0.3s;

    &:hover {
        border-color: rgba($primary-color, 0.5);
        background: linear-gradient(135deg, rgba($secondary-color, 0.08) 0%, rgba($primary-color, 0.05) 100%);
    }

    .hint-icon {
        font-size: 48px;
        margin-bottom: $spacing-md;
        animation: bounce 2s infinite;
    }

    .hint-title {
        font-size: $font-size-xl;
        font-weight: $font-weight-semibold;
        color: $text-primary;
        margin: 0 0 $spacing-sm 0;
    }

    .hint-desc {
        font-size: $font-size-md;
        color: $text-secondary;
        margin: 0 0 $spacing-md 0;

        strong {
            color: $primary-color;
            font-weight: $font-weight-semibold;
        }
    }

    .hint-arrow {
        font-size: $font-size-xxxl;
        color: $primary-color;
        animation: bounce-arrow 1.5s infinite;
        margin-top: $spacing-sm;
    }
}

@keyframes bounce {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-10px);
    }
}

@keyframes bounce-arrow {
    0%, 100% {
        transform: translateY(0);
        opacity: 1;
    }
    50% {
        transform: translateY(-8px);
        opacity: 0.6;
    }
}

.table-preview-minimal {
    width: 100%;
    border-radius: $border-radius-large;
    border-collapse: separate;
    border-spacing: 0;
    background: rgba($background-primary, 0.96);
    box-shadow: none;

    th {
        background: transparent;
        color: $primary-color;
        font-size: $font-size-lg;
        font-weight: $font-weight-semibold;
        padding: $spacing-md $spacing-xxl;
        border-bottom: 2px solid $primary-color;
        border-right: 1.5px solid $border-light;
        letter-spacing: 1.1px;
        height: 40px;
        line-height: 40px;
        text-align: center;

        .th-type {
            color: $text-muted;
            font-size: $font-size-xs;
            margin-left: $spacing-xs;
        }

        .th-non-edit {
            color: $error-color;
            margin-left: $spacing-xs;
        }

        &.th-disabled {
            color: $text-muted;
            border-bottom: 2px dashed $error-color;
            background: rgba($secondary-color, 0.05);

            .th-type,
            .th-non-edit {
                color: $error-color;
            }
        }
    }

    tr {
        height: 40px;
    }
}

.table-name-header {
    text-align: center;
    margin-bottom: $spacing-md;

    .table-name-main {
        color: $primary-color;
        font-size: $font-size-xl;
        font-weight: $font-weight-bold;
        letter-spacing: 1.1px;
        margin-right: $spacing-xs;
    }

    .table-name-alias {
        color: $text-secondary;
        font-size: $font-size-md;
        font-weight: $font-weight-normal;
    }
}

/* 覆盖所有主按钮、标签字体、表单等为黑色（主色保留边框/底线高亮） */
.editor-title,
.table-preview-minimal th,
.field-chip,
.dialog-title,
.btn-primary,
.btn-outline {
    color: var(--main-font-color) !important;
}

.user-name,
.user-id {
    color: var(--main-font-color) !important;
}

.table-meta-editor {
    background: rgba($background-primary, 0.96);
    padding: $spacing-xl $spacing-xl $spacing-lg $spacing-xl;
    border-radius: $border-radius-large;
    @include shadow(1);
    margin-bottom: $spacing-xxl;
}

.table-meta-row {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-md;

    &:last-child {
        margin-bottom: 0;
    }
}

.meta-label {
    color: var(--main-font-color);
    font-size: $font-size-md;
    width: 100px;
    flex-shrink: 0;
    font-weight: $font-weight-medium;
    margin-right: $spacing-lg;
}

.meta-required {
    color: $error-color;
    font-size: $font-size-md;
    margin-left: 2px;
}

.meta-input {
    flex: 1;
    min-width: 0;
    border: 2px solid $primary-color;
    border-radius: $border-radius-large;
    padding: $spacing-sm $spacing-xl;
    font-size: $font-size-md;
    color: var(--main-font-color);
    background: $background-light;
    outline: none;
    @include shadow(1);
    transition: border 0.19s, box-shadow 0.15s;

    &:focus {
        border-color: $secondary-color;
        box-shadow: 0 0 1.5px $secondary-color;
    }
}

.meta-textarea {
    min-height: 34px;
    resize: vertical;
}

.meta-input:invalid {
    border-color: $error-color;
}

.input-error {
    border-color: $error-color !important;
    background: rgba($error-color, 0.05) !important;
}

.error-message {
    color: $error-color;
    font-size: $font-size-sm;
    margin-top: -$spacing-sm;
    margin-bottom: $spacing-md;
    margin-left: 116px;  // 对齐label宽度
}

.submit-btn {
    padding: $spacing-md $spacing-xxl;
    border-radius: $border-radius;
    font-size: $font-size-lg;
    background: $primary-color;
    color: $text-white;
    border: none;
    cursor: pointer;
    @include shadow(2);
    transition: all 0.2s;
    font-weight: $font-weight-medium;
    min-width: 140px;

    &:hover:not(:disabled) {
        background: $secondary-color;
        box-shadow: 0 6px 24px $shadow-light;
        transform: translateY(-1px);
    }

    &:active:not(:disabled) {
        transform: translateY(0);
    }

    &:disabled {
        background: $border-color;
        color: $text-muted;
        cursor: not-allowed;
        box-shadow: none;
    }
}

/* 新：自定义select皮肤 */
.custom-select {
    width: 100%;
    margin-bottom: $spacing-lg;
    border-radius: $border-radius-large;
    background: $background-light;
    border: 2px solid $primary-color;
    cursor: pointer;
    position: relative;
    color: var(--main-font-color);
    box-shadow: 0 2px 12px rgba($primary-color, 0.05);
    transition: border 0.18s;

    &.open,
    &:focus-within {
        border-color: $secondary-color;
    }

    .select-value {
        font-size: $font-size-lg;
        min-height: 38px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: $spacing-sm $spacing-xl $spacing-sm $spacing-md;
        border-radius: $border-radius-large;
        letter-spacing: 1px;

        .select-arrow {
            margin-left: $spacing-lg;
            color: $secondary-color;
            font-size: $font-size-xl;
            display: inline-block;
            pointer-events: none;
            transition: color 0.18s;
        }
    }

    .custom-select-dropdown {
        position: absolute;
        left: 0;
        right: 0;
        top: calc(100% + 2px);
        z-index: 1600;
        background: $background-primary;
        border-radius: $border-radius-large;
        box-shadow: 0 2px 16px $shadow-medium;
        margin: 0;
        padding: 0;
        border: 1.5px solid $border-light;
        overflow: hidden;

        li {
            display: block;
            padding: $spacing-lg $spacing-xl;
            font-size: $font-size-lg;
            color: $primary-color;
            background: transparent;
            cursor: pointer;
            border-bottom: 1px solid rgba($primary-color, 0.05);
            transition: all 0.15s;

            &:last-child {
                border-bottom: none;
            }

            &:hover:not(.disabled),
            &.selected:not(.disabled) {
                background: rgba($secondary-color, 0.08);
                color: $secondary-color;
            }

            &.disabled {
                color: $text-muted;
                background: rgba($border-color, 0.1);
                cursor: not-allowed;
                opacity: 0.6;
            }
        }
    }
}

.visibleto-chips {
    display: flex;
    gap: $spacing-md;
}

.visible-chip {
    display: inline-block;
    border-radius: $border-radius-small;
    border: 1.5px dashed $primary-color;
    color: var(--main-font-color);
    font-size: $font-size-md;
    padding: 2px $spacing-lg;
    background: rgba($background-secondary, 0.88);
    cursor: pointer;
    transition: all 0.13s;
    user-select: none;

    &.selected {
        background: $primary-color;
        color: $text-white;
        border-style: solid;
        font-weight: $font-weight-semibold;
    }

    &:hover {
        background: $secondary-color;
        color: $text-white;
    }
}

@media (max-width: 992px) {
    .main-layout {
        flex-direction: column;
    }

    .left-tree {
        width: 100%;
        min-height: auto;
        border-right: none;
        border-bottom: 1px solid $border-color;
        padding: $spacing-md;
    }

    .table-management-content {
        width: 100%;
        padding: $spacing-md !important;
    }

    .table-meta-editor,
    .table-structure-editor,
    .table-structure-preview-minimal {
        padding: $spacing-md !important;
        border-radius: $border-radius;
        min-width: 0;
        width: 100%;
    }

    .table-preview-minimal th,
    .fields-list,
    .meta-label,
    .meta-input,
    .field-chip,
    .depends-chip,
    .visible-chip {
        font-size: $font-size-md !important;
        padding: $spacing-xs !important;
    }

    .fields-list,
    .visibleto-chips,
    .depends-chips {
        gap: $spacing-xs;
    }
}
</style>