package com.txq.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户PO
 * */
@Data
@Accessors(chain = true)
@TableName("user")
public class UserPO {
    // 工号
    private String id;

    // 用户名
    private String username;

    // 密码
    private String password;

    // 邮箱
    private String email;

    // 逻辑删除字段
    @TableLogic
    @JsonIgnore
    private Integer deleted;
}
