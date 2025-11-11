package com.txq.common.result;

// 校验结果封装类
public record ValidationResult(boolean valid, String message) {

    public static ValidationResult success() {
        return new ValidationResult(true, "");
    }

    public static ValidationResult fail(String msg) {
        return new ValidationResult(false, msg);
    }
}