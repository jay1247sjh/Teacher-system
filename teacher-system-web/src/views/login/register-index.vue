<template>
    <div class="register-container">
        <div class="register-wrapper">
            <!-- Logo区域 -->
            <div class="logo-section">
                <img src="@/assets/logo.jpg" alt="学校Logo" class="school-logo" />
                <h1 class="register-title">外国语学院教师管理系统</h1>
                <p class="register-subtitle">
                    Foreign Languages College Teacher Management System
                </p>
            </div>

            <!-- 注册表单区域 -->
            <div class="form-section">
                <div class="form-header">
                    <h2>教师注册</h2>
                    <p>请填写您的注册信息</p>
                </div>

                <form class="register-form" @submit.prevent="handleRegister">
                    <FormInput id="employeeId" v-model="registerForm.employeeId" label="工号" placeholder="请输入10位工号"
                        :maxlength="10" autocomplete="username" icon="id" required />

                    <FormInput id="password" v-model="registerForm.password" label="密码" placeholder="请输入密码"
                        autocomplete="new-password" icon="lock" show-password-toggle :show-password="showPassword"
                        required @toggle-password="togglePassword" />

                    <FormInput id="confirmPassword" v-model="registerForm.confirmPassword" label="确认密码"
                        placeholder="请再次输入密码" autocomplete="new-password" icon="lock" show-password-toggle
                        :show-password="showConfirmPassword" :error-message="passwordMismatch ? '两次输入的密码不一致' : ''"
                        required @toggle-password="toggleConfirmPassword" />

                    <FormInput id="name" v-model="registerForm.name" label="姓名" placeholder="请输入真实姓名"
                        autocomplete="name" icon="user" required />

                    <FormInput id="email" v-model="registerForm.email" label="邮箱" type="email" placeholder="请输入邮箱地址"
                        autocomplete="email" icon="email" :error-message="emailError ? '请输入有效的邮箱地址' : ''" required />

                    <EmailCodeInput id="emailCode" v-model="registerForm.emailCode" label="邮箱验证码" placeholder="请输入验证码"
                        :maxlength="6" :countdown="countdown" :canSend="canSendCode"
                        :error-message="codeError ? '验证码错误或已过期' : ''" required
                        @update:modelValue="registerForm.emailCode = $event" @send-code="sendEmailCode" />

                    <button type="submit" class="register-btn" :disabled="loading || !isFormValid">
                        {{ loading ? "" : "注册" }}
                        <span v-if="loading" class="loading-spinner"></span>
                    </button>

                    <div class="login-link">
                        已有账号？<router-link to="/login" class="link">立即登录</router-link>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import {useRouter} from "vue-router";
import FormInput from "@/components/FormInput.vue";
import EmailCodeInput from "@/components/CodeInput.vue";

export default defineComponent({
    name: "RegisterIndex",
    components: {
        FormInput,
        EmailCodeInput,
    },
    setup() {
        // 工号长度
        const EMPLOYEE_ID_LENGTH = 10;
        // 邮箱正则表达式
        const EMAIL_REGEX = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,63}$/i;

        // 路由实例
        const router = useRouter();

        return {
            EMPLOYEE_ID_LENGTH,
            EMAIL_REGEX,
            router,
        };
    },
    data() {
        return {
            // 注册表单
            registerForm: {
                // 工号
                employeeId: "",
                // 密码
                password: "",
                // 确认密码
                confirmPassword: "",
                // 姓名
                name: "",
                // 邮箱
                email: "",
                // 邮箱验证码
                emailCode: "",
            },
            // 显示密码
            showPassword: false,
            // 显示确认密码
            showConfirmPassword: false,
            // 加载状态
            loading: false,
            // 验证码错误
            codeError: false,
            // 验证码倒计时时间
            countdown: 0,
        };
    },
    computed: {
        // 判断两次输入密码是否相同
        passwordMismatch() {
            return (
                this.registerForm.password &&
                this.registerForm.confirmPassword &&
                this.registerForm.password !== this.registerForm.confirmPassword
            );
        },
        // 判断邮箱格式是否正确，true为错误
        emailError() {
            const email = this.registerForm.email.trim();
            // 如果邮箱为空，不显示错误
            if (email === '') {
                return false;
            }
            // 如果邮箱不为空但格式错误，显示错误
            return !this.EMAIL_REGEX.test(email);
        },
        // 判断是否可以发送验证码
        canSendCode() {
            const email = this.registerForm.email.trim();
            return email !== '' && this.EMAIL_REGEX.test(email);
        },
        // 表单校验
        isFormValid() {
            return (
                this.registerForm.employeeId.length === this.EMPLOYEE_ID_LENGTH &&
                this.registerForm.password &&
                this.registerForm.confirmPassword &&
                this.registerForm.name &&
                this.registerForm.email &&
                this.registerForm.emailCode &&
                !this.passwordMismatch &&
                !this.codeError
            );
        },
    },
    methods: {
        // 切换密码显示状态
        togglePassword() {
            this.showPassword = !this.showPassword;
        },
        // 切换确认密码显示状态
        toggleConfirmPassword() {
            this.showConfirmPassword = !this.showConfirmPassword;
        },
        // 发送验证码
        async sendEmailCode() {
            // 设置倒计时并开始倒计
            this.countdown = 60;
            let timer = setInterval(() => {
                this.countdown--;
                if (this.countdown <= 0) {
                    clearInterval(timer);
                }
            }, 1000);
        },
        // 注册
        async handleRegister() { },
    },
});
</script>

