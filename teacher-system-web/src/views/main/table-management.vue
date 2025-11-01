<template v-if="$route.name === 'TableManagement'">
    <section class="table-management-content flex-1 flex-column">
        <div class="table-meta-editor">
            <div class="table-meta-row">
                <label class="meta-label">表格全称 <span class="meta-required">*</span></label>
                <input type="text" class="meta-input" v-model="tableMeta.fullName" maxlength="20"
                    placeholder="请输入表格全称" />
            </div>
            <div class="table-meta-row">
                <label class="meta-label">表格别名 <span class="meta-required">*</span></label>
                <input type="text" class="meta-input" v-model="tableMeta.alias" maxlength="16"
                    placeholder="请输入别名（唯一标识）" />
            </div>
            <div class="table-meta-row">
                <label class="meta-label">可见成员</label>
                <div class="visibleto-chips">
                    <span v-for="role in visibleRoles" :key="role.val" class="visible-chip"
                        :class="{ selected: tableMeta.visibleTo.includes(role.val) }"
                        @click="toggleVisibleTo(role.val)">
                        {{ role.label }}
                    </span>
                </div>
            </div>

        </div>
        <div class="table-structure-editor">
            <div class="editor-header flex-between">
                <span class="editor-title">表结构自定义</span>
                <button class="add-field-btn" @click="openAddDialog">+ 添加字段</button>
            </div>
            <div class="fields-list">
                <span v-for="(field, idx) in fieldList" :key="field.name" class="field-chip"
                    :class="{ 'chip-disabled': !field.editable }" @click="openEditDialog(idx, $event)">
                    {{ field.name }}
                    <small class="field-type-tag" :class="{ 'chip-disabled': !field.editable }">{{
                        field.type === 'content' ? '内容' : '计算' }}</small>
                    <span v-if="!field.editable" class="field-non-edit" title="仅管理员可操作">🔒</span>
                    <span class="chip-delete-btn" title="删除" @click.stop="removeField(idx)">×</span>
                </span>
            </div>
            <div v-if="showDialog" class="field-dialog-bg">
                <div class="field-dialog">
                    <h4 class="dialog-title">{{ dialogMode === 'edit' ? '编辑字段' : '添加新字段' }}</h4>
                    <label class="dialog-label">字段名</label>
                    <input class="dialog-input" v-model="fieldForm.name" placeholder="字段名，如：地址" maxlength="10" />
                    <label class="dialog-label">字段类型</label>
                    <div class="custom-select" ref="selectEl" :class="{ open: selOpen }" @click="selOpen = !selOpen">
                        <div class="select-value">
                            {{ fieldForm.type === 'content' ? '内容字段' : '计算字段' }}
                            <span class="select-arrow">⌄</span>
                        </div>
                        <ul v-if="selOpen" class="custom-select-dropdown">
                            <li :class="{ selected: fieldForm.type === 'content' }"
                                @click.stop="changeFieldType('content')">内容字段</li>
                            <li :class="{ selected: fieldForm.type === 'calc' }" @click.stop="changeFieldType('calc')">
                                计算字段</li>
                        </ul>
                    </div>
                    <div v-if="fieldForm.type === 'calc'" class="depends-section">
                        <div class="dialog-label depends-label">依赖内容字段 <span class="meta-required">*</span></div>
                        <div class="depends-chips">
                            <span v-for="f in contentFieldList" :key="f.name" class="depends-chip"
                                :class="{ selected: fieldForm.dependsOn?.includes(f.name) }"
                                @click.stop="toggleDepends(f.name)">
                                {{ f.name }}
                            </span>
                            <span v-if="!contentFieldList.length" class="no-content-tip">无内容字段可选</span>
                        </div>
                    </div>
                    <div class="dialog-checkbox-row">
                        <label>
                            <input type="checkbox" v-model="fieldForm.editable" />
                            <span>允许普通用户操作</span>
                        </label>
                    </div>
                    <div class="dialog-actions flex-between">
                        <button class="btn-outline" @click="resetDialog">取消</button>
                        <button class="btn-primary" :disabled="!isValidFieldName || disableCalcFieldAdd"
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
            <table class="table-preview-minimal">
                <thead>
                    <tr>
                        <th v-for="field in fieldList" :key="field.name" :class="{ 'th-disabled': !field.editable }">
                            <span>{{ field.name }}</span>
                            <small class="th-type">({{ field.type === 'content' ? '内容' : '计算' }})</small>
                            <span v-if="field.type === 'calc' && field.dependsOn && field.dependsOn.length"
                                class="th-depends">依赖: {{ field.dependsOn.join('、') }}</span>
                            <span v-if="!field.editable" class="th-non-edit" title="仅管理员可操作">🔒</span>
                        </th>
                    </tr>
                </thead>
            </table>
        </div>
    </section>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import TableLayout from '@/layouts/TableLayout.vue';

// 类型定义
interface FieldDef {
    name: string;
    type: 'content' | 'calc';
    editable: boolean;
    dependsOn?: string[];
}
interface TableMeta {
    fullName: string;
    alias: string;
    visibleTo: string[];
}
interface VisibleRole {
    val: string;
    label: string;
}

