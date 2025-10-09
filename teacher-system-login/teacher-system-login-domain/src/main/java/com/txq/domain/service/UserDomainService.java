package com.txq.domain.service;

import com.txq.common.utils.PasswordValidator;
import com.txq.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户信息领域服务
 */
@Service
@RequiredArgsConstructor
public class UserDomainService {

    /**
     * 用户信息持久化
     */
    private final UserRepository userRepository;

    /**
     * PasswordEncryptor
     */
    private final PasswordValidator passwordValidator;

    /**
     * 注册用户
     */
    public void registerUser() {

    }
}
