<template>
    <div class="error-page">
        <div class="error-content">
            <h1 class="error-code">404</h1>
            <h2 class="error-title">页面不存在</h2>
            <p class="error-description">抱歉，您访问的页面不存在或已被删除。</p>
            <div class="error-actions">
                <button class="btn-primary" @click="goHome">返回首页</button>
                <button class="btn-secondary" @click="goBack">返回上一页</button>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useRouter} from 'vue-router'

export default defineComponent({
    name: 'NotFound',
    setup() {
        const router = useRouter()
        
        const goHome = (): void => {
            router.push('/home')
        }
        
        const goBack = (): void => {
            router.back()
        }
        
        return {
            goHome,
            goBack
        }
    }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.error-page {
    min-height: 100vh;
    @include flex-center;
    @include gradient-bg(#f5f7fa, #c3cfe2);
    padding: $spacing-huge;
}

.error-content {
    text-align: center;
    background: rgba($background-primary, 0.95);
    border-radius: $border-radius-large;
    @include shadow(3);
    padding: $spacing-huge * 2;
}

.error-code {
    font-size: 120px;
    font-weight: $font-weight-light;
    @include text-gradient($primary-color, $secondary-color);
    margin-bottom: $spacing-lg;
}

.error-title {
    font-size: $font-size-xxxl;
    color: $text-primary;
    margin-bottom: $spacing-md;
}

.error-description {
    font-size: $font-size-lg;
    color: $text-secondary;
    margin-bottom: $spacing-huge;
}

.error-actions {
    @include flex-center;
    gap: $spacing-lg;
}

.btn-primary {
    @include button-primary;
    padding: $spacing-lg $spacing-xxxl;
}

.btn-secondary {
    @include button-outline;
    padding: $spacing-lg $spacing-xxxl;
}
</style>

