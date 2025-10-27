package com.txq.domain.infra.repository;

import com.txq.domain.model.Role;

/**
 * 角色信息持久化层接口
 */
public interface RoleRepository {
    /**
     * 根据角色id获取角色信息
     */
    Role getRoleByRoleId(int roleId);
}
