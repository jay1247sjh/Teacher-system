package com.txq.infrastructure.repository;

import com.txq.domain.model.User;
import com.txq.domain.repository.UserRepository;
import com.txq.infrastructure.mapper.UserMapper;
import com.txq.infrastructure.po.UserDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


/**
 * 用户数据数据持久层
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    /**
     * 根据id查找是否已注册
     */
    public boolean existsById(String id) {
        return userMapper.selectById(id) != null;
    }

    /**
     * 保存对象
     */
    public void saveUser(User user) {
        UserDO userDO = new UserDO();

        userMapper.insert(userDO);
    }
}
