package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户角色PO
 */
@Data
@Accessors(chain = true)
@TableName("user_role")
public class UserRolePO {
    // id
    private int id;

    // 用户id
    private String userId;

    // 角色id
    private int roleId;
}
