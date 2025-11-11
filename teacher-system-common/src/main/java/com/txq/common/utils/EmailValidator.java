package com.txq.common.utils;

import com.txq.common.result.ValidationResult;

import java.util.regex.Pattern;

/**
 * 邮箱校验器
 */
public final class EmailValidator {

    /**
     * 简洁通用的邮箱格式正则（不区分大小写）
     * <ul>
     *     <li>本地部分（@ 前）允许字母、数字、点、下划线、百分号、加号、减号。</li>
     *     <li>域名部分允许字母、数字、点和连字符，且必须有顶级域名。</li>
     *     <li>顶级域名长度 2~63。</li>
     * </ul>
     * 示例：
     * <pre>
     *     user@example.com          ✅
     *     user.name@sub.domain.com  ✅
     *     user+1@example.co.uk      ✅
     *     @example.com              ❌
     * </pre>
     */
    private static final String EMAIL_REGEX =
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$";

    /**
     * 预编译 Pattern
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

    // 工具类不允许实例化
    private EmailValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 校验邮箱格式是否合法
     *
     * @param email 待校验的邮箱字符串
     *
     * @return {@code true} 表示格式合法；{@code false} 表示格式不合法
     */
    public static ValidationResult validEmail(String email) {
        if (email == null) {
            return ValidationResult.fail("邮箱不能为空");
        }
        // 去除首尾空格
        String trimmed = email.trim();
        // 如果只有空格则错误
        if (trimmed.isEmpty()) {
            return ValidationResult.fail("邮箱不能只由空格组成");
        }
        if (EMAIL_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.fail("邮箱格式不正确");
        }
    }
}
