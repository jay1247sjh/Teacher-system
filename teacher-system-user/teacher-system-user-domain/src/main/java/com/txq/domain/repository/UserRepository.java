package com.txq.domain.repository;

import com.txq.domain.model.User;

/**
 * 用户信息持久化层接口
 * */
public interface UserRepository {

    /**
     * 根据id判断是否存在
     * */
    boolean existsById(String id);

    /**
     * 保存用户信息
     * */
    void saveUser(User user);
}