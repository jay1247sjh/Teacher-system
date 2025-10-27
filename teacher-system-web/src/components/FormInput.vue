<template>
    <div class="form-group">
        <label v-if="label" :for="id">{{ label }}</label>
        <div class="input-wrapper">
            <i v-if="icon" :class="`icon-${icon}`"></i>
            <input :id="id" :value="modelValue" :type="inputType" :placeholder="placeholder" :maxlength="maxlength"
                :autocomplete="autocomplete" :required="required" @input="handleInput" />
            <button v-if="showPasswordToggle" type="button" class="password-toggle" @click="$emit('toggle-password')">
                {{ showPassword ? '👁️' : '👁️' }}
            </button>
        </div>
        <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
        </div>
    </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
    name: 'FormInput',
    props: {
        id: {
            type: String,
            required: true
        },
        modelValue: {
            type: String,
            required: true
        },
        label: {
            type: String,
            default: ''
        },
        type: {
            type: String,
            default: 'text'
        },
        placeholder: {
            type: String,
            default: ''
        },
        maxlength: {
            type: Number,
            default: undefined
        },
        autocomplete: {
            type: String,
            default: ''
        },
        required: {
            type: Boolean,
            default: false
        },
        icon: {
            type: String,
            default: ''
        },
        showPasswordToggle: {
            type: Boolean,
            default: false
        },
        showPassword: {
            type: Boolean,
            default: false
        },
        errorMessage: {
            type: String,
            default: ''
        }
    },
    emits: ['update:modelValue', 'toggle-password'],
    computed: {
        inputType() {
            if (this.showPasswordToggle) {
                return this.showPassword ? 'text' : 'password'
            }
            return this.type
        }
    },
    methods: {
        handleInput(event: Event) {
            const target = event.target as HTMLInputElement
            this.$emit('update:modelValue', target.value)
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
