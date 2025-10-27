package com.txq.domain.service;

import com.txq.common.exception.BizException;
import com.txq.domain.infra.repository.RoleRepository;
import com.txq.domain.infra.repository.UserRepository;
import com.txq.domain.infra.repository.UserRoleRepository;
import com.txq.domain.model.Password;
import com.txq.domain.model.Role;
import com.txq.domain.model.WorkId;

import static com.txq.domain.status.ErrorCode.*;

/**
 * 用户信息领域服务
 */
public class UserDomainService {

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    private final RoleRepository roleRepository;

    public UserDomainService(UserRepository userRepository,
                             UserRoleRepository userRoleRepository,
                             RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * 检查用户是否已经注册
     */
    public void checkUserCanRegister(WorkId workId) {
        if (userRepository.existsById(workId.getId())) {
            throw new BizException(ACCOUNT_EXIST_ERROR_CODE, "账号已存在");
        }
    }

    /**
     * 判断账号密码是否正确
     */
    public void checkLoginForm(WorkId workId, Password password) {
        // 判断账号是否已注册
        if (!userRepository.existsById(workId.getId())) {
            throw new BizException(ACCOUNT_NOT_EXIST_ERROR_CODE, "账号未注册");
        }
        // 从数据库获取注册密码
        String rawPassword = userRepository.getPasswordById(workId.getId());
        // 将密码封装成Password对象以便比较
        Password encryptPassword = Password.encrypted(rawPassword);
        // 判断密码是否相同
        if (!encryptPassword.comparePassword(password)) {
            throw new BizException(PASSWORD_ERROR_CODE, "输入密码错误");
        }
    }

    /**
     * 获取用户roleId
     */
    public int getRoleId(WorkId workId) {
        return userRoleRepository.getRoleIdById(workId.getId());
    }

    /**
     * 获取用户role信息
     */
    Role getRoleByRoleId(int roleId) {
        return roleRepository.getRoleByRoleId(roleId);
    }
}
