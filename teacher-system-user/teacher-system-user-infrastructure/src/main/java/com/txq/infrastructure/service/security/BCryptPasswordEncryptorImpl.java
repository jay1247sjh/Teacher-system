package com.txq.infrastructure.service.security;

import com.txq.domain.infra.security.PasswordEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码加密服务
 */
@Component
@RequiredArgsConstructor
public class BCryptPasswordEncryptorImpl implements PasswordEncryptor {

    private final BCryptPasswordEncoder encoder;

    /**
     * 使用BCrypt加密
     */
    @Override
    public String encrypt(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
