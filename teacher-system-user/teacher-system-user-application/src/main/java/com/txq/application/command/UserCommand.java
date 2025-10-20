package com.txq.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户参数Command
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCommand {
    // 工号
    private String id;

    // 密码
    private String password;

    // 姓名
    private String username;

    // 邮箱
    private String email;
}
