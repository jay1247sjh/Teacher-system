package com.txq.domain.service;

import com.txq.common.exception.BizException;
import com.txq.domain.model.User;
import com.txq.domain.model.WorkId;
import com.txq.domain.repository.UserRepository;
import com.txq.domain.security.PasswordEncryptor;

import static com.txq.domain.status.ErrorCode.ACCOUNT_EXIST_ERROR_CODE;

/**
 * 用户信息领域服务
 */
public class UserDomainService {

    private final UserRepository userRepository;

    public UserDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 检查用户是否已经注册
     */
    public void checkUserCanRegister(WorkId workId) {
        if (userRepository.existsById(workId.getId())) {
            throw new BizException(ACCOUNT_EXIST_ERROR_CODE, "账号已存在");
        }
    }
}
