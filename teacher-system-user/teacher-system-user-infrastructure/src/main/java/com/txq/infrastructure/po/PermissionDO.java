package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 权限PO
 */
@Data
@Accessors(chain = true)
@TableName("permission")
public class PermissionDO {
    // id
    @TableId(type = IdType.AUTO)
    private int id;

    // 角色id
    private int roleId;

    // 权限id
    private int permissionId;
}
