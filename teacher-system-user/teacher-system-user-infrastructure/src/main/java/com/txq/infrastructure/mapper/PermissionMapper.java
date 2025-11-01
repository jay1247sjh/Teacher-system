package com.txq.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txq.infrastructure.po.PermissionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 */
@Mapper
public interface PermissionMapper extends BaseMapper<PermissionPO> {
    
    /**
     * 根据角色ID列表获取所有权限标识（perms）
     * 通过 user_role -> role_permission -> permission 关联查询
     */
    @Select("<script>" +
            "SELECT DISTINCT p.perms " +
            "FROM role_permission rp " +
            "JOIN permission p ON rp.permission_id = p.id " +
            "WHERE rp.role_id IN " +
            "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            "AND p.perms IS NOT NULL " +
            "AND p.perms != ''" +
            "</script>")
    List<String> selectPermsByRoleIds(@Param("roleIds") List<Integer> roleIds);
}

