package com.txq.application.service.impl;

import com.txq.application.query.EmailQuery;
import com.txq.application.query.UserQuery;
import com.txq.application.service.ILoginService;
import com.txq.common.service.CodeCache;
import com.txq.domain.event.SendMailEvent;
import com.txq.domain.infra.notice.MailService;
import com.txq.domain.infra.repository.UserRepository;
import com.txq.domain.infra.security.PasswordEncryptor;
import com.txq.domain.model.User;
import com.txq.domain.model.WorkId;
import com.txq.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 登录注册应用类
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements ILoginService {

    // 事件推送者
    private final ApplicationEventPublisher publisher;

    // 用户信息领域服务
    private final UserDomainService userDomainService;

    // 用户信息存储类
    private final UserRepository userRepository;

    // 密码加密类
    private final PasswordEncryptor passwordEncryptor;

    /**
     * 注册用户
     */
    @Override
    public void register(UserQuery userQuery) {
        // 工号值对象
        WorkId workId = new WorkId(userQuery.getId());
        // 调用领域服务判断是否可以注册
        userDomainService.checkUserCanRegister(workId);
        // 创建领域对象
        User user = User.create(
                userQuery.getId(),
                userQuery.getUsername(),
                userQuery.getPassword(),
                userQuery.getEmail(),
                passwordEncryptor
        );

        // 持久化
        userRepository.saveUser(user);
    }

    /**
     * 发送验证码
     */
    @Override
    public void sendCode(EmailQuery emailQuery) {
        publisher.publishEvent(new SendMailEvent(
                emailQuery.getEmail(),
                emailQuery.getUsername()
        ));
    }
}
