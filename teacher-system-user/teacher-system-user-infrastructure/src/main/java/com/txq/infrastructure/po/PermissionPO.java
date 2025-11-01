package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 权限PO
 */
@Data
@Accessors(chain = true)
@TableName("permission")
public class PermissionPO {
    // 权限ID
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 父级id,0为根节点
    private Integer parentId;

    // 权限名称
    private String permissionName;

    // 权限描述
    private String permissionDescription;

    // 权限类型：DIR, MENU, BUTTON, API
    private String type;

    // 路由路径
    private String path;

    // 重定向路径
    private String redirect;

    // 组件路径
    private String component;

    // 按钮权限标识
    private String perms;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;
}

