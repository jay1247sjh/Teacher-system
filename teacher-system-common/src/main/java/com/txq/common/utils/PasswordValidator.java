package com.txq.common.utils;

import com.txq.common.result.ValidationResult;

import java.util.regex.Pattern;

/**
 * 密码校验器
 */
public class PasswordValidator {

    // 默认正则表达式（检查是否只包含英文、数字和常见符号）
    private static final String PASSWORD_REGEX = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{}|;:',.<>?/]+$";

    // 预编译 Pattern
    private static final Pattern DEFAULT_PASSWORD_PATTERN =
            Pattern.compile(PASSWORD_REGEX);

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
            return ValidationResult.fail("密码长度必须在 " + minLength + " 到 " + maxLength + " 位之间");
        }
        if (!DEFAULT_PASSWORD_PATTERN.matches(PASSWORD_REGEX, password)) {
            return ValidationResult.fail("密码格式只能包含英文字母、数字及常见符号");
        }
        return ValidationResult.success();
    }
}
