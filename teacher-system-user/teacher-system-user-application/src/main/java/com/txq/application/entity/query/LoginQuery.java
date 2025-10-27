package com.txq.application.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录表单Query
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginQuery {
    // 工号
    private String id;

    // 密码
    private String password;
}
