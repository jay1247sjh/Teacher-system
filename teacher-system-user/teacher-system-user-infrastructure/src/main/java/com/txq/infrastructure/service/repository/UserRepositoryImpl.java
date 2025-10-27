package com.txq.infrastructure.service.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.txq.domain.infra.repository.UserRepository;
import com.txq.domain.model.User;
import com.txq.infrastructure.assembler.UserAssembler;
import com.txq.infrastructure.mapper.UserMapper;
import com.txq.infrastructure.po.UserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


/**
 * 用户数据持久层
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
     * 保存注册用户
     */
    public void saveUser(User user) {
        userMapper.insert(UserAssembler.toPO(user));
    }

    /**
     * 根据id查找密码
     */
    @Override
    public String getPasswordById(String id) {
        LambdaQueryWrapper<UserPO> passwordQuery = new LambdaQueryWrapper<UserPO>()
                .select(UserPO::getPassword)
                .eq(UserPO::getId, id);
        // 提取密码
        return userMapper.selectOne(passwordQuery).getPassword();
    }
}
