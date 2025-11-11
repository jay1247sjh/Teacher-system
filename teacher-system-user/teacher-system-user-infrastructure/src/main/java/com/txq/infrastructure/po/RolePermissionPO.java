package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 角色权限PO
 */
@Data
@Accessors(chain = true)
@TableName("role_permission")
public class RolePermissionPO {
    // id
    private int id;

    // 父级id
    private int parentId;

    // 权限名称
    private String permissionName;

    // 权限描述
    private String permissionDescription;

    // 权限类型
    private String type;

    // 路由路径
    private String path;

    // 路由重定向
    private String redirect;

    // 路由组件
    private String component;

    // 按钮权限标识
    private String perms;
}
