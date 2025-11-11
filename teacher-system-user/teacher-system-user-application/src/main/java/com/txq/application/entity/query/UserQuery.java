package com.txq.application.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户参数Query
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQuery {
    // 工号
    private String id;

    // 密码
    private String password;

    // 姓名
    private String username;

    // 邮箱
    private String email;

    // 验证码
    private String code;
}
