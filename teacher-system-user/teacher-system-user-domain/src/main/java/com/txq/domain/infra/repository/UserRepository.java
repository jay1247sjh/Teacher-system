package com.txq.domain.infra.repository;

import com.txq.domain.model.User;

/**
 * 用户信息持久化层接口
 */
public interface UserRepository {

    /**
     * 根据id判断是否存在
     */
    boolean existsById(String id);

    /**
     * 保存用户信息
     */
    void saveUser(User user);

    /**
     * 根据id获取密码
     */
    String getPasswordById(String id);

    /**
     * 根据id获取用户名
     */
    String getUsernameById(String id);
    
    /**
     * 获取所有普通用户列表（roleId=3）
     */
    java.util.List<java.util.Map<String, String>> getNormalUsers();
}