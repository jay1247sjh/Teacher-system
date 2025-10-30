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
                <li class="tree-leaf clickable">校级项目EH4</li>
              </ul>
            </details>
          </li>
        </ul>
      </div>
      <div class="user-info flex-column-center">
        <div class="avatar-wrapper">
          <div class="user-avatar flex-center">TQ</div>
        </div>
        <div class="user-details">
          <span class="user-name">汤启强</span>
          <span class="user-id">工号: 10501</span>
        </div>
      </div>
    </aside>
    <main class="layout-content">
      <RouterView />
    </main>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';


export default defineComponent({
  name: 'TableLayout',
  methods: {
    gotoHome(): void {
      this.$router.push({ name: 'HomeWelcome' });
    },
    goToTableManager(): void {
      this.$router.push({ name: 'TableManagement' });
    },
    goToAccountManager(): void {
      this.$router.push({ name: 'AccountManagement' });
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
