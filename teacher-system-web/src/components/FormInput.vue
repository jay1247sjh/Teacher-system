<template>
    <div class="form-group">
        <label v-if="label" :for="id">{{ label }}</label>
        <div class="input-wrapper">
            <i v-if="icon" :class="`icon-${icon}`"></i>
            <input :id="id" :value="modelValue" :type="inputType" :placeholder="placeholder" :maxlength="maxlength"
                :autocomplete="autocomplete" :required="required" @input="handleInput" />
            <button v-if="showPasswordToggle" type="button" class="password-toggle" @click="toggleShowPassword">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path v-if="!input.showPassword" d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle v-if="!input.showPassword" cx="12" cy="12" r="3"></circle>
                    <path v-if="input.showPassword" d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line v-if="input.showPassword" x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
            </button>
        </div>
        <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
        </div>
    </div>
</template>

<script lang="ts">
import {defineComponent, type PropType} from 'vue'

interface InputState {
    showPassword: boolean
}

export default defineComponent({
    name: 'FormInput',
    props: {
        id: {
            type: String as PropType<string>,
            required: true
        },
        modelValue: {
            type: String as PropType<string>,
            required: true
        },
        label: {
            type: String as PropType<string>,
            default: ''
        },
        type: {
            type: String as PropType<string>,
            default: 'text'
        },
        placeholder: {
            type: String as PropType<string>,
            default: ''
        },
        maxlength: {
            type: Number as PropType<number>,
            default: undefined
        },
        autocomplete: {
            type: String as PropType<string>,
            default: ''
        },
        required: {
            type: Boolean as PropType<boolean>,
            default: false
        },
        icon: {
            type: String as PropType<string>,
            default: ''
        },
        showPasswordToggle: {
            type: Boolean as PropType<boolean>,
            default: false
        },
        errorMessage: {
            type: String as PropType<string>,
            default: ''
        }
    },
    emits: ['update:modelValue', 'toggle-password'],
    data(): { input: InputState } {
        return {
            input: {
                showPassword: false
            }
        }
    },
    computed: {
        inputType(): string {
            if (this.showPasswordToggle) {
                return this.input.showPassword ? 'text' : 'password'
            }
            return this.type
        }
    },
    methods: {
        // 处理输入
        handleInput(event: Event) {
            const target = event.target as HTMLInputElement
            this.$emit('update:modelValue', target.value)
        },
        // 反转密码显示
        toggleShowPassword(): void {
            this.input.showPassword = !this.input.showPassword
        }
    }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

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
                content: '🆔';
                font-size: $font-size-lg;
            }

            &.icon-user::before {
                content: '👤';
                font-size: $font-size-lg;
            }

            &.icon-lock::before {
                content: '🔒';
                font-size: $font-size-lg;
            }

            &.icon-email::before {
                content: '📧';
                font-size: $font-size-lg;
            }

            &.icon-code::before {
                content: '🔢';
                font-size: $font-size-lg;
            }
        }

        input {
            @include input-base;

            // 禁用浏览器自带的密码显示/隐藏按钮
            &::-ms-reveal,
            &::-ms-clear {
                display: none;
            }

            &::-webkit-credentials-auto-fill-button,
            &::-webkit-contacts-auto-fill-button {
                visibility: hidden;
                display: none !important;
                pointer-events: none;
                height: 0;
                width: 0;
                margin: 0;
            }
        }

        .password-toggle {
            @include password-toggle;
            color: $text-muted;
            
            svg {
                display: block;
            }

            &:hover {
                color: $primary-color;
            }
        }
    }

    .error-message {
        @include error-message;
    }
}
</style>
