package com.txq.domain.service;

import com.txq.common.exception.BizException;
import com.txq.domain.infra.repository.RoleRepository;
import com.txq.domain.infra.repository.UserRepository;
import com.txq.domain.infra.repository.UserRoleRepository;
import com.txq.domain.model.Role;
import com.txq.domain.model.WorkId;

import java.util.List;

import static com.txq.domain.status.ErrorCode.ACCOUNT_EXIST_ERROR_CODE;

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
     * 获取用户roleId
     */
    public List<Integer> getRoleId(WorkId workId) {
        return userRoleRepository.getRoleIdById(workId.getId());
    }

    /**
     * 获取用户role信息
     */
    Role getRoleByRoleId(int roleId) {
        return roleRepository.getRoleByRoleId(roleId);
    }
}
