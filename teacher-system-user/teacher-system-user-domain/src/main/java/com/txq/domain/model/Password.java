package com.txq.domain.model;

import com.txq.common.exception.BizException;
import com.txq.common.result.ValidationResult;
import com.txq.common.utils.PasswordValidator;
import com.txq.domain.infra.security.PasswordEncryptor;

import static com.txq.domain.status.ErrorCode.PASSWORD_VALIDATE_ERROR_CODE;

/**
 * 密码领域对象
 */
public class Password {

    private String password;

    public Password() {

    }

    public Password(String password) {
        this.password = password;
    }

    /**
     * 校验并加密
     * */
    public static Password of(String rawPassword,
                              PasswordEncryptor passwordEncryptor) {
        // 校验密码
        validatePasswordFormat(rawPassword);
        // 加密密码
        String encryptPassword = passwordEncryptor.encrypt(rawPassword);
        return new Password(encryptPassword);
    }

    /**
     * 加密后初始化
     * */
    public static Password encrypted(String encryptedPassword) {
        return new Password(encryptedPassword);
    }

    /**
     * 获取密码
     * */
    public String getPassword() {
        return password;
    }

    /**
     * 校验密码
     */
    private static void validatePasswordFormat(String password) {
        // 校验密码格式
        ValidationResult result = PasswordValidator.validatePassword(password, 6, 16);
        // 密码格式不对
        if (!result.valid()) {
            throw new BizException(PASSWORD_VALIDATE_ERROR_CODE, result.message());
        }
    }
}
