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

    /**
     * 根据id获取用户名
     */
    @Override
    public String getUsernameById(String id) {
        LambdaQueryWrapper<UserPO> usernameQuery = new LambdaQueryWrapper<UserPO>()
                .select(UserPO::getUsername)
                .eq(UserPO::getId, id);
        // 提取用户名
        return userMapper.selectOne(usernameQuery).getUsername();
    }
    
    /**
     * 获取所有普通用户列表（roleId=3）
     */
    @Override
    public java.util.List<java.util.Map<String, String>> getNormalUsers() {
        // 查询所有普通用户（roleId=3）
        java.util.List<java.util.Map<String, Object>> resultMaps = userMapper.selectMaps(
                new LambdaQueryWrapper<UserPO>()
                        .select(UserPO::getId, UserPO::getUsername)
                        .inSql(UserPO::getId, 
                               "SELECT user_id FROM user_role WHERE role_id = 3")
                        .eq(UserPO::getDeleted, 0)
                        .orderByAsc(UserPO::getId)
        );
        
        // 转换为 Map<String, String>
        return resultMaps.stream()
                .map(map -> {
                    java.util.Map<String, String> stringMap = new java.util.HashMap<>();
                    stringMap.put("id", String.valueOf(map.get("id")));
                    stringMap.put("username", String.valueOf(map.get("username")));
                    return stringMap;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
