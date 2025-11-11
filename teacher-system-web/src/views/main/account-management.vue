<template>
  <div class="account-management">
    <div class="am-header">
      <h2 class="am-title">账号管理</h2>
      <div class="am-actions">
        <input v-model="keyword" class="am-search" type="text" placeholder="搜索用户名/工号" @keyup.enter="onSearch" />
        <button class="btn-search" @click="onSearch">搜索</button>
        <button class="btn-reset" @click="resetSearch">重置</button>
        <button class="btn-add" @click="openAddDialog">+ 添加账号</button>
      </div>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>
    <div v-else class="am-table-wrapper">
      <table class="am-table">
        <thead>
          <tr>
            <th>工号</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>创建时间</th>
            <th class="th-ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>
              <span v-for="(roleName, index) in user.roleNames" :key="index" class="role-tag">
                {{ roleName }}
              </span>
              <span v-if="user.roleNames.length === 0" class="role-tag-empty">无角色</span>
            </td>
            <td>{{ formatTime(user.createTime) }}</td>
            <td class="td-ops">
              <button class="op-btn edit" @click="openEditDialog(user)">修改信息</button>
              <button class="op-btn del" @click="confirmDelete(user)">删除</button>
            </td>
          </tr>
          <tr v-if="filteredUsers.length === 0">
            <td colspan="6" class="td-empty">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 添加/编辑对话框 -->
    <div v-if="dialog.visible" class="am-dialog-bg" @click.self="closeDialog">
      <div class="am-dialog">
        <div class="dlg-header">
          <h3 class="dlg-title">{{ dialog.mode === 'add' ? '添加账号' : '修改账号' }}</h3>
          <button class="dlg-close" @click="closeDialog">×</button>
        </div>
        
        <!-- 添加模式下的验证码提示 -->
        <div v-if="dialog.mode === 'add'" class="verification-notice">
          <span class="notice-icon">🔐</span>
          <span class="notice-text">创建新用户需要邮箱验证，请确保邮箱地址有效</span>
        </div>
        
        <div class="dlg-body">
          <div class="form-group">
            <label class="form-label">工号 <span class="required">*</span></label>
            <input 
              v-model="formData.id" 
              :disabled="dialog.mode === 'edit'"
              class="form-input"
              :class="{ 'input-disabled': dialog.mode === 'edit' }"
              placeholder="请输入工号"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">用户名 <span class="required">*</span></label>
            <input 
              v-model="formData.username" 
              class="form-input"
              placeholder="请输入用户名"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">
              密码 
              <span v-if="dialog.mode === 'add'" class="required">*</span>
              <span v-else class="hint">（留空则不修改）</span>
            </label>
            <input 
              v-model="formData.password" 
              type="password"
              class="form-input"
              :placeholder="dialog.mode === 'add' ? '请输入密码' : '留空则不修改密码'"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">
              邮箱
              <span v-if="dialog.mode === 'add'" class="required">*</span>
            </label>
            <input 
              v-model="formData.email" 
              type="email"
              class="form-input"
              placeholder="请输入邮箱（用于接收验证码）"
              @blur="emailInputBlurred = true"
            />
          </div>
          
          <!-- 添加验证码输入框（仅在添加模式下显示） -->
          <div v-if="dialog.mode === 'add'" class="form-group">
            <label class="form-label">邮箱验证码 <span class="required">*</span></label>
            <div class="code-input-wrapper">
              <input 
                v-model="formData.code" 
                class="form-input code-input"
                placeholder="请输入验证码"
                maxlength="6"
              />
              <button 
                type="button"
                class="btn-send-code" 
                :disabled="!canSendCode || countdown > 0"
                @click="sendVerificationCode"
              >
                {{ countdown === 0 ? '发送验证码' : `${countdown}s后重发` }}
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">角色 <span class="required">*</span></label>
            <div class="role-info-hint">
              <span class="hint-icon">ℹ️</span>
              <span class="hint-text">请选择一个角色，用户将拥有该角色的所有权限</span>
            </div>
            <div class="role-radio-group">
              <label 
                v-for="role in availableRoles" 
                :key="role.id" 
                class="role-radio"
                :class="{ 'role-selected': isRoleSelected(role.id) }"
              >
                <input 
                  type="radio" 
                  name="role"
                  :value="role.id"
                  :checked="isRoleSelected(role.id)"
                  @change="selectRole(role.id)"
                />
                <span class="radio-custom"></span>
                <div class="role-info">
                  <span class="role-name">{{ role.name }}</span>
                  <span class="role-desc">{{ getRoleDescription(role.id) }}</span>
                </div>
              </label>
            </div>
          </div>
        </div>
        <div class="dlg-actions">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-confirm" @click="handleSave">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {createUser, deleteUser, getAllUsersForManagement, updateUser, type UserManagement} from '@/api/userManagement'
