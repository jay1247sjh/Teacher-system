package com.txq.domain.infra.repository;

/**
 * 用户角色持久化层接口
 */
public interface UserRoleRepository {
    /**
     * 根据id获取角色id
     */
    int getRoleIdById(String id);

    /**
     * 根据角色id获取角色信息
     * */

}
