<template>
  <div class="home-welcome">
    <div class="welcome-card">
      <div class="welcome-icon">📋</div>
      <h2 class="welcome-title">欢迎使用教师管理系统</h2>
      <p class="welcome-sub">从左侧选择功能开始使用</p>
      <ul class="welcome-hints">
        <li><span>表格管理</span>：创建/编辑表格结构与字段</li>
        <li><span>账号管理</span>：配置成员账户与权限</li>
        <li>或选择下方表格列表直接进入相应表格</li>
      </ul>
      <div class="welcome-cta">小提示：可随时在右上角搜索或使用快捷键进行快速定位</div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
export default defineComponent({ name: 'HomeWelcome' })
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.home-welcome {
  width: 100%;
  min-height: 100vh;
  @include flex-center;
}

.welcome-card {
  width: 86%;
  max-width: 980px;
  background: rgba($background-primary, .96);
  border-radius: $border-radius-xl;
  padding: $spacing-huge;
  @include shadow(3);
  text-align: center;
}

.welcome-icon {
  font-size: 56px;
  margin-bottom: $spacing-lg;
}

.welcome-title {
  font-size: $font-size-giant;
  font-weight: $font-weight-semibold;
  color: $primary-color;
  margin-bottom: $spacing-sm;
}

.welcome-sub {
  color: $text-secondary;
  font-size: $font-size-lg;
  margin-bottom: $spacing-xxl;
}

.welcome-hints {
  list-style: none;
  padding: 0;
  margin: 0 0 $spacing-xl 0;

  li {
    color: $text-primary;
    font-size: $font-size-md;
    margin-bottom: $spacing-sm;

    span {
      color: $secondary-color;
      font-weight: $font-weight-medium;
    }
  }
}

.welcome-cta {
  margin-top: $spacing-lg;
  color: $text-muted;
  font-size: $font-size-sm;
}

@media (max-width: 992px) {
  .welcome-card {
    width: 92%;
    padding: $spacing-xxl;
  }

  .welcome-title {
    font-size: $font-size-xxxl;
  }
}
</style>