import {sendCode} from '@/api/user'

interface FormData {
  id: string
  username: string
  password: string
  email: string
  code: string
  roleIds: number[]
}

interface Role {
  id: number
  name: string
}

export default defineComponent({
  name: 'AccountManagement',
  data() {
    return {
      loading: false,
      keyword: '',
      users: [] as UserManagement[],
      dialog: {
        visible: false,
        mode: 'add' as 'add' | 'edit'
      },
      formData: {
        id: '',
        username: '',
        password: '',
        email: '',
        code: '',
        roleIds: []
      } as FormData,
      availableRoles: [
        { id: 1, name: '超级管理员' },
        { id: 2, name: '管理员' },
        { id: 3, name: '普通成员' }
      ] as Role[],
      countdown: 0,
      emailInputBlurred: false
    }
  },
  computed: {
    filteredUsers(): UserManagement[] {
      const k = this.keyword.trim().toLowerCase()
      if (!k) return this.users
      return this.users.filter(u => 
        u.username.toLowerCase().includes(k) || 
        u.id.toLowerCase().includes(k)
      )
    },
    canSendCode(): boolean {
      const email = this.formData.email.trim()
      const emailRegex = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,63}$/i
      return email !== '' && emailRegex.test(email)
    }
  },
  mounted() {
    this.loadUsers()
  },
  methods: {
    async loadUsers() {
      this.loading = true
      try {
        this.users = await getAllUsersForManagement()
      } catch (error) {
        console.error('加载用户列表失败:', error)
        ElMessage.error('加载用户列表失败')
      } finally {
        this.loading = false
      }
    },
    
    onSearch() {
      // 搜索由计算属性实时处理
    },
    
    resetSearch() {
      this.keyword = ''
    },
    
    openAddDialog() {
      this.dialog = {
        visible: true,
        mode: 'add'
      }
      this.formData = {
        id: '',
        username: '',
        password: '',
        email: '',
        code: '',
        roleIds: []
      }
      this.countdown = 0
      this.emailInputBlurred = false
    },
    
    openEditDialog(user: UserManagement) {
      this.dialog = {
        visible: true,
        mode: 'edit'
      }
      this.formData = {
        id: user.id,
        username: user.username,
        password: '',
        email: user.email || '',
        roleIds: [...user.roleIds]
      }
    },
    
    closeDialog() {
      this.dialog.visible = false
    },
    
    isRoleSelected(roleId: number): boolean {
      return this.formData.roleIds.length > 0 && this.formData.roleIds[0] === roleId
    },
    
    getRoleName(roleId: number): string {
      const role = this.availableRoles.find(r => r.id === roleId)
      return role ? role.name : ''
    },
    
    getRoleDescription(roleId: number): string {
      const descriptions: Record<number, string> = {
        1: '拥有所有权限，可管理系统、表格和账号',
        2: '可编辑和审核数据，但不能管理系统',
        3: '只能查看和提交自己的数据'
      }
      return descriptions[roleId] || ''
    },
    
    selectRole(roleId: number) {
      // 单选模式：替换为新选择的角色
      this.formData.roleIds = [roleId]
    },
    
    async sendVerificationCode() {
      if (!this.canSendCode || this.countdown > 0) {
        return
      }
      
      if (!this.formData.username.trim()) {
        ElMessage.warning('请先输入用户名')
        return
      }
      
      try {
        await sendCode({
          username: this.formData.username,
          email: this.formData.email
        })
        
        ElMessage.success('验证码已发送到邮箱，请注意查收')
        
        // 开始倒计时
        this.countdown = 60
        const timer = setInterval(() => {
          this.countdown--
          if (this.countdown <= 0) {
            clearInterval(timer)
          }
        }, 1000)
      } catch (error) {
        console.error('发送验证码失败:', error)
        // 错误信息已由axios拦截器处理
      }
    },
    
    async handleSave() {
      // 验证必填字段
      if (!this.formData.id.trim()) {
        ElMessage.warning('请输入工号')
        return
      }
      if (!this.formData.username.trim()) {
        ElMessage.warning('请输入用户名')
        return
      }
      if (this.dialog.mode === 'add' && !this.formData.password.trim()) {
        ElMessage.warning('请输入密码')
        return
      }
      // 添加模式下邮箱为必填
      if (this.dialog.mode === 'add' && !this.formData.email.trim()) {
        ElMessage.warning('请输入邮箱')
        return
      }
      // 验证邮箱格式
      if (this.dialog.mode === 'add' && this.formData.email.trim()) {
        const emailRegex = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,63}$/i
        if (!emailRegex.test(this.formData.email.trim())) {
          ElMessage.warning('请输入有效的邮箱地址')
          return
        }
      }
      // 添加模式下验证码为必填
      if (this.dialog.mode === 'add' && !this.formData.code.trim()) {
        ElMessage.warning('请输入邮箱验证码')
        return
      }
      if (this.formData.roleIds.length === 0) {
        ElMessage.warning('请至少选择一个角色')
        return
      }
      
      try {
        if (this.dialog.mode === 'add') {
          await createUser({
            id: this.formData.id,
            username: this.formData.username,
            password: this.formData.password,
            email: this.formData.email || undefined,
            code: this.formData.code || undefined,
            roleIds: this.formData.roleIds
          })
          ElMessage.success('账号创建成功')
        } else {
          await updateUser({
            id: this.formData.id,
            username: this.formData.username,
            email: this.formData.email || undefined,
            roleIds: this.formData.roleIds,
            newPassword: this.formData.password || undefined
          })
          ElMessage.success('账号更新成功')
        }
        
      this.closeDialog()
        await this.loadUsers()
      } catch (error) {
        console.error('保存失败:', error)
        // 错误信息已由axios拦截器处理
      }
    },
    
    async confirmDelete(user: UserManagement) {
      try {
        await ElMessageBox.confirm(
          `确定要删除用户"${user.username}"（工号：${user.id}）吗？`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        await deleteUser(user.id)
        ElMessage.success('用户删除成功')
        await this.loadUsers()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
        }
      }
    },
    
    formatTime(time: string): string {
      if (!time) return '-'
      const date = new Date(time)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.account-management {
  width: 100%;
  padding: $spacing-xxl;
  max-width: 1400px;
  margin: 0 auto;
}

.am-header {
  @include flex-between;
  margin-bottom: $spacing-xl;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.am-title {
  color: $primary-color;
  font-size: $font-size-xxxl;
  font-weight: $font-weight-bold;
}

.am-actions {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex-wrap: wrap;
}

.am-search {
  border: 2px solid $border-color;
  border-radius: $border-radius;
  padding: $spacing-sm $spacing-lg;
  font-size: $font-size-md;
  outline: none;
  background: $background-primary;
  min-width: 220px;
  transition: border-color 0.3s;
  
  &:focus {
    border-color: $primary-color;
  }
}

.btn-search, .btn-reset, .btn-add {
  padding: $spacing-sm $spacing-xl;
  border-radius: $border-radius;
  border: none;
  cursor: pointer;
  font-size: $font-size-md;
  transition: all 0.3s;
}

.btn-search {
  background: $primary-color;
  color: $text-white;
  
  &:hover {
    background: darken($primary-color, 10%);
  }
}

.btn-reset {
  background: $background-secondary;
  color: $text-primary;
  border: 1px solid $border-color;
  
  &:hover {
    background: darken($background-secondary, 5%);
  }
}

.btn-add {
  background: $secondary-color;
  color: $text-white;
  
  &:hover {
    background: darken($secondary-color, 10%);
  }
}

.loading-state {
  text-align: center;
  padding: $spacing-xxxl;
  color: $text-secondary;
  font-size: $font-size-lg;
}

.am-table-wrapper {
  background: $background-primary;
  border-radius: $border-radius-large;
  @include shadow(2);
  padding: $spacing-xl;
  overflow-x: auto;
}

.am-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.am-table th {
  text-align: left;
  color: $primary-color;
  border-bottom: 2px solid $primary-color;
  padding: $spacing-md;
  font-weight: $font-weight-bold;
  white-space: nowrap;
}

.am-table td {
  padding: $spacing-md;
  border-bottom: 1px solid $border-light;
  color: $text-primary;
}

.am-table tbody tr {
  transition: background 0.2s;
  
  &:hover {
    background: rgba($primary-color, 0.05);
  }
}

.th-ops, .td-ops {
  text-align: right;
}

.role-tag {
  display: inline-block;
  background: linear-gradient(135deg, $primary-color 0%, $secondary-color 100%);
  color: $text-white;
  padding: $spacing-xs $spacing-sm;
  border-radius: $border-radius-small;
  font-size: $font-size-xs;
  margin-right: $spacing-xs;
  font-weight: $font-weight-medium;
}

.role-tag-empty {
  color: $text-muted;
  font-size: $font-size-sm;
}

.op-btn {
  padding: $spacing-xs $spacing-md;
  border-radius: $border-radius-small;
  margin-left: $spacing-sm;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  font-size: $font-size-sm;
}

.op-btn.edit {
  background: $secondary-color;
  color: $text-white;
  
  &:hover {
    background: darken($secondary-color, 10%);
    transform: translateY(-1px);
  }
}

.op-btn.del {
  background: $error-color;
  color: $text-white;
  
  &:hover {
    background: darken($error-color, 10%);
    transform: translateY(-1px);
  }
}

.td-empty {
  text-align: center;
  color: $text-muted;
  padding: $spacing-xxxl 0;
  font-size: $font-size-lg;
}

// 对话框样式
.am-dialog-bg {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  @include flex-center;
  z-index: 2000;
}

.am-dialog {
  width: 90%;
  max-width: 600px;
  max-height: 85vh;
  background: $background-primary;
  border-radius: $border-radius-xl;
  @include shadow(3);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dlg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-xl;
  border-bottom: 1px solid $border-color;
}

.dlg-title {
  color: $primary-color;
  font-size: $font-size-xxl;
  font-weight: $font-weight-bold;
  margin: 0;
}

.dlg-close {
  background: none;
  border: none;
  font-size: 32px;
  cursor: pointer;
  color: $text-secondary;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
  
  &:hover {
    color: $error-color;
  }
}

// 验证码提示
.verification-notice {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  margin: 0;
  background: linear-gradient(135deg, rgba($primary-color, 0.08) 0%, rgba($secondary-color, 0.05) 100%);
  border-bottom: 2px solid rgba($primary-color, 0.15);

  .notice-icon {
    font-size: 24px;
    flex-shrink: 0;
  }

  .notice-text {
    font-size: $font-size-sm;
    color: $text-secondary;
    line-height: 1.5;
  }
}

.dlg-body {
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
  
  .required {
    color: $error-color;
    margin-left: $spacing-xs;
  }
  
  .hint {
    color: $text-muted;
    font-size: $font-size-sm;
    font-weight: normal;
  }
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

.role-info-hint {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: linear-gradient(135deg, #e0f2fe 0%, #bfdbfe 100%);
  border-radius: $border-radius;
  margin-bottom: $spacing-md;
  
  .hint-icon {
    font-size: $font-size-lg;
    flex-shrink: 0;
  }
  
  .hint-text {
    color: #1e40af;
    font-size: $font-size-sm;
    line-height: 1.5;
  }
}

.role-radio-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.role-radio {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
  padding: $spacing-lg;
  border: 2px solid $border-color;
  border-radius: $border-radius-large;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  background: $background-primary;
  
  &:hover {
    background: rgba($primary-color, 0.05);
    border-color: $primary-color;
    transform: translateX(4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
  
  &.role-selected {
    background: linear-gradient(135deg, rgba($primary-color, 0.12) 0%, rgba($secondary-color, 0.12) 100%);
    border-color: $primary-color;
    box-shadow: 0 4px 16px rgba($primary-color, 0.2);
    
    .radio-custom {
      border-color: $primary-color;
      
      &::after {
        opacity: 1;
        transform: scale(1);
        background: linear-gradient(135deg, $primary-color 0%, $secondary-color 100%);
      }
    }
    
    .role-name {
      color: $primary-color;
    }
  }
  
  input[type="radio"] {
    position: absolute;
    opacity: 0;
    width: 0;
    height: 0;
    cursor: pointer;
  }
  
  .radio-custom {
    width: 22px;
    height: 22px;
    min-width: 22px;
    border: 2px solid $border-color;
    border-radius: 50%;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    background: white;
    margin-top: 2px;
    
    &::after {
      content: '';
      width: 12px;
      height: 12px;
      border-radius: 50%;
      opacity: 0;
      transform: scale(0);
      transition: all 0.3s;
    }
  }
  
  .role-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
  }
  
  .role-name {
    color: $text-primary;
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    transition: color 0.3s;
  }
  
  .role-desc {
    color: $text-secondary;
    font-size: $font-size-sm;
    line-height: 1.5;
  }
}

.code-input-wrapper {
  display: flex;
  gap: $spacing-sm;
  align-items: center;
  
  .code-input {
    flex: 1;
  }
  
  .btn-send-code {
    padding: $spacing-md $spacing-lg;
    border: none;
    border-radius: $border-radius;
    background: $secondary-color;
    color: $text-white;
    cursor: pointer;
    font-size: $font-size-md;
    white-space: nowrap;
    min-width: 120px;
    transition: all 0.3s;
    
    &:hover:not(:disabled) {
      background: darken($secondary-color, 10%);
    }
    
    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      background: $background-secondary;
      color: $text-muted;
    }
  }
}

.dlg-actions {
  @include flex-between;
  padding: $spacing-xl;
  border-top: 1px solid $border-color;
  gap: $spacing-md;
}

.btn-cancel, .btn-confirm {
  flex: 1;
  padding: $spacing-md $spacing-xl;
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
  color: $text-white;
  
  &:hover {
    background: darken($primary-color, 10%);
  }
}

@media (max-width: 992px) {
  .account-management {
    padding: $spacing-lg;
  }
  
  .am-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .am-actions {
    justify-content: flex-end;
  }
  
  .am-search {
    min-width: 100%;
  }
}
</style>
