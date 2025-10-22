package com.txq.domain.infra.security;

/**
 * 密码加密接口
 * */
public interface PasswordEncryptor {

    /**
     * 加密密码
     * */
    String encrypt(String rawPassword);
}