package com.txq.domain.infra.security;

/**
 * 密码加密接口
 */
public interface PasswordEncryptor {

    /**
     * 加密密码
     */
    String encrypt(String rawPassword);

    /**
     * 校验密码
     */
    boolean matches(String rawPassword, String encodedPassword);
}