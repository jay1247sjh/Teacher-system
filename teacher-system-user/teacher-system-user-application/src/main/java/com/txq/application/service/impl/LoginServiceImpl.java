package com.txq.application.service.impl;

import com.txq.application.entity.properties.KeyPairConfig;
import com.txq.application.entity.query.EmailQuery;
import com.txq.application.entity.query.LoginQuery;
import com.txq.application.entity.query.UserQuery;
import com.txq.application.entity.vo.UserVO;
import com.txq.application.service.ILoginService;
import com.txq.common.exception.BizException;
import com.txq.common.result.Response;
import com.txq.domain.event.SendMailEvent;
import com.txq.domain.infra.notice.MailService;
import com.txq.domain.infra.repository.UserRepository;
import com.txq.domain.infra.repository.UserRoleRepository;
import com.txq.domain.infra.security.PasswordEncryptor;
import com.txq.domain.model.Password;
import com.txq.domain.model.User;
import com.txq.domain.model.WorkId;
import com.txq.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import sjh.jwt.JwtUtils;
import sjh.jwt.KeyUtils;

import java.security.PrivateKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.txq.common.result.Response.ResultCode.BUSINESS_ERROR;

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

    // 用户邮箱处理类
    private final MailService mailService;

    // 用户信息存储类
    private final UserRepository userRepository;

    // 用户角色信息存储类
    private final UserRoleRepository userRoleRepository;

    // 密码加密类
    private final PasswordEncryptor passwordEncryptor;

    // 密钥配置类
    private final KeyPairConfig keyPairConfig;

    /**
     * 注册用户
     */
    @Override
    public void register(UserQuery userQuery) {
        // 工号值对象
        WorkId workId = new WorkId(userQuery.getId());
        // 调用领域服务判断是否可以注册
        userDomainService.checkUserCanRegister(workId);
        // 验证注册码
        mailService.validateMailCode(userQuery.getEmail(), userQuery.getCode());
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

    /**
     * 登录
     */
    @Override
    public UserVO login(LoginQuery loginQuery) {
        // 获取工号对象
        WorkId workId = WorkId.of(loginQuery.getId());
        // 获取密码对象
        Password password = Password.of(loginQuery.getPassword(), passwordEncryptor);
        // 校验账号和密码
        userDomainService.checkLoginForm(workId, password);
        try {
            // 获取私钥Base64字符串
            String privateKey = keyPairConfig.getPrivateKey();
            // 还原为私钥对象
            PrivateKey privatekey = KeyUtils.getPrivatekey(privateKey);
            // 添加用户额外信息
            Map<String, Object> personalInfo = new HashMap<>();
            // 存入用户角色id
            personalInfo.put("roleId", userRoleRepository.getRoleIdById(workId.getId()));
            // 生成Token
            JwtUtils.generateToken(
                    privatekey,
                    Duration.ofMinutes(30),
                    workId.getId(),
                    "teacher-user-service",
                    "teacher-other-service",
                    personalInfo,
                    null
            );
        } catch (Exception e) {
            throw new BizException(BUSINESS_ERROR);
        }
    }
}
