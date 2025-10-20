package com.txq.domain.model;

import com.txq.common.exception.BizException;
import com.txq.common.result.ValidationResult;
import com.txq.common.utils.EmailValidator;

import static com.txq.domain.status.ErrorCode.PASSWORD_VALIDATE_ERROR_CODE;

/**
 * 邮箱领域对象
 */
public class Email {

    private String email;

    public Email() {

    }

    public Email(String email) {
        this.email = email;
    }

    /**
     * 校验并返回
     * */
    public static Email of(String email) {
        validateEmailFormat(email);
        return new Email(email);
    }

    /**
     * 获取邮箱
     * */
    public String getEmail() {
        return email;
    }

    /**
     * 校验邮箱
     */
    private static void validateEmailFormat(String email) {
        ValidationResult result = EmailValidator.validEmail(email);
        // 邮箱格式不对
        if (!result.valid()) {
            throw new BizException(PASSWORD_VALIDATE_ERROR_CODE, result.message());
        }
    }
}
