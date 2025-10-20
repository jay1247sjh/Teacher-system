package com.txq.application.service.impl;

import com.txq.application.command.UserCommand;
import com.txq.application.service.ILoginService;
import com.txq.domain.model.User;
import com.txq.domain.model.WorkId;
import com.txq.domain.repository.UserRepository;
import com.txq.domain.security.PasswordEncryptor;
import com.txq.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 登录注册应用类
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements ILoginService {

    // 用户信息领域服务
    private final UserDomainService userDomainService;

    // 用户信息存储类
    private final UserRepository userRepository;

    // 密码加密类
    private final PasswordEncryptor passwordEncryptor;

    /**
     * 注册用户
     * */
    public void register(UserCommand userCommand) {
        // 工号值对象
        WorkId workId = new WorkId(userCommand.getId());
        // 调用领域服务判断是否可以注册
        userDomainService.checkUserCanRegister(workId);
        // 创建领域对象
        User user = User.create(
                userCommand.getId(),
                userCommand.getUsername(),
                userCommand.getPassword(),
                userCommand.getEmail(),
                passwordEncryptor
        );
        // 持久化
        userRepository.saveUser(user);
    }
}
