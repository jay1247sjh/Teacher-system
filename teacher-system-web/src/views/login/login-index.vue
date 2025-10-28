<template>
    <div class="login-container">
        <div class="login-wrapper">
            <!-- Logo区域 -->
            <div class="logo-section">
                <img src="@/assets/logo.png" alt="学校Logo" class="school-logo" />
                <h1 class="login-title">外国语学院教师管理系统</h1>
                <p class="login-subtitle">Staff Archive of SFL</p>
            </div>

            <!-- 登录表单区域 -->
            <div class="form-section">
                <div class="form-header">
                    <h2>欢迎登录</h2>
                    <p>请输入您的账号信息</p>
                </div>

                <form class="login-form" @submit="handleLogin">
                    <FormInput id="username" v-model="loginForm.username" label="用户名" placeholder="请输入用户名"
                        autocomplete="username" icon="user" required />

                    <FormInput id="password" v-model="loginForm.password" label="密码" placeholder="请输入密码"
                        autocomplete="current-password" icon="lock" show-password-toggle :show-password="showPassword"
                        required @toggle-password="togglePassword" />

                    <div class="form-options">
                        <label class="remember-me">
                            <input type="checkbox" v-model="loginForm.rememberMe" />
                            记住我
                        </label>
                        <a href="#" class="forgot-password">忘记密码？</a>
                    </div>

                    <button type="submit" class="login-btn" :disabled="loading">
                        {{ loading ? '' : '登录' }}
                        <span v-if="loading" class="loading-spinner"></span>
                    </button>

                    <div class="register-link">
                        还没有账号？
                        <router-link to="/register" class="link">立即注册</router-link>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import FormInput from '@/components/FormInput.vue'

export default defineComponent({
    name: 'LoginIndex',
    components: {
        FormInput
    },
    data() {
        return {
            loginForm: {
                username: '',
                password: '',
                rememberMe: false
            },
            showPassword: false,
            loading: false
        }
    },
    methods: {
        togglePassword() {
            this.showPassword = !this.showPassword
        },
        async handleLogin() {
            if (!this.loginForm.username || !this.loginForm.password) {
                return
            }

            this.loading = true

            try {
                // 这里添加登录逻辑
                console.log('登录信息:', this.loginForm)

                // 模拟API调用
                await new Promise((resolve) => setTimeout(resolve, 1000))

                // 登录成功后的处理
                console.log('登录成功')
            } catch (error) {
                console.error('登录失败:', error)
            } finally {
                this.loading = false
            }
        }
    }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.login-container {
    min-height: 100vh;
    @include gradient-bg(#f5f7fa, #c3cfe2);
    @include flex-center;
    padding: $spacing-xl;
    font-family: $font-family-primary;
}

.login-wrapper {
    display: flex;
    background: rgba($background-primary, 0.95);
    border-radius: $border-radius-large;
    @include shadow(3);
    overflow: hidden;
    max-width: 1000px;
    width: 100%;
    min-height: 600px;

    @include mobile {
        flex-direction: column;
        margin: $spacing-xl;
    }

    @include mobile-small {
        margin: 10px;
        border-radius: 15px;
    }
}

.logo-section {
    flex: 1;
    @include gradient-bg($secondary-color, $secondary-dark);
    @include flex-column-center;
    padding: $spacing-huge;
    color: $text-white;
    position: relative;

    &::before {
        content: '';
        @include absolute-full;
        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.05"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
        opacity: 0.3;
    }

    @include mobile {
        padding: $spacing-xl $spacing-lg;
    }

    @include mobile-small {
        padding: $spacing-lg 15px;
    }

    .school-logo {
        width: 120px;
        height: 120px;
        border-radius: 50%;
        object-fit: cover;
        border: 4px solid rgba($text-white, 0.9);
        @include shadow(2);
        transition: all $transition-normal;
        @include hover-scale;

        &:hover {
            @include shadow(3);
        }

        @include mobile {
            width: 80px;
            height: 80px;
        }
    }
}

.login-title {
    font-size: $font-size-giant;
    font-weight: $font-weight-light;
    margin: $spacing-xl 0 10px;
    text-align: center;
    position: relative;
    z-index: 1;

    @include mobile {
        font-size: $font-size-xxxl;
    }
}

.login-subtitle {
    font-size: $font-size-xxl;
    opacity: 0.8;
    text-align: center;
    position: relative;
    z-index: 1;
}

.form-section {
    flex: 1;
    padding: $spacing-huge $spacing-huge;
    @include flex-column-center;

    @include mobile {
        padding: $spacing-xl $spacing-lg;
    }

    @include mobile-small {
        padding: 30px 15px;
    }
}

.form-container {
    width: 100%;
    max-width: 400px;
}

.form-header {
    text-align: center;
    margin-bottom: $spacing-huge;

    h2 {
        color: $text-primary;
        font-size: $font-size-xxxl;
        font-weight: $font-weight-light;
        margin: 0 0 10px;
    }

    p {
        color: $text-secondary;
        font-size: $font-size-md;
        margin: 0;
    }
}

.login-form {
    width: 100%;

    .form-group {
        margin-bottom: $spacing-xl;

        label {
            display: block;
            color: $text-primary;
            font-size: $font-size-md;
            font-weight: $font-weight-medium;
            margin-bottom: $spacing-sm;
        }

        .input-wrapper {
            position: relative;

            i {
                @include icon-base;

                &.icon-user::before {
                    content: '👤';
                    font-size: $font-size-lg;
                }

                &.icon-lock::before {
                    content: '🔒';
                    font-size: $font-size-lg;
                }
            }

            input {
                @include input-base;
            }

            .password-toggle {
                @include password-toggle;
            }
        }
    }

    .form-options {
        @include flex-between;
        margin-bottom: $spacing-xl;

        .remember-me {
            @include flex-center;
            color: $text-secondary;
            font-size: $font-size-md;
            cursor: pointer;

            input {
                margin-right: $spacing-sm;
                accent-color: $primary-color;
            }
        }

        .forgot-password {
            color: $primary-color;
            text-decoration: none;
            font-size: $font-size-md;

            &:hover {
                text-decoration: underline;
            }
        }
    }

    .register-link {
        text-align: center;
        margin-bottom: $spacing-xl;
        color: $text-secondary;
        font-size: $font-size-md;

        .link {
            color: $primary-color;
            text-decoration: none;
            font-weight: $font-weight-medium;

            &:hover {
                text-decoration: underline;
            }
        }
    }

    .login-btn {
        @include button-primary;
        width: 100%;
        padding: $spacing-lg;
        font-size: $font-size-lg;
        @include hover-lift;

        &:hover:not(:disabled) {
            box-shadow: 0 10px 25px $shadow-primary;
        }

        &:active:not(:disabled) {
            transform: translateY(0);
        }

        &:disabled {
            opacity: 0.7;
            cursor: not-allowed;
        }

        .loading-spinner {
            @include loading-spinner;
        }
    }
}

// 响应式设计优化
@include mobile {
    .login-container {
        padding: $spacing-xl;
    }
}

@include mobile-small {
    .login-container {
        padding: 10px;
    }
}
</style>
