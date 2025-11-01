package com.txq.domain.infra.repository;

import com.txq.domain.model.Permission;

import java.util.List;

/**
 * 权限持久化层接口
 */
public interface PermissionRepository {
    /**
     * 根据id获取权限
     */
    List<Permission> getPermissionByPermissionId(int id);
    
    /**
     * 根据角色ID列表获取所有权限标识（perms）
     * @param roleIds 角色ID列表
     * @return 权限标识列表（去重后）
     */
    List<String> getPermsByRoleIds(List<Integer> roleIds);
}