<style scoped lang="scss">
@import "@/styles/variables.scss";
@import "@/styles/mixins.scss";

.register-container {
    min-height: 100vh;
    @include gradient-bg(#f5f7fa, #c3cfe2);
    @include flex-center;
    padding: $spacing-xl;
    font-family: $font-family-primary;

    @include mobile {
        padding: $spacing-xl;
    }

    @include mobile-small {
        padding: 10px;
    }
}

.register-wrapper {
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
    @include flex-center;
    flex-direction: column;
    padding: $spacing-huge;
    color: $text-white;
    position: relative;

    &::before {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.05"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
        opacity: 0.3;
    }

    @include mobile {
        padding: $spacing-xl $spacing-lg;
    }

    @include mobile-small {
        padding: $spacing-lg 15px;
    }
}

.logo-container {
    position: relative;
    z-index: 1;
    margin-bottom: $spacing-xl;
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

.register-title {
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

.register-subtitle {
    font-size: $font-size-md;
    opacity: 0.8;
    text-align: center;
    position: relative;
    z-index: 1;
}

.form-section {
    flex: 1;
    padding: $spacing-huge;
    @include flex-column-center;
    overflow-y: auto;

    @include mobile {
        padding: $spacing-xl $spacing-lg;
    }

    @include mobile-small {
        padding: $spacing-lg 15px;
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

.register-form {
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

                &.icon-id::before {
                    content: "🆔";
                    font-size: $font-size-lg;
                }

                &.icon-user::before {
                    content: "👤";
                    font-size: $font-size-lg;
                }

                &.icon-lock::before {
                    content: "🔒";
                    font-size: $font-size-lg;
                }

                &.icon-email::before {
                    content: "📧";
                    font-size: $font-size-lg;
                }

                &.icon-code::before {
                    content: "🔢";
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

        .email-code-wrapper {
            display: flex;
            gap: 10px;

            .input-wrapper {
                flex: 1;
            }

            .send-code-btn {
                @include button-info;
                padding: $spacing-md $spacing-xl;
                font-size: $font-size-md;
                white-space: nowrap;
                min-width: 120px;
                @include hover-lift-small;

                &:hover:not(:disabled) {
                    box-shadow: 0 5px 15px $shadow-info;
                }

                &:active:not(:disabled) {
                    transform: translateY(0);
                }

                &:disabled {
                    opacity: 0.6;
                    cursor: not-allowed;
                    background: $text-light;
                }
            }

            @include mobile {
                flex-direction: column;

                .send-code-btn {
                    min-width: auto;
                    width: 100%;
                }
            }
        }

        .error-message {
            @include error-message;
        }
    }

    .form-options {
        margin-bottom: $spacing-xl;

        .agree-terms {
            @include flex-center;
            color: $text-secondary;
            font-size: $font-size-md;
            cursor: pointer;

            input {
                margin-right: $spacing-sm;
                accent-color: $primary-color;
            }

            .terms-link {
                color: $primary-color;
                text-decoration: none;

                &:hover {
                    text-decoration: underline;
                }
            }
        }
    }

    .register-btn {
        @include button-primary;
        width: 100%;
        padding: $spacing-lg;
        font-size: $font-size-lg;
        margin-bottom: $spacing-xl;
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

    .login-link {
        text-align: center;
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
}
</style>
