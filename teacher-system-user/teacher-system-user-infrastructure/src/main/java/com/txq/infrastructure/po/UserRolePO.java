package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 用户角色PO
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("user_role")
public class UserRolePO {
    // id
    @TableId(type = IdType.AUTO)
    private int id;

    // 用户id
    private String userId;

    // 角色id
    private int roleId;

    public UserRolePO(String userId, int roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
}
