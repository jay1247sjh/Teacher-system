package com.txq.application.dto;

import lombok.Data;

/**
 * 注册DTO
 */
@Data
public class RegisterDTO {

    // 工号
    private String id;

    // 密码
    private String password;

    // 姓名
    private String username;

    // 邮箱
    private String email;
}
