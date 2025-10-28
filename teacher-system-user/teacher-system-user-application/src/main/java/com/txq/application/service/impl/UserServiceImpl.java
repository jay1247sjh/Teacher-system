package com.txq.application.service.impl;

import com.txq.application.entity.properties.KeyPairConfig;
import com.txq.application.entity.query.EmailQuery;
import com.txq.application.entity.query.LoginQuery;
import com.txq.application.entity.query.UserQuery;
import com.txq.application.entity.vo.UserVO;
import com.txq.application.service.IUserService;
import com.txq.common.exception.BizException;
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
import java.time.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.txq.common.result.Response.ResultCode.BUSINESS_ERROR;
import static com.txq.domain.status.ErrorCode.ACCOUNT_NOT_EXIST_ERROR_CODE;
import static com.txq.domain.status.ErrorCode.PASSWORD_ERROR_CODE;

/**
 * 登录注册应用类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

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
        // 持久化用户信息
        userRepository.saveUser(user);
        // 角色赋值为普通角色
        userRoleRepository.addUserRole(workId.getId(), 3);
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
        // 过期时间
        int expireTime = 30;
        // 获取工号对象
        WorkId id = WorkId.of(loginQuery.getId());
        // 校验账号和密码
        // 判断账号是否已注册
        if (!userRepository.existsById(id.getId())) {
            throw new BizException(ACCOUNT_NOT_EXIST_ERROR_CODE, "账号未注册");
        }
        // 从数据库获取注册密码
        String encryptedPassword = userRepository.getPasswordById(id.getId());
        // 判断密码是否相同
        if (!passwordEncryptor.matches(loginQuery.getPassword(), encryptedPassword)) {
            throw new BizException(PASSWORD_ERROR_CODE, "输入密码错误");
        }
        try {
            // 获取私钥Base64字符串
            String privateKey = keyPairConfig.getPrivateKey();
            // 还原为私钥对象
            PrivateKey privatekey = KeyUtils.getPrivatekey(privateKey);
            // 添加用户额外信息
            Map<String, Object> personalInfo = new HashMap<>();
            // 存入用户角色id
            personalInfo.put("roleId", userRoleRepository.getRoleIdById(id.getId()));
            // 生成Token
            String token = JwtUtils.generateToken(
                    privatekey,
                    Duration.ofMinutes(expireTime),
                    id.getId(),
                    "teacher-user-service",
                    "teacher-other-service",
                    personalInfo,
                    null
            );
            // 生成用户信息视图
            return new UserVO(
                    token,
                    Date.from(Instant.now().plus(Duration.ofMinutes(expireTime))).toInstant(),
                    id.getId(),
                    userRepository.getUsernameById(id.getId()),
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            throw new BizException(BUSINESS_ERROR);
        }
    }
}
