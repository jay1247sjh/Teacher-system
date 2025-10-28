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
}
