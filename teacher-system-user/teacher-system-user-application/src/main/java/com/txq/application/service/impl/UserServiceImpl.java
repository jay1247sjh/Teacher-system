package com.txq.application.service.impl;

import com.txq.application.entity.properties.KeyPairConfig;
import com.txq.application.entity.query.EmailQuery;
import com.txq.application.entity.query.LoginQuery;
import com.txq.application.entity.query.UserQuery;
import com.txq.application.entity.vo.UserManagementVO;
import com.txq.application.entity.vo.UserVO;
import com.txq.application.service.IMetricsService;
import com.txq.application.service.IUserService;
import com.txq.common.exception.BizException;
import com.txq.domain.event.SendMailEvent;
import com.txq.domain.infra.notice.MailService;
import com.txq.domain.infra.repository.PermissionRepository;
import com.txq.domain.infra.repository.UserAvatarRepository;
import com.txq.domain.infra.repository.UserRepository;
import com.txq.domain.infra.repository.UserRoleRepository;
import com.txq.domain.infra.security.PasswordEncryptor;
import com.txq.domain.model.User;
import com.txq.domain.model.WorkId;
import com.txq.domain.service.UserDomainService;
import com.txq.infrastructure.mapper.UserMapper;
import com.txq.infrastructure.mapper.UserRoleMapper;
import com.txq.infrastructure.po.UserPO;
import com.txq.infrastructure.po.UserRolePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sjh.jwt.JwtUtils;
import sjh.jwt.KeyUtils;

import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.txq.domain.status.ErrorCode.*;

