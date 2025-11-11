package com.txq.infrastructure.service.repository;

import com.txq.domain.infra.repository.RoleRepository;
import com.txq.domain.model.Role;
import com.txq.infrastructure.mapper.RoleMapper;
import com.txq.infrastructure.po.RolePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 角色数据持久层
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleMapper roleMapper;

    @Override
    public Role getRoleByRoleId(int roleId) {
        // 获取PO数据
        RolePO rolePO = roleMapper.selectById(roleId);
        return new Role(rolePO.getId(),
                rolePO.getRoleName(),
                rolePO.getRoleDescription(),
                null);
    }
}
