package com.txq.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 权限领域模型
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Permission {
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

    // 重定向路由
    private String redirect;

    // 组件路径
    private String component;

    // 按钮权限标识
    private String perms;
}
