<template>
  <div class="table-layout">
    <aside class="left-tree flex-column bg-white shadow-lg">
      <div class="tree-container">
        <ul>
          <li>
            <details open>
              <summary class="tree-title clickable" @click="gotoHome">主页</summary>
            </details>
          </li>
        </ul>
        <ul>
          <li>
            <details open>
              <summary class="tree-title clickable">管理员管理</summary>
              <ul>
                <li class="tree-leaf clickable" @click="goToTableManager">表格管理</li>
                <li class="tree-leaf clickable" @click="goToAccountManager">账号管理</li>
              </ul>
            </details>
          </li>
          <li>
            <details open>
              <summary class="tree-title clickable">表格列表</summary>
              <ul>
                <li 
                  v-for="table in tableList" 
                  :key="table.tableId" 
                  class="tree-leaf clickable"
                  @click="goToTableDetail(table.tableId)"
                >
                  {{ table.tableFullName }}
                </li>
                <li v-if="tableList.length === 0" class="tree-leaf tree-leaf-empty">
                  暂无表格
                </li>
              </ul>
            </details>
          </li>
        </ul>
      </div>
      <div class="user-info flex-column-center">
        <AvatarUpload 
          :avatar="currentAvatar"
          :username="username"
          @update:avatar="currentAvatar = $event"
          @avatar-changed="handleAvatarChanged"
        />
        <div class="user-details">
          <span class="user-name">{{ username }}</span>
          <span class="user-id">工号: {{ userId }}</span>
        </div>
        <button class="btn-logout" @click="handleLogout" title="退出登录">
          <span class="logout-icon">🚪</span>
          <span class="logout-text">退出登录</span>
        </button>
      </div>
    </aside>
    <main class="layout-content">
      <RouterView v-slot="{ Component }">
        <keep-alive>
          <component :is="Component" />
        </keep-alive>
      </RouterView>
    </main>
  </div>
</template>

<script lang="ts">
import { defineComponent, provide } from 'vue';
import { getTableList, type TableListItem } from '@/api/table';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '@/store/user';
import AvatarUpload from '@/components/AvatarUpload.vue';

export default defineComponent({
  name: 'TableLayout',
  components: {
    AvatarUpload
  },
  data() {
    return {
      tableList: [] as TableListItem[],
      currentAvatar: null as string | null  // 当前头像路径
    }
  },
  computed: {
    // 从store获取用户信息
    username(): string {
      const userStore = useUserStore();
      return userStore.username || '未登录';
    },
    userId(): string {
      const userStore = useUserStore();
      return userStore.userId || '-';
    }
  },
  mounted() {
    this.loadTableList();
    // 初始化头像
    const userStore = useUserStore();
    this.currentAvatar = userStore.userInfo?.avatar || null;
  },
  created() {
    // 提供刷新表格列表的方法给子组件
    provide('refreshTableList', this.loadTableList);
  },
  methods: {
    // 头像更改回调
    handleAvatarChanged(newAvatar: string | null): void {
      console.log('头像已更改:', newAvatar);
      this.currentAvatar = newAvatar;
    },
    gotoHome(): void {
      this.$router.push({ name: 'HomeWelcome' });
    },
    goToTableManager(): void {
      this.$router.push({ name: 'TableManagement' });
    },
    goToAccountManager(): void {
      this.$router.push({ name: 'AccountManagement' });
    },
    goToTableDetail(tableId: number): void {
      this.$router.push({ 
        name: 'TableDetail', 
        params: { id: tableId } 
      });
    },
    async loadTableList(): Promise<void> {
      try {
        this.tableList = await getTableList();
      } catch (error) {
        console.error('加载表格列表失败:', error);
        ElMessage.error('加载表格列表失败');
      }
    },
    
    /**
     * 退出登录
     */
    async handleLogout(): Promise<void> {
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '退出确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );
        
        // 清除用户信息
        const userStore = useUserStore();
        userStore.clearUserInfo();
        
        ElMessage.success('已退出登录');
        
        // 跳转到登录页
        this.$router.push({ name: 'Login' });
      } catch (error) {
        // 用户取消退出
        console.log('取消退出登录');
      }
    }
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.table-layout {
  display: flex;
  min-height: 100vh;
  font-family: $font-family-primary;
  background: $background-secondary;
}

.left-tree {
  width: 260px;
  min-height: 100vh;
  background: $background-primary;
  border-right: 1px solid $border-color;
  @include shadow(2);
  @include flex-column-between;
  padding: $spacing-xl $spacing-lg;
  position: relative;
}

.layout-content {
  flex: 1;
  min-width: 0;
  background: transparent;
  padding: $spacing-huge; // 给桌面端适当内边距
}

/* 用户区、树样式可直接复用 */
.user-info {
  margin-top: $spacing-xxl;
  padding-top: $spacing-xl;
  border-top: 1px solid $border-color;
  text-align: center;
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

.btn-logout {
  margin-top: $spacing-lg;
  width: 100%;
  padding: $spacing-sm $spacing-md;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: $text-white;
  border: none;
  border-radius: $border-radius;
  font-size: $font-size-md;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  box-shadow: 0 2px 8px rgba(#ff6b6b, 0.2);
  
  .logout-icon {
    font-size: $font-size-lg;
  }
  
  .logout-text {
    font-size: $font-size-md;
  }
  
  &:hover {
    background: linear-gradient(135deg, #ff5252 0%, #e63946 100%);
    box-shadow: 0 4px 12px rgba(#ff6b6b, 0.3);
    transform: translateY(-1px);
  }
  
  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 6px rgba(#ff6b6b, 0.2);
  }
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

.tree-leaf-empty {
  color: $text-muted;
  font-style: italic;
  cursor: default !important;
  
  &:hover {
    color: $text-muted !important;
    background: transparent !important;
  }
}

/* 响应式：平板及以下 */
@media (max-width: 992px) {
  .table-layout {
    flex-direction: column;
  }

  .left-tree {
    width: 100%;
    min-height: auto;
    border-right: none;
    border-bottom: 1px solid $border-color;
    padding: $spacing-lg $spacing-md;
  }

  .layout-content {
    width: 100%;
    padding: $spacing-xl $spacing-lg;
  }

  .user-info {
    margin-top: $spacing-xl;
    padding-top: $spacing-lg;
  }
}

/* 极小屏：手机 */
@media (max-width: 576px) {
  .layout-content {
    padding: $spacing-lg $spacing-md;
  }

  .tree-container ul {
    padding-left: $spacing-md;
  }

  .user-avatar {
    width: 56px;
    height: 56px;
    font-size: $font-size-xxl;
  }

  .user-details .user-name {
    font-size: $font-size-md;
  }

  .user-details .user-id {
    font-size: $font-size-sm;
  }
}
</style>
