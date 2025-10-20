package com.txq.infrastructure.security;

import com.txq.domain.security.PasswordEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码加密类
 */
@Component
@RequiredArgsConstructor
public class BCryptPasswordEncryptor implements PasswordEncryptor {

    private final BCryptPasswordEncoder encoder;

    /**
     * 使用BCrypt加密
     */
    @Override
    public String encrypt(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
