package com.txq.domain.model;

import com.txq.domain.infra.security.PasswordEncryptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户领域模型
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class User {
    // 工号
    private WorkId workId;

    // 用户名
    private String username;

    // 密码
    private Password password;

    // 邮箱
    private Email email;

    /**
     * 创建User领域根
     */
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
