package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 角色PO
 */
@Data
@Accessors(chain = true)
@TableName("role")
public class RolePO {
    // id
    private int id;

    // 角色名称
    private String roleName;

    // 角色描述
    private String roleDescription;
}
