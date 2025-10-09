package com.txq.application.service.impl;

import com.txq.application.dto.RegisterDTO;
import com.txq.application.service.ILoginService;
import com.txq.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录注册应用类
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements ILoginService {

    // 用户信息领域服务对象
    private final UserDomainService userDomainService;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void register(RegisterDTO registerDTO) {

    }
}
