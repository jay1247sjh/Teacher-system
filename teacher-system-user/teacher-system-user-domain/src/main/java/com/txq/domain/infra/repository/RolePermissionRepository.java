package com.txq.domain.infra.repository;

/**
 * 角色权限持久化层接口
 */
public interface RolePermissionRepository {
    /**
     * 根据id获取权限id
     */
    int getPermissionIdById(int id);
}
