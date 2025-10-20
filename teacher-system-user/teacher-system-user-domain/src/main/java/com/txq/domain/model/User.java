package com.txq.domain.model;

import com.txq.domain.security.PasswordEncryptor;
import lombok.Getter;

/**
 * 注册领域模型
 */
@Getter
public class User {
    // 工号
    private WorkId workId;

    // 用户名
    private String username;

    // 密码
    private Password password;

    // 邮箱
    private Email email;

    public User(WorkId id, String username, Password password, Email email) {
        this.workId = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public static User create(String id, String username, String rawPassword, String email,
                              PasswordEncryptor passwordEncryptor) {
        return new User(
                WorkId.of(id),
                username,
                Password.of(rawPassword, passwordEncryptor),
                Email.of(email)
        );
    }
}
