package com.txq.interfaces.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册参数DTO
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
}
