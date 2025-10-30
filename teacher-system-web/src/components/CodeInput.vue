<template>
    <div class="form-group">
        <label v-if="label" :for="id">{{ label }}</label>
        <div class="email-code-wrapper">
            <div class="input-wrapper">
                <i class="icon-code"></i>
                <input :id="id" :value="modelValue" type="text" :placeholder="placeholder" :maxlength="maxlength"
                    :required="required" @input="handleInput" />
            </div>
            <button type="button" class="send-code-btn" :disabled="!canSend || countdown > 0" @click="sendCode">
                {{ countdown === 0 ? "发送验证码" : `${countdown}s后重发` }}
            </button>
        </div>
        <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, type PropType } from "vue";

export default defineComponent({
    name: "EmailCodeInput",
    props: {
        // 组件id
        id: {
            type: String as PropType<string>,
            required: true,
        },
        // 表单值
        modelValue: {
            type: String as PropType<string>,
            required: true,
        },
        // 标签名
        label: {
            type: String as PropType<string>,
            default: "",
        },
        // 占位符
        placeholder: {
            type: String as PropType<string>,
            default: "",
        },
        // 最长长度
        maxlength: {
            type: Number as PropType<number>,
            default: undefined,
        },
        // 是否必须
        required: {
            type: Boolean as PropType<boolean>,
            default: false,
        },
        // 是否可发送
        canSend: {
            type: Boolean as PropType<boolean>,
            required: true,
        },
        // 倒计时时间
        countdown: {
            type: Number as PropType<number>,
            default: 0,
        },
        // 错误信息
        errorMessage: {
            type: String as PropType<string>,
            default: "",
        },
    },
    emits: {
        "update:modelValue": (value: string) => typeof value === "string",
        "send-code": () => true
    },
    methods: {
        // 处理输入事件
        handleInput(event: Event) {
            const target = event.target as HTMLInputElement;
            this.$emit("update:modelValue", target.value);
        },
        // 发送验证码
        sendCode() {
            this.$emit("send-code");
        }
    },
});
</script>

<style scoped lang="scss">
@import "@/styles/variables.scss";
@import "@/styles/mixins.scss";

.form-group {
    margin-bottom: $spacing-xl;

    label {
        display: block;
        color: $text-primary;
        font-size: $font-size-md;
        font-weight: $font-weight-medium;
        margin-bottom: $spacing-sm;
    }

    .email-code-wrapper {
        display: flex;
        gap: 10px;

        .input-wrapper {
            flex: 1;
            position: relative;

            i {
                @include icon-base;

                &.icon-code::before {
                    content: "🔢";
                    font-size: $font-size-lg;
                }
            }

            input {
                @include input-base;
            }
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
</style>
