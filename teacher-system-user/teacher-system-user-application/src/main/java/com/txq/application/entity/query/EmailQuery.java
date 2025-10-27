package com.txq.application.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱验证码参数Query
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailQuery {
    // 姓名
    private String username;

    // 邮箱
    private String email;
}
