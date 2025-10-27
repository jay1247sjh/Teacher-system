package com.txq.infrastructure.service.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.txq.domain.infra.repository.UserRoleRepository;
import com.txq.infrastructure.mapper.UserRoleMapper;
import com.txq.infrastructure.po.UserRolePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 用户数据数据持久层
 */
@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final UserRoleMapper userRoleMapper;

    /**
     * 获取角色id
     */
    @Override
    public int getRoleIdById(String id) {
        LambdaQueryWrapper<UserRolePO> roleIdQuery = new LambdaQueryWrapper<UserRolePO>()
                .select(UserRolePO::getRoleId)
                .eq(UserRolePO::getUserId, id);
        // 获取角色id
        return userRoleMapper.selectOne(roleIdQuery).getRoleId();
    }
}
