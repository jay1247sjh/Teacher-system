package com.txq.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txq.infrastructure.po.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    /**
     * 获取所有用户及其角色信息（不包含密码）
     */
    @Select("SELECT u.id, u.username, u.email, u.create_time, u.update_time, " +
            "GROUP_CONCAT(ur.role_id) as role_ids, " +
            "GROUP_CONCAT(r.role_name) as role_names " +
            "FROM user u " +
            "LEFT JOIN user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN role r ON ur.role_id = r.id " +
            "WHERE u.deleted = 0 " +
            "GROUP BY u.id, u.username, u.email, u.create_time, u.update_time " +
            "ORDER BY u.create_time DESC")
    List<Map<String, Object>> selectAllUsersWithRoles();
    
    /**
     * 根据用户ID获取用户及其角色信息（不包含密码）
     */
    @Select("SELECT u.id, u.username, u.email, u.create_time, u.update_time, " +
            "GROUP_CONCAT(ur.role_id) as role_ids, " +
            "GROUP_CONCAT(r.role_name) as role_names " +
            "FROM user u " +
            "LEFT JOIN user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN role r ON ur.role_id = r.id " +
            "WHERE u.id = #{userId} AND u.deleted = 0 " +
            "GROUP BY u.id, u.username, u.email, u.create_time, u.update_time")
    Map<String, Object> selectUserWithRolesById(@Param("userId") String userId);
}
