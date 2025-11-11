package com.txq.infrastructure.service.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.txq.domain.infra.repository.PermissionRepository;
import com.txq.domain.model.Permission;
import com.txq.infrastructure.mapper.PermissionMapper;
import com.txq.infrastructure.po.PermissionPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限持久化层实现
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionMapper permissionMapper;

    @Override
    public List<Permission> getPermissionByPermissionId(int id) {
        LambdaQueryWrapper<PermissionPO> queryWrapper = new LambdaQueryWrapper<PermissionPO>()
                .eq(PermissionPO::getId, id);
        
        return permissionMapper.selectList(queryWrapper)
                .stream()
                .map(this::convertToPermission)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getPermsByRoleIds(List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return permissionMapper.selectPermsByRoleIds(roleIds);
    }

    /**
     * 将PO转换为领域模型
     */
    private Permission convertToPermission(PermissionPO po) {
        return new Permission(
                po.getId(),
                po.getParentId(),
                po.getPermissionName(),
                po.getPermissionDescription(),
                po.getType(),
                po.getPath(),
                po.getRedirect(),
                po.getComponent(),
                po.getPerms()
        );
    }
}

