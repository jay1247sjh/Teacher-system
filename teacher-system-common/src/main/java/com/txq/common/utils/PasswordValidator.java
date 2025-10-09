package com.txq.common.utils;

import java.util.regex.Pattern;

/**
 * 密码校验器
 */
public class PasswordValidator {

    // 默认正则表达式（检查是否只包含英文、数字和常见符号）
    private static final String regex = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{}|;:',.<>?/]+$";

    /**
     * 校验密码是否符合要求
     *
     * @param password  密码
     * @param minLength 最小长度
     * @param maxLength 最大长度
     */
    public static ValidationResult validatePassword(String password, int minLength, int maxLength) {
        // 检查密码是否为null或空字符串
        if (password == null || password.isEmpty()) {
            return ValidationResult.fail("密码不能为空");
        }
        // 检查密码是否存在空字符
        if (password.contains(" ")) {
            return ValidationResult.fail("密码不能存在空字符");
        }
        // 检查长度是否符合
        if (password.length() < minLength || password.length() > maxLength) {
            return ValidationResult.fail("密码长度：大于" + (minLength - 1) + "位 小于" + (maxLength + 1));
        }
        if (!Pattern.matches(regex, password)) {
            return ValidationResult.fail("密码格式必须由英文字母和数字组合");
        }
        return ValidationResult.success();
    }

    /**
     * 校验密码是否符合传入正则表达式
     */
    public static ValidationResult validatePassword(String password, String regex) {
        if (!Pattern.matches(regex, password)) {
            return ValidationResult.fail("密码格式错误");
        } else {
            return ValidationResult.success();
        }
    }

    // 校验结果封装类
    public record ValidationResult(boolean valid, String message) {

        public static ValidationResult success() {
            return new ValidationResult(true, "");
        }

        public static ValidationResult fail(String msg) {
            return new ValidationResult(false, msg);
        }
    }
}
