package com.txq.domain.infra.repository;

import java.util.List;

/**
 * 用户角色持久化层接口
 */
public interface UserRoleRepository {
    /**
     * 根据id获取角色id
     */
    List<Integer> getRoleIdById(String id);

    /**
     * 插入用户角色对应关系
     */
    void addUserRole(String workId, int roleId);
}
