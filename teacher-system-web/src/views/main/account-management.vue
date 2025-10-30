<template>
  <div class="account-management">
    <div class="am-header">
      <h2 class="am-title">账号管理</h2>
      <div class="am-actions">
        <input v-model="keyword" class="am-search" type="text" placeholder="搜索用户名/工号" />
        <button class="btn-primary" @click="onSearch">搜索</button>
        <button class="btn-outline" @click="resetSearch">重置</button>
      </div>
    </div>

    <div class="am-table-wrapper">
      <table class="am-table">
        <thead>
          <tr>
            <th>用户名</th>
            <th>工号</th>
            <th class="th-ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in filteredUsers" :key="u.empId">
            <td>{{ u.username }}</td>
            <td>{{ u.empId }}</td>
            <td class="td-ops">
              <button class="op-btn info" @click="viewUser(u)">查看</button>
              <button class="op-btn edit" @click="editUser(u)">修改信息</button>
              <button class="op-btn del" @click="removeUser(u)">删除</button>
            </td>
          </tr>
          <tr v-if="!filteredUsers.length">
            <td colspan="3" class="td-empty">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 轻量弹窗：查看/编辑占位（可后续接入真实表单） -->
    <div v-if="dialog.visible" class="am-dialog-bg" @click.self="closeDialog">
      <div class="am-dialog">
        <h3 class="dlg-title">{{ dialog.mode==='view'?'查看信息':'修改信息' }}</h3>
        <div class="dlg-body">
          <div class="dlg-row"><span>用户名：</span>{{ dialog.user?.username }}</div>
          <div class="dlg-row"><span>工号：</span>{{ dialog.user?.empId }}</div>
        </div>
        <div class="dlg-actions">
          <button class="btn-outline" @click="closeDialog">关闭</button>
          <button v-if="dialog.mode==='edit'" class="btn-primary" @click="saveUser">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

interface UserItem {
  username: string
  empId: string
}

export default defineComponent({
  name: 'AccountManagement',
  data() {
    return {
      keyword: '',
      users: [
        { username: '张三', empId: '10001' },
        { username: '李四', empId: '10002' },
        { username: '王五', empId: '10003' }
      ] as UserItem[],
      dialog: {
        visible: false,
        mode: 'view' as 'view'|'edit',
        user: null as UserItem | null
      }
    }
  },
  computed: {
    filteredUsers(): UserItem[] {
      const k = this.keyword.trim()
      if (!k) return this.users
      return this.users.filter(u => u.username.includes(k) || u.empId.includes(k))
    }
  },
  methods: {
    onSearch(): void { /* 已由计算属性实时过滤，这里保留占位 */ },
    resetSearch(): void { this.keyword = '' },
    viewUser(user: UserItem): void { this.dialog = { visible: true, mode: 'view', user } },
    editUser(user: UserItem): void { this.dialog = { visible: true, mode: 'edit', user: { ...user } } },
    removeUser(user: UserItem): void {
      this.users = this.users.filter(u => u.empId !== user.empId)
    },
    closeDialog(): void { this.dialog.visible = false },
    saveUser(): void {
      if (!this.dialog.user) return
      const idx = this.users.findIndex(u => u.empId === this.dialog.user!.empId)
      if (idx > -1) this.users.splice(idx, 1, { ...this.dialog.user })
      this.closeDialog()
    }
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.account-management { width: 100%; }

.am-header { @include flex-between; margin-bottom: $spacing-xl; }
.am-title { color: $primary-color; font-size: $font-size-xxl; }
.am-actions { @include flex-between; gap: $spacing-md; }
.am-search {
  border: 2px solid $primary-color; border-radius: $border-radius-large; padding: $spacing-sm $spacing-lg; font-size: $font-size-md; outline: none; background: $background-light; min-width: 220px;
}

.btn-primary { @include button-primary; padding: $spacing-sm $spacing-xl; border-radius: $border-radius; }
.btn-outline { @include button-outline; padding: $spacing-sm $spacing-xl; border-radius: $border-radius; }

.am-table-wrapper { background: $background-primary; border-radius: $border-radius-large; @include shadow(2); padding: $spacing-xl; }
.am-table { width: 100%; border-collapse: collapse; }
.am-table th { text-align: left; color: $primary-color; border-bottom: 2px solid $primary-color; padding: $spacing-md 0; }
.am-table td { padding: $spacing-md 0; border-bottom: 1px solid $border-light; color: $text-primary; }
.th-ops, .td-ops { text-align: right; }

.op-btn { padding: $spacing-xs $spacing-md; border-radius: $border-radius-small; margin-left: $spacing-sm; border: none; cursor: pointer; transition: .15s; }
.op-btn.info { background: $info-color; color: $text-white; }
.op-btn.edit { background: $secondary-color; color: $text-white; }
.op-btn.del { background: $error-color; color: $text-white; }
.op-btn:hover { filter: brightness(0.95); transform: translateY(-1px); }

.td-empty { text-align: center; color: $text-muted; padding: $spacing-xl 0; }

/* dialog */
.am-dialog-bg { position: fixed; inset: 0; background: rgba(0,0,0,.22); @include flex-center; z-index: 2000; }
.am-dialog { width: 90%; max-width: 520px; background: $background-primary; border-radius: $border-radius-xl; @include shadow(3); padding: $spacing-xxl; }
.dlg-title { color: $primary-color; font-size: $font-size-xxl; margin-bottom: $spacing-md; text-align: center; }
.dlg-body { color: $text-primary; font-size: $font-size-md; margin-bottom: $spacing-xl; }
.dlg-row { margin-bottom: $spacing-sm; span { color: $text-secondary; margin-right: $spacing-sm; } }
.dlg-actions { @include flex-between; }

@media (max-width: 992px) {
  .am-header { flex-direction: column; align-items: stretch; gap: $spacing-md; }
  .am-actions { justify-content: flex-end; }
}
</style>
