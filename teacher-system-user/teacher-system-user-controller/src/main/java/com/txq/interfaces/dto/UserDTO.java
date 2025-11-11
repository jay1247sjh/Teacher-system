package com.txq.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
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
