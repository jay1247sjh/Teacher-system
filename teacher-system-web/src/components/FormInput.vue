<template>
    <div class="form-group">
        <label v-if="label" :for="id">{{ label }}</label>
        <div class="input-wrapper">
            <i v-if="icon" :class="`icon-${icon}`"></i>
            <input :id="id" :value="modelValue" :type="inputType" :placeholder="placeholder" :maxlength="maxlength"
                :autocomplete="autocomplete" :required="required" @input="handleInput" />
            <button v-if="showPasswordToggle" type="button" class="password-toggle" @click="toggleShowPassword">
                {{ input.showPassword ? '👁️‍🗨️' : '👁️‍🗨️' }}
            </button>
        </div>
        <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'

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
                showPassword: true
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
        }

        .password-toggle {
            @include password-toggle;
        }
    }

    .error-message {
        @include error-message;
    }
}
</style>
