package com.txq.infrastructure.po;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserDO {
    // 工号
    private String id;

    // 用户名
    private String username;

    // 密码
    private String password;

    // 邮箱
    private String email;
}