export default defineComponent({
    name: 'Home',
    components: { TableLayout },
    data() {
        return {
            // 表格属性
            tableMeta: {
                fullName: '',
                alias: '',
                visibleTo: ['admin', 'member']
            } as TableMeta,
            visibleRoles: [
                { val: 'admin', label: '管理员' },
                { val: 'member', label: '普通成员' }
            ] as VisibleRole[],

            // 字段定义
            fieldList: [
                { name: '姓名', type: 'content', editable: true },
                { name: '年龄', type: 'calc', editable: true, dependsOn: ['姓名'] },
                { name: '手机号', type: 'content', editable: false }
            ] as FieldDef[],

            // 字段编辑/弹窗状态
            showDialog: false,
            showFieldExists: false,
            dialogMode: 'add' as 'add' | 'edit',
            editingFieldIndex: -1,
            selOpen: false,
            fieldForm: {
                name: '',
                type: 'content',
                editable: true,
                dependsOn: [] as string[]
            } as FieldDef,
            selectEl: null as HTMLElement | null
        };
    },
    computed: {
        contentFieldList(): FieldDef[] {
            return this.fieldList.filter(f => f.type === 'content');
        },
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
        disableCalcFieldAdd(): boolean {
            return (
                this.fieldForm.type === 'calc' &&
                (!this.fieldForm.dependsOn || !this.fieldForm.dependsOn.length)
            );
        }
    },
    mounted() {
        document.addEventListener('click', this.onDocClick as any);
    },
    beforeUnmount() {
        document.removeEventListener('click', this.onDocClick as any);
    },
    methods: {
        toggleVisibleTo(val: string): void {
            const idx = this.tableMeta.visibleTo.indexOf(val);
            if (idx >= 0 && this.tableMeta.visibleTo.length > 1) {
                this.tableMeta.visibleTo.splice(idx, 1);
            } else if (idx < 0) {
                this.tableMeta.visibleTo.push(val);
            }
        },
        changeFieldType(type: 'content' | 'calc'): void {
            this.fieldForm.type = type;
            this.selOpen = false;
            if (type !== 'calc') this.fieldForm.dependsOn = [];
        },
        toggleDepends(name: string): void {
            const arr = this.fieldForm.dependsOn || [];
            if (arr.includes(name)) {
                this.fieldForm.dependsOn = arr.filter((v: string) => v !== name);
            } else {
                this.fieldForm.dependsOn = [...arr, name];
            }
        },
        openAddDialog(): void {
            this.dialogMode = 'add';
            this.showDialog = true;
            this.showFieldExists = false;
            this.editingFieldIndex = -1;
            this.fieldForm = { name: '', type: 'content', editable: true, dependsOn: [] };
            this.selOpen = false;
        },
        openEditDialog(idx: number, e: MouseEvent): void {
            this.dialogMode = 'edit';
            this.showDialog = true;
            this.editingFieldIndex = idx;
            this.showFieldExists = false;
            const field = this.fieldList[idx];
            this.fieldForm = {
                ...field,
                dependsOn: field.dependsOn ? [...field.dependsOn] : []
            };
            this.selOpen = false;
        },
        resetDialog(): void {
            this.showDialog = false;
            this.showFieldExists = false;
            this.editingFieldIndex = -1;
            this.fieldForm = { name: '', type: 'content', editable: true, dependsOn: [] };
            this.selOpen = false;
        },
        addField(): void {
            const value = this.fieldForm.name.trim();
            if (!this.isValidFieldName || this.disableCalcFieldAdd) return;
            this.fieldList.push({ ...this.fieldForm, dependsOn: [...(this.fieldForm.dependsOn || [])] });
            this.resetDialog();
        },
        confirmEdit(): void {
            if (!this.isValidFieldName || this.editingFieldIndex < 0 || this.disableCalcFieldAdd) return;
            this.fieldList[this.editingFieldIndex] = {
                ...this.fieldForm,
                dependsOn: [...(this.fieldForm.dependsOn || [])]
            };
            this.resetDialog();
        },
        removeField(idx: number): void {
            this.fieldList.splice(idx, 1);
            if (this.editingFieldIndex === idx) this.resetDialog();
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

        .field-type-tag {
            margin-left: $spacing-xs;
            color: $text-muted;
            font-size: $font-size-xs;
            font-weight: normal;
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

            .field-type-tag,
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

            &:last-child {
                border-bottom: none;
            }

            &:hover,
            &.selected {
                background: rgba($secondary-color, 0.08);
                color: $secondary-color;
            }
        }
    }
}

/* 新：内容字段多选chips */
.depends-section {
    margin-bottom: $spacing-lg;
}

.depends-label {
    margin-bottom: $spacing-xs;
    font-weight: $font-weight-medium;
}

.depends-chips {
    min-height: 32px;
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-md;
}

.depends-chip {
    display: inline-block;
    border-radius: $border-radius-small;
    border: 1.5px dashed $primary-color;
    color: var(--main-font-color);
    font-size: $font-size-md;
    padding: 2px $spacing-md;
    cursor: pointer;
    background: rgba($background-secondary, 0.85);
    transition: all 0.12s;

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

.no-content-tip {
    color: $text-muted;
    font-size: $font-size-sm;
    margin-left: $spacing-lg;
}

.table-preview-minimal .th-depends {
    display: block;
    margin-top: 2px;
    color: $secondary-color;
    font-size: $font-size-sm;
    font-weight: normal;
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