/**
 * 用户信息应用类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    // 事件推送者
    private final ApplicationEventPublisher publisher;

    // 用户信息领域服务
    private final UserDomainService userDomainService;

    // Mapper
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    // 用户邮箱处理类
    private final MailService mailService;

    // 用户信息存储类
    private final UserRepository userRepository;

    // 用户角色信息存储类
    private final UserRoleRepository userRoleRepository;

    // 权限信息存储类
    private final PermissionRepository permissionRepository;

    // 用户头像存储类
    private final UserAvatarRepository userAvatarRepository;

    // 密码加密类
    private final PasswordEncryptor passwordEncryptor;

    // 密钥配置类
    private final KeyPairConfig keyPairConfig;
    
    // 指标服务（用于业务监控）
    private final IMetricsService metricsService;

    /**
     * 注册用户
     */
    @Override
    public void register(UserQuery userQuery) {
        // 记录注册尝试
        metricsService.recordRegisterAttempt();
        
        try {
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
            
            // 记录注册成功
            metricsService.recordRegisterSuccess();
        } catch (Exception e) {
            // 记录注册失败
            metricsService.recordRegisterFailure();
            throw e;
        }
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
        // 记录验证码发送
        metricsService.recordVerificationCodeSent();
    }

    /**
     * 登录
     */
    @Override
    public UserVO login(LoginQuery loginQuery) {
        // 记录登录尝试
        metricsService.recordLoginAttempt();
        
        // 使用Timer记录登录耗时，并在登录成功/失败时记录相应指标
        return metricsService.recordLoginDuration(() -> {
            try {
                // 过期时间
                int expireTime = 30;
                // 获取工号对象
                WorkId id = WorkId.of(loginQuery.getId());
                // 校验账号和密码
                // 判断账号是否已注册
                if (!userRepository.existsById(id.getId())) {
                    metricsService.recordLoginFailure();
                    throw new BizException(ACCOUNT_NOT_EXIST_ERROR_CODE, "账号未注册");
                }
                // 从数据库获取注册密码
                String encryptedPassword = userRepository.getPasswordById(id.getId());
                // 判断密码是否相同
                if (!passwordEncryptor.matches(loginQuery.getPassword(), encryptedPassword)) {
                    metricsService.recordLoginFailure();
                    throw new BizException(PASSWORD_ERROR_CODE, "输入密码错误");
                }
                try {
                    // 获取私钥Base64字符串
                    String privateKey = keyPairConfig.getPrivateKey();
                    // 还原为私钥对象
                    PrivateKey privatekey = KeyUtils.getPrivatekey(privateKey);
                    // 获取用户角色ID列表
                    List<Integer> roleIds = userRoleRepository.getRoleIdById(id.getId());

                    // 获取用户权限列表（根据角色查询）
                    List<String> permissions = permissionRepository.getPermsByRoleIds(roleIds);

                    // 添加用户额外信息
                    Map<String, Object> personalInfo = new HashMap<>();
                    // 存入用户角色id
                    personalInfo.put("roleId", roleIds);
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
                    // 获取用户头像
                    String avatarPath = userAvatarRepository.getAvatarPathByUserId(id.getId());

                    // 记录登录成功，并将用户添加到在线列表
                    metricsService.recordLoginSuccess(id.getId());

                    // 生成用户信息视图（只返回权限，不返回角色）
                    return new UserVO(
                            token,
                            Date.from(Instant.now().plus(Duration.ofMinutes(expireTime))).toInstant(),
                            id.getId(),
                            userRepository.getUsernameById(id.getId()),
                            avatarPath,  // 返回头像路径
                            permissions,  // 只返回权限标识列表，前端基于权限判断
                            null
                    );
                } catch (BizException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("登录失败，系统错误", e);
                    metricsService.recordLoginFailure();
                    throw new BizException(LOGIN_SYSTEM_ERROR_CODE, "登录失败，请稍后重试");
                }
            } catch (BizException e) {
                throw e;
            }
        });
    }

    /**
     * 获取所有普通用户列表
     */
    @Override
    public List<com.txq.application.entity.vo.SimpleUserVO> getNormalUsers() {
        List<java.util.Map<String, String>> users = userRepository.getNormalUsers();
        return users.stream()
                .map(user -> com.txq.application.entity.vo.SimpleUserVO.builder()
                        .id(user.get("id"))
                        .username(user.get("username"))
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取所有用户列表（含角色信息，不含密码）- 超级管理员功能
     */
    @Override
    public List<UserManagementVO> getAllUsersForManagement() {
        log.info("获取所有用户列表（管理功能）");

        List<Map<String, Object>> users = userMapper.selectAllUsersWithRoles();

        return users.stream()
                .map(this::convertToUserManagementVO)
                .collect(Collectors.toList());
    }

    /**
     * 创建用户 - 超级管理员功能
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(String id, String username, String password, String email, String code, List<Integer> roleIds) {
        log.info("创建用户，工号: {}, 用户名: {}, 角色: {}", id, username, roleIds);

        // 检查用户是否已存在
        if (userRepository.existsById(id)) {
            throw new BizException(USER_ID_ALREADY_EXISTS_ERROR_CODE, "用户工号已存在");
        }

        // 如果提供了邮箱和验证码，则验证邮箱验证码
        if (email != null && !email.trim().isEmpty() && code != null && !code.trim().isEmpty()) {
            mailService.validateMailCode(email, code);
        }

        // 加密密码
        String encryptedPassword = passwordEncryptor.encrypt(password);

        // 创建用户
        UserPO userPO = new UserPO();
        userPO.setId(id);
        userPO.setUsername(username);
        userPO.setPassword(encryptedPassword);
        userPO.setEmail(email);
        userPO.setDeleted(0); // 0表示未删除
        userMapper.insert(userPO);

        // 分配角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Integer roleId : roleIds) {
                UserRolePO userRolePO = new UserRolePO();
                userRolePO.setUserId(id);
                userRolePO.setRoleId(roleId);
                userRoleMapper.insert(userRolePO);
            }
        }

        // 记录用户创建指标
        metricsService.recordUserCreated();
        log.info("用户创建成功，工号: {}", id);
    }

    /**
     * 更新用户 - 超级管理员功能
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(String id, String username, String email, List<Integer> roleIds, String newPassword) {
        log.info("更新用户，工号: {}, 用户名: {}, 角色: {}", id, username, roleIds);

        // 检查用户是否存在
        UserPO existingUser = userMapper.selectById(id);
        if (existingUser == null || existingUser.getDeleted() == 1) {
            throw new BizException(USER_NOT_EXIST_ERROR_CODE, "用户不存在");
        }

        // 更新用户信息
        UserPO userPO = new UserPO();
        userPO.setId(id);
        userPO.setUsername(username);
        userPO.setEmail(email);

        // 如果提供了新密码，则更新密码
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            String encryptedPassword = passwordEncryptor.encrypt(newPassword);
            userPO.setPassword(encryptedPassword);
        }

        userMapper.updateById(userPO);

        // 更新角色：先删除旧角色，再添加新角色
        if (roleIds != null) {
            // 删除旧角色
            userRoleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRolePO>()
                    .eq(UserRolePO::getUserId, id));

            // 添加新角色
            for (Integer roleId : roleIds) {
                UserRolePO userRolePO = new UserRolePO();
                userRolePO.setUserId(id);
                userRolePO.setRoleId(roleId);
                userRoleMapper.insert(userRolePO);
            }
        }

        // 记录用户更新指标
        metricsService.recordUserUpdated();
        log.info("用户更新成功，工号: {}", id);
    }

    /**
     * 删除用户（逻辑删除）- 超级管理员功能
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        log.info("删除用户，工号: {}", id);

        // 检查用户是否存在
        UserPO existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new BizException(USER_NOT_EXIST_ERROR_CODE, "用户不存在");
        }

        // 逻辑删除（MyBatis-Plus会自动将deleteById转换为UPDATE操作，设置deleted=1）
        userMapper.deleteById(id);

        // 记录用户删除指标
        metricsService.recordUserDeleted();
        log.info("用户删除成功，工号: {}", id);
    }

    /**
     * 将Map转换为UserManagementVO
     */
    private UserManagementVO convertToUserManagementVO(Map<String, Object> map) {
        String roleIdsStr = (String) map.get("role_ids");
        String roleNamesStr = (String) map.get("role_names");

        List<Integer> roleIds = new ArrayList<>();
        List<String> roleNames = new ArrayList<>();

        if (roleIdsStr != null && !roleIdsStr.isEmpty()) {
            roleIds = Arrays.stream(roleIdsStr.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }

        if (roleNamesStr != null && !roleNamesStr.isEmpty()) {
            roleNames = Arrays.asList(roleNamesStr.split(","));
        }

        return UserManagementVO.builder()
                .id((String) map.get("id"))
                .username((String) map.get("username"))
                .email((String) map.get("email"))
                .roleIds(roleIds)
                .roleNames(roleNames)
                .createTime((LocalDateTime) map.get("create_time"))
                .updateTime((LocalDateTime) map.get("update_time"))
                .build();
    }
}
