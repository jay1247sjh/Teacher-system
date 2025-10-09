package com.txq.domain.model;

import com.txq.common.exception.BizException;
import com.txq.common.utils.PasswordValidator;
import com.txq.domain.security.PasswordEncryptor;
import lombok.Getter;
import lombok.Setter;

import static com.txq.domain.status.ErrorCode.PASSWORD_VALIDATE_ERROR_CODE;
import static com.txq.domain.status.ErrorCode.WORK_ID_VALIDATE_ERROR_CODE;

/**
 * 注册领域模型
 */
@Getter
@Setter
public class User {
    // 工号
    private String id;

    // 用户名
    private String username;

    // 密码
    private String password;

    // 邮箱
    private String email;

    public User(String id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**
     * 外部调用注册
     */
    public static User register(String id, String username, String rawPassword, String email,
                                PasswordEncryptor passwordEncryptor) {
        // 校验工号
        validateIdFormat(id);
        // 校验原始密码
        validatePasswordFormat(rawPassword);
        // 密码加密
        String encryptedPassword = passwordEncryptor.encrypt(rawPassword);
        // 返回领域对象
        return new User(id, username, email, encryptedPassword);
    }

    /**
     * 校验工号
     */
    private static void validateIdFormat(String id) {
        if (id == null || id.length() != 10) {
            throw new BizException(WORK_ID_VALIDATE_ERROR_CODE, "工号格式不正确");
        }
    }

    /**
     * 校验密码
     */
    private static void validatePasswordFormat(String password) {
        PasswordValidator.ValidationResult result = PasswordValidator.validatePassword(password, 6, 16);
        if (!result.valid()) {
            throw new BizException(PASSWORD_VALIDATE_ERROR_CODE, result.message());
        }
    }
}